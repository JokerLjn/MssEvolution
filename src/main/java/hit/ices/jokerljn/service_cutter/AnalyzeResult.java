package hit.ices.jokerljn.service_cutter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hit.ices.jokerljn.cost.MssCost;
import hit.ices.jokerljn.importer.ImportObject;
import hit.ices.jokerljn.importer.mss.Microservice;
import hit.ices.jokerljn.importer.mss.MicroserviceSystem;
import hit.ices.jokerljn.importer.mss.MssAtomicChange;
import hit.ices.jokerljn.importer.uml.classmodel.ClassDiagram;
import hit.ices.jokerljn.importer.uml.classmodel.ClassRelation;
import hit.ices.jokerljn.importer.uml.classmodel.UmlClass;
import hit.ices.jokerljn.quality.QualityScore;
import hit.ices.jokerljn.utils.FileUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/12/26 10:37
 * @desc analyze service cutter result
 */
public class AnalyzeResult {
    private static final String CASE1_INPUT_PATH = "service_cutter/case1/";
    private static final String CASE2_INPUT_PATH = "service_cutter/case2/";

    private static final String CASE1_CLASS_PATH = "input/case1/olduml/class.json";
    private static final String CASE1_MSS_PATH = "input/case1/mss/oldMss.json";

    private static final String CASE2_CLASS_PATH = "input/case2/olduml/class.json";
    private static final String CASE2_MSS_PATH = "input/case2/mss/oldMss.json";

    private static final String CASE1_OUTPUT_PATH = "src/main/resources/service_cutter/case1/";
    private static final String CASE2_OUTPUT_PATH = "src/main/resources/service_cutter/case2/";

    /**
     * analyze cutter result ms
     * @param caseNum case id
     * @param choice group number
     */
    public void analyze(int caseNum, int choice) throws IOException, URISyntaxException {
        String inputClassPath = "";
        String inputMsPath = "";
        String oldClassPath, oldMssPath;
        String outPath = "";
        String className = "class" + choice;
        String msName = "cutter_result" + choice;
        if (caseNum == 1) {
            inputClassPath = CASE1_INPUT_PATH;
            inputMsPath = CASE1_INPUT_PATH;
            outPath = CASE1_OUTPUT_PATH;
            oldClassPath = CASE1_CLASS_PATH;
            oldMssPath = CASE1_MSS_PATH;
        } else {
            inputClassPath = CASE2_INPUT_PATH;
            inputMsPath = CASE2_INPUT_PATH;
            outPath = CASE2_OUTPUT_PATH;
            oldClassPath = CASE2_CLASS_PATH;
            oldMssPath = CASE2_MSS_PATH;
        }
        String inputMssChangePath = inputClassPath + "mssChange" + choice + ".json";
        inputMsPath += msName + ".json";
        inputClassPath += className + ".json";

        // get class diagram
        ClassDiagram classDiagram = ImportObject.importClassDiagram(inputClassPath);
        // get ms data
        JSONObject msJson = JSON.parseObject(FileUtils.readFile(inputMsPath));
        JSONArray services = msJson.getJSONArray("services");

        // get mss
        MicroserviceSystem microserviceSystem = new MicroserviceSystem();
        String mssName = caseNum == 1 ? "DDD Cargo" : "AcmeAir";
        microserviceSystem.setName(mssName);
        microserviceSystem.setMicroservices(new ArrayList<>());
        // set ms
        for (int i=0; i<services.size(); i++) {
            JSONObject service = services.getJSONObject(i);
            microserviceSystem.getMicroservices().add(generateMs(classDiagram, service));
        }

        // analyze and write to file
        QualityScore scorer = new QualityScore();

        // generate data
        StringBuilder builder = new StringBuilder();

        // add ms info
        builder.append(microserviceSystem).append("----------------------------\n");

        // compute old mss
        ClassDiagram oldClass = ImportObject.importClassDiagram(oldClassPath);
        MicroserviceSystem oldMss = ImportObject.importMicroserviceSystem(oldMssPath);
        double oldCohesionDegree = scorer.getCohesionDegree(oldMss.getMicroservices());
        double oldCoupingDegree = scorer.getCoupingDegree(oldMss.getMicroservices(), oldClass);
        double oldScore = scorer.getQualityScoreByUml(oldMss.getMicroservices(), oldClass);
        builder.append("oldQualityScore = ").append(oldScore).append(", ").
                append("cohesionDegree = ").append(oldCohesionDegree).append(", ").
                append("coupingDegree = ").append(oldCoupingDegree).append("\n");

        double cohesionDegree = scorer.getCohesionDegree(microserviceSystem.getMicroservices());
        double coupingDegree = scorer.getCoupingDegree(microserviceSystem.getMicroservices(), classDiagram);
        double score = scorer.getQualityScoreByUml(microserviceSystem.getMicroservices(), classDiagram);

        builder.append("newQualityScore = ").append(score).append(", ").
                append("cohesionDegree = ").append(cohesionDegree).append(", ").
                append("coupingDegree = ").append(coupingDegree).append("\n");

        double decreaseDegree = (oldScore - score) / oldScore;
        builder.append("decreasePercent = ").append(decreaseDegree).append("\n");
        // get mss cost
        List<MssAtomicChange> changeList = ImportObject.importMssChange(inputMssChangePath);
        if (changeList != null) {
            double minCost = new MssCost().computeCost(changeList);
            builder.append("-----------------------------------\n").
                    append("mssCost = ").append(minCost).append("\n");
        }

        // write to file
        String fileName = "mssResult" + choice + ".txt";
        try {
            FileUtils.delFile(outPath + fileName);
            FileUtils.writeToFile(outPath, builder.toString(), fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Microservice generateMs(ClassDiagram classDiagram, JSONObject service) {
        Microservice ms = new Microservice();
        ms.setName(service.getString("name"));

        // set class
        Set<UmlClass> umlClassList = new HashSet<>();
        JSONArray classList = service.getJSONArray("nanoentities");
        for (int i=0; i<classList.size(); i++) {
            UmlClass temp = getClass(classDiagram, getClassName(classList.getString(i)));
            if (temp != null) {
                umlClassList.add(temp);
            }
        }
        ms.setClassList(new ArrayList<>(umlClassList));

        // set class relation
        List<ClassRelation> relations = new ArrayList<>();
        ms.setClassRelationList(relations);
        getClassRelation(classDiagram, ms);

        // set interfaceList
        ms.setInterfaceList(new HashSet<>());
        return ms;
    }

    private UmlClass getClass(ClassDiagram classDiagram, String className) {
        for (UmlClass umlClass : classDiagram.getClassList()) {
            if (className.equals(umlClass.getName())) {
                return umlClass;
            }
        }
        return null;
    }

    private void getClassRelation(ClassDiagram classDiagram, Microservice ms) {
        List<UmlClass> classList = ms.getClassList();

        for (ClassRelation relation : classDiagram.getClassRelationList()) {
            if (classList.contains(relation.getOrigin()) && classList.contains(relation.getDestination())) {
                ms.getClassRelationList().add(relation);
            }
        }
    }

    private static String getClassName(String entity) {
        return entity.split("\\.")[0];
    }
}
