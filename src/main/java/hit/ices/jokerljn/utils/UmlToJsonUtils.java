package hit.ices.jokerljn.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/12/6 9:31
 * @desc 解析 .uml文件得到 json格式的数据
 */
@Slf4j
public class UmlToJsonUtils {

    /**
     * 从输入路径中将.uml文件读取为 xml格式的数据，然后转化为 json格式的数据，最后写入到指定文件中
     */
    public static void getUmlClassDiagram(String inputPath, String outPath, String name, String filename) throws IOException, URISyntaxException {
        String fileXmlData = FileUtils.readFile(inputPath);
        if (fileXmlData.length() == 0) {
            throw new IllegalArgumentException(".UML文件内容为空");
        }
        String classDiagramData = getUmlClassData(fileXmlData, name).toJSONString();
        try {
            FileUtils.writeToFile(outPath, classDiagramData, filename);
            log.info(".uml文件 {} 解析构建类图JSON成功！", inputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析 xml 数据构建 类图 json 数据
     */
    public static JSONObject getUmlClassData(String inputData, String name) {
        JSONObject classDiagram = new JSONObject();
        classDiagram.put("name", name);
        JSONArray classList = new JSONArray();
        JSONArray relationList = new JSONArray();

        // 解析 xml 数据
        Document document = null;
        try {
            document = DocumentHelper.parseText(inputData);
            Element root = document.getRootElement();
            //  iterate through child elements of root get class
            Element nodes = root.element("nodes");
            for (Iterator<Element> it1 = nodes.elementIterator(); it1.hasNext();) {
                Element node = it1.next();
                classList.add(getClassJson(delPackage(node.getStringValue())));
            }

            //  iterate through child elements of root get class relation
            Element edges = root.element("edges");
            for (Iterator<Element> it1 = edges.elementIterator(); it1.hasNext();) {
                Element edge = it1.next();
                String from = delPackage(edge.attributeValue("source"));
                String to = delPackage(edge.attributeValue("target"));
                String type = delPackage(edge.attributeValue("relationship"));
                type = mapRelationToClassRelation(type);
                if (type != null) {
                    relationList.add(getRelationJson(from, to, type));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        classDiagram.put("classList", classList);
        classDiagram.put("classRelationList", relationList);

        return classDiagram;
    }

    public static void getOldMssModel(String inputPath, String outPath, String name, String filename) throws IOException {
        Map<String, String> inputMs = FileUtils.readAllFilesBehindPath(inputPath);
        JSONObject mss = new JSONObject();
        mss.put("name", name);
        JSONArray msList = new JSONArray();

        for (String fileName : inputMs.keySet()) {
            String data = inputMs.get(fileName);
            JSONObject msJson = getUmlClassData(data, fileName);
            msJson.put("interfaceList", new JSONArray());
            msList.add(msJson);
        }
        mss.put("microservices", msList);
        try {
            FileUtils.writeToFile(outPath, mss.toJSONString(), filename);
            log.info("目录 {} 下的 .uml 文件解析构建 OldMss JSON成功！", inputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将xml文件中的 relation 类型映射到 class relation type
     * @see hit.ices.jokerljn.importer.uml.classmodel.ClassRelation.RelationType
     */
    private static String mapRelationToClassRelation(String type) {
        switch (type) {
            case "DEPENDENCY" :
                type = "ClassDependency";
                break;
            case "TO_ONE":
                type = "ClassAssociation";
                break;
            case "TO_MANY":
                type = "ClassComposition";
                break;
            case "GENERALIZATION":
                type = "ClassInheritance";
                break;
            case "REALIZATION":
                type = "ClassRealization";
                break;
            default:
                type = null;
        }
        return type;
    }

    /**
     * get class json
     * type : all set Entity
     */
    private static JSONObject getClassJson(String name) {
        JSONObject classJson = new JSONObject();
        classJson.put("name", name);
        classJson.put("type","Entity");
        classJson.put("attributes", new ArrayList<>());
        classJson.put("methods", new ArrayList<>());
        return classJson;
    }

    /**
     * get class relation json
     */
    private static JSONObject getRelationJson(String from, String to, String type) {
        JSONObject relationJson = new JSONObject();
        relationJson.put("origin", from);
        relationJson.put("destination", to);
        relationJson.put("type", type);
        return relationJson;
    }

    /**
     * 去除类名中的包名
     */
    private static String delPackage(String classAllName) {
        String[] names = classAllName.split("\\.");
        return names[names.length - 1];
    }

    public static void getUmlSequenceData(String inputPath, String outPath, String name, String filename) throws IOException, URISyntaxException {
        Map<String, String> sequenceData = FileUtils.readAllFilesBehindPath(inputPath);
        JSONObject sequenceDiagram = new JSONObject();
        sequenceDiagram.put("name", name);
        sequenceDiagram.put("objects", new JSONArray());
        sequenceDiagram.put("methodCalls", new JSONArray());
        sequenceDiagram.put("combineClips", new JSONArray());

        Set<String> objectSet = new HashSet<>();
        for (String key : sequenceData.keySet()) {
            parseSequenceData(sequenceData.get(key), sequenceDiagram, objectSet);
        }

        // add objects
        objectSet.forEach(sequenceDiagram.getJSONArray("objects")::add);

        // write to file
        FileUtils.writeToFile(outPath, sequenceDiagram.toJSONString(), filename);
    }

    private static void parseSequenceData(String data, JSONObject sequenceDiagram, Set<String> objectSet) {
        JSONArray methodCalls = sequenceDiagram.getJSONArray("methodCalls");

        JSONObject sequenceJson = JSON.parseObject(data, Feature.DisableCircularReferenceDetect);
        JSONArray viewArray = sequenceJson.getJSONArray("ownedViews");
        Map<String, String> idNameMap = new HashMap<>();
        for (int i=0; i< viewArray.size(); i++) {
            JSONObject view = viewArray.getJSONObject(i);
            if ("UMLSeqLifelineView".equals(view.get("_type"))) {
                // generate object
                String viewId = view.getJSONArray("subViews").getJSONObject(1).getString("_id");
                String nameText = view.getJSONArray("subViews").getJSONObject(0).
                        getJSONArray("subViews").getJSONObject(1).getString("text");
                if (!nameText.contains(":")) {
                    idNameMap.put(viewId, nameText);
                    objectSet.add(nameText);
                }
            } else if ("UMLSeqMessageView".equals(view.get("_type"))) {
                // generate methodCall
                String nameText = view.getJSONArray("subViews").getJSONObject(0).getString("text");
                String fromId = view.getJSONObject("tail").getString("$ref");
                String toId = view.getJSONObject("head").getString("$ref");

                String fromName = idNameMap.get(fromId);
                String toName = idNameMap.get(toId);
                if (fromName != null && toName != null) {
                    JSONObject methodCall = new JSONObject();
                    methodCall.put("num", methodCalls.size() + 1);
                    methodCall.put("from", fromName);
                    methodCall.put("to", toName);
                    String methodName = "", type = "REPLY";
                    if (nameText.length() > 5) {
                        type = "SIMPLE";
                        methodName = nameText.substring(4);
                    }
                    methodCall.put("type", type);
                    methodCall.put("method", methodName);

                    methodCalls.add(methodCall);
                }
            }
        }

    }

}
