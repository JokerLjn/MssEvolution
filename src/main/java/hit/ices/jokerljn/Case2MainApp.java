package hit.ices.jokerljn;

import hit.ices.jokerljn.importer.InputData;
import hit.ices.jokerljn.importer.mss.MicroserviceSystem;
import hit.ices.jokerljn.importer.uml.UmlModel;
import hit.ices.jokerljn.importer.umlchange.UmlAtomicChange;
import hit.ices.jokerljn.mapRule.OptionalMssWithChange;
import hit.ices.jokerljn.quality.QualityScore;
import hit.ices.jokerljn.solve.Solver;
import hit.ices.jokerljn.solve.ViolenceSolver;
import hit.ices.jokerljn.utils.FileUtils;
import hit.ices.jokerljn.utils.MagicNoteUtils;
import hit.ices.jokerljn.utils.UmlToJsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/12/6 15:54
 * @desc case 2 acemair main application
 */
@Getter
@Setter
@Slf4j
public class Case2MainApp {
    private final String path = "input/case2/";
    private final String allPath = "src/main/resources/input/case2/";
    private final String outPath = "src/main/resources/output/case2/";

    private UmlModel oldUml;
    private MicroserviceSystem oldMss;

    private void generateUmlClassDiagram() {
        String inputPath = path + "puml/acmeair-monolithic-java.uml";
        String outPath = allPath + "olduml/";
        String fileName = "class.json";
        String name = "Acmeair";

        try {
            UmlToJsonUtils.getUmlClassDiagram(inputPath, outPath, name, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateOldMss() {
        String umlPath = allPath + "pMss";
        String outPath = allPath + "mss/";
        String fileName = "oldMss.json";
        String name = "Acmeair Mss";
        try {
            UmlToJsonUtils.getOldMssModel(umlPath, outPath, name, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateSequence() {
        String sequencePath = allPath + "puml/sequence/";
        String outPath = allPath + "olduml/";
        String fileName = "sequence.json";
        String name = "AcmeAir sequence";
        try {
            UmlToJsonUtils.getUmlSequenceData(sequencePath, outPath, name, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void importData() {
        try {
            MicroserviceSystem oldmss = MainApplication.importOldMss(getPath());
            UmlModel umlModel = MainApplication.importOldUml(getPath());
            MagicNoteUtils.printJerryNote();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void violenceGetResult(int choice, double decreaseDegree, boolean isTest, int testNum, boolean simpleRule) {
        InputData data = MainApplication.init(path, choice);
        List<List<UmlAtomicChange>> umlChanges = MainApplication.groupAndSort(data);

        String ruleName = "";
        if (simpleRule) {
            ruleName = "InSimpleRule_";
        }

        String name = "minResult_" + ruleName + decreaseDegree + ".txt";
        if (choice == 2) {
            name = "midResult_" + ruleName + decreaseDegree + ".txt";
        } else if (choice == 3) {
            if (isTest) {
                name = "maxResult_" + testNum + "_" + ruleName + decreaseDegree + ".txt";
            } else {
                name = "maxResult_" + ruleName + decreaseDegree + ".txt";
            }
        }

        FileUtils.delFile(outPath + name);
        Solver violenceSolver = new ViolenceSolver(data.getOldUmlModel(), data.getNewUmlModel(), data.getOldMss(), new QualityScore(0.5, 0.5), decreaseDegree);

        OptionalMssWithChange best = null;

        long start = System.currentTimeMillis();

        if (isTest) {
            List<Integer> random = new ArrayList<>();
            for (int i=0; i<testNum; i++) {
                random.add(new Random().nextInt(umlChanges.size()));
            }
            for (int x : random) {
                List<UmlAtomicChange> umlAtomicChangeList = umlChanges.get(x);
                best = Case1MainApp.getOptionalMssWithChange(violenceSolver, best, umlAtomicChangeList, simpleRule);
            }
        } else {
            best = Case1MainApp.getOptionalMssWithChange(umlChanges, violenceSolver, best, simpleRule);
        }
        long end = System.currentTimeMillis();

        // write to file
        if (best != null) {
            double time = (end - start) / 1000d / 60;
            violenceSolver.writeBestSolutionToFile(best, outPath, name, time);
            MagicNoteUtils.printJerryNote();
        } else {
            System.out.println("-------------------------------------------------------------\n");
            log.warn("以微服务系统恶化程度不超过 {} 为约束条件，未找到满足的方案，请调高参数后再次尝试！", decreaseDegree);
        }
    }

    public static void main(String[] args) {
        Case2MainApp app = new Case2MainApp();
        try {
//            app.violenceGetResult(1, 0.5, false, 5);
//            app.violenceGetResult(2, 0.5, false, 5);
//            double degree = -1.0;
//            for (int i=0; i<=10; i++) {
//                app.violenceGetResult(1, degree, false, 5, true);
//                app.violenceGetResult(2, degree, false, 5, true);
//                app.violenceGetResult(3, degree, false, 5, true);
//                degree += 0.2;
//            }
            app.violenceGetResult(1, 1.0, false, 5, false);
            app.violenceGetResult(2, 1.0, false, 5, false);
            app.violenceGetResult(3, 1.8, false, 5, false);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
