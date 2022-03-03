package hit.ices.jokerljn;

import hit.ices.jokerljn.importer.InputData;
import hit.ices.jokerljn.importer.umlchange.UmlAtomicChange;
import hit.ices.jokerljn.mapRule.OptionalMssWithChange;
import hit.ices.jokerljn.quality.QualityScore;
import hit.ices.jokerljn.solve.Solver;
import hit.ices.jokerljn.solve.ViolenceSolver;
import hit.ices.jokerljn.utils.FileUtils;
import hit.ices.jokerljn.utils.MagicNoteUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/12/13 15:54
 * @desc
 */
@Slf4j
public class Case1MainApp {

    private final String path = "input/case1/";
    private final String allPath = "src/main/resources/input/case1/";
    private final String outPath = "src/main/resources/output/case1/";

    /**
     * get map result
     * @param choice case num :1 - 3
     * @param decreaseDegree
     * @param isTest
     * @param testNum
     * @param simpleRule use simpleRule : true or false
     */
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
                name = "maxResult_" + testNum + "_" + ruleName  + decreaseDegree + ".txt";
            } else {
                name = "maxResult_" + decreaseDegree + ruleName  + ".txt";
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
                best = getOptionalMssWithChange(violenceSolver, best, umlAtomicChangeList, simpleRule);
            }
        } else {
            best = getOptionalMssWithChange(umlChanges, violenceSolver, best, simpleRule);
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

    static OptionalMssWithChange getOptionalMssWithChange(List<List<UmlAtomicChange>> umlChanges, Solver violenceSolver, OptionalMssWithChange best, boolean simpleRule) {
        for (List<UmlAtomicChange> umlAtomicChangeList : umlChanges) {
            best = getOptionalMssWithChange(violenceSolver, best, umlAtomicChangeList, simpleRule);
        }
        return best;
    }

    static OptionalMssWithChange getOptionalMssWithChange(Solver violenceSolver, OptionalMssWithChange best, List<UmlAtomicChange> umlAtomicChangeList, boolean simpleRule) {
        try {
            OptionalMssWithChange tempBest = violenceSolver.solve(umlAtomicChangeList, simpleRule);
            if (tempBest != null) {
                if (best == null) {
                    best = tempBest;
                } else {
                    if (best.getCost() < tempBest.getCost() ||
                            (best.getCost() == tempBest.getCost() && best.getQualityScore() < tempBest.getQualityScore())) {
                        best = tempBest;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return best;
    }



    public static void main(String[] args) {
        Case1MainApp app = new Case1MainApp();
        try {
//            app.violenceGetResult(1, 1.0, false, 5);
//            app.violenceGetResult(2, 1.0, false, 5);
//            app.violenceGetResult(3, 0.5, true, 600);
//            app.violenceGetResult(1, 1.0, false, 5, true);
//            app.violenceGetResult(2, 1.0, false, 5, true);
//            app.violenceGetResult(3, 1.0, true, 20, true);
            double degree = -1.0;
            for (int i=0; i<=10; i++) {
                app.violenceGetResult(1, degree, false, 5, true);
                app.violenceGetResult(2, degree, false, 5, true);
                app.violenceGetResult(3, degree, true, 5, true);
                degree += 0.2;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("出错了！");
        }
    }

}
