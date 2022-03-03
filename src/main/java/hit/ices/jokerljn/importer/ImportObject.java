package hit.ices.jokerljn.importer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import hit.ices.jokerljn.importer.mss.MicroserviceSystem;
import hit.ices.jokerljn.importer.mss.MssAtomicChange;
import hit.ices.jokerljn.importer.uml.classmodel.ClassDiagram;
import hit.ices.jokerljn.importer.uml.sequence.SequenceDiagram;
import hit.ices.jokerljn.importer.umlchange.*;
import hit.ices.jokerljn.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 21:20
 * @desc import json String to object
 */
@Slf4j
public class ImportObject {
    /**
     * 按路径读取json文件，反序列化为 T class
     *
     * @param filepath: 文件路径
     * @param type 参数类型
     * @param <T> 泛型类型
     * @return T class
     */
    private static <T> T readJsonToObject(String filepath, final Class<T> type) throws IOException, URISyntaxException {
        // get json string
        String jsonString = FileUtils.readFile(filepath);
        ObjectMapper objectMapper = new ObjectMapper();
        T resultClass = objectMapper.readValue(jsonString, type);
        log.info("对象 {} 反序列化成功！", type);
        return resultClass;
    }

    /**
     * import classDiagram
     *
     * @param filepath: 文件路径
     * @return classDiagram
     */
    public static ClassDiagram importClassDiagram(String filepath) throws IOException, URISyntaxException {
        ClassDiagram classDiagram = readJsonToObject(filepath, ClassDiagram.class);
        log.info("类图 {} 导入成功！", classDiagram.getName());
        return classDiagram;
    }

    /**
     * import classDiagram
     *
     * @param filepath: 文件路径
     * @return sequenceDiagram
     */
    public static SequenceDiagram importSequenceDiagram(String filepath) throws IOException, URISyntaxException {
        SequenceDiagram sequenceDiagram = readJsonToObject(filepath, SequenceDiagram.class);
        log.info("时序图 {} 导入成功！", sequenceDiagram.getName());
        return sequenceDiagram;
    }

    /**
     * import mss
     *
     * @param filepath path
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static MicroserviceSystem importMicroserviceSystem(String filepath) throws IOException, URISyntaxException {
        MicroserviceSystem microserviceSystem = readJsonToObject(filepath, MicroserviceSystem.class);
        log.info("微服务系统 {} 导入成功！", microserviceSystem.getName());
        return microserviceSystem;
    }

    /**
     * import uml change
     *
     * @param path file path
     * @return
     * @throws IOException
     */
    public static List<UmlAtomicChange> importUmlChange(String path) throws IOException, URISyntaxException {
        List<UmlAtomicChange> changeList = new ArrayList<>();

        // 解析 json 字符串
        JSONArray inputJson = JSON.parseArray(FileUtils.readFile(path));

        for (int i=0; i<inputJson.size(); i++) {
            JSONObject change = inputJson.getJSONObject(i);
            try {
                changeList.add(getChangeFromJson(change));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return changeList;
    }

    private static UmlAtomicChange getChangeFromJson(JSONObject change) throws IllegalArgumentException {
        String obj = change.getString("obj");
        UmlAtomicChange.OptObj optObj = UmlAtomicChange.OptObj.valueOf(obj);
        UmlAtomicChange atomicChange;
        switch (optObj) {
            case CLASS:
                atomicChange = JSONObject.toJavaObject(change, ClassChange.class);
                break;
            case ATTRIBUTE:
                atomicChange = JSONObject.toJavaObject(change, AttributeChange.class);
                break;
            case METHOD:
                atomicChange = JSONObject.toJavaObject(change, MethodChange.class);
                break;
            case RELATION:
                atomicChange = JSONObject.toJavaObject(change, RelationShipChange.class);
                break;
            case OBJECT:
                atomicChange = JSONObject.toJavaObject(change, ObjectChange.class);
                break;
            case MESSAGE:
                atomicChange = JSONObject.toJavaObject(change, MessageChange.class);
                break;
            default:
                throw new IllegalArgumentException("输入uml原子变化错误，找不到匹配类型！");
        }

        return atomicChange;
    }

    public static List<MssAtomicChange> importMssChange(String path)
    {
        List<MssAtomicChange> changeList = new ArrayList<>();

        // 解析 json 字符串
        JSONArray inputJson = null;
        try {
            inputJson = JSON.parseArray(FileUtils.readFile(path));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        for (int i=0; i<inputJson.size(); i++) {
            JSONObject change = inputJson.getJSONObject(i);
            try {
                changeList.add(getMssChangeFromJson(change));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return changeList;
    }

    private static MssAtomicChange getMssChangeFromJson(JSONObject change) {
        MssAtomicChange mssChange = new MssAtomicChange();
        mssChange.setObj(MssAtomicChange.OptObj.valueOf(change.getString("obj")));
        mssChange.setType(MssAtomicChange.OptType.valueOf(change.getString("type")));
        mssChange.setMsName(change.getString("msName"));
        mssChange.setValueMap(change.getJSONObject("valueMap").getInnerMap());

        return mssChange;
    }
}
