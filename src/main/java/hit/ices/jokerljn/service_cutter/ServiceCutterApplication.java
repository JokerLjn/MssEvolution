package hit.ices.jokerljn.service_cutter;

import hit.ices.jokerljn.MainApplication;
import hit.ices.jokerljn.importer.ImportObject;
import hit.ices.jokerljn.importer.uml.classmodel.ClassDiagram;
import hit.ices.jokerljn.utils.FileUtils;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/12/23 14:39
 * @desc
 */
public class ServiceCutterApplication {
    private final String case1_path = "input/case1/";
    private final String case2_path = "input/case2/";
    private final String case1_inputPath = "service_cutter/case1/";
    private final String case2_inputPath = "service_cutter/case2/";
    private final String case1_outPath = "src/main/resources/service_cutter/case1/";
    private final String case2_outPath = "src/main/resources/service_cutter/case2/";

    /**
     * 生成 2 个案例的变化后的 UML 类图 json文件
     */
    public void generateNewClassDiagram() {
        try {
            for (int i=1; i<=3; i++) {
                MainApplication.generateNewClassJson(case1_path, i, case1_outPath,"class" + i + ".json");
                MainApplication.generateNewClassJson(case2_path, i, case2_outPath,"class" + i + ".json");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateInput(String casePath, String outPath, String fileName) throws IOException, URISyntaxException {
        ClassDiagram classDiagram = ImportObject.importClassDiagram(casePath);
        try {
            String inputJson = ChangeToServiceCutterInput.getServiceCutterInput(classDiagram);
            FileUtils.writeToFile(outPath, inputJson, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将 2 个案例的变化后的类图json文件转化为 service cutter 可读的 json 格式
     */
    public void generateTwoCasesInput() {
        try {
            for (int i=1; i<=3; ++i) {
                String className = "class" + i + ".json";
                generateInput(case1_inputPath + className, case1_outPath, "cutter_class" + i + ".json");
                generateInput(case2_inputPath + className, case2_outPath, "cutter_class" + i + ".json");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void analyzeMss() {
        AnalyzeResult analyzer = new AnalyzeResult();
        try {
//            analyzer.analyze(1, 1);
//            analyzer.analyze(1, 2);
//            analyzer.analyze(1, 3);
            analyzer.analyze(2, 1);
            analyzer.analyze(2, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ServiceCutterApplication serviceCutter = new ServiceCutterApplication();
//        serviceCutter.generateTwoCasesInput();
        serviceCutter.analyzeMss();
    }
}
