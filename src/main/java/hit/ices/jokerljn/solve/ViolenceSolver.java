package hit.ices.jokerljn.solve;

import hit.ices.jokerljn.MainApplication;
import hit.ices.jokerljn.importer.mss.MicroserviceSystem;
import hit.ices.jokerljn.importer.uml.UmlModel;
import hit.ices.jokerljn.importer.umlchange.UmlAtomicChange;
import hit.ices.jokerljn.mapRule.OptionalMssWithChange;
import hit.ices.jokerljn.quality.QualityScore;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/28 10:23
 * @desc
 */
@Slf4j
public class ViolenceSolver extends Solver {

    public ViolenceSolver(UmlModel oldUmlModel, UmlModel newUmlModel, MicroserviceSystem oldMss, QualityScore qualityScore, double decreaseDegree) {
        super(oldUmlModel, newUmlModel, oldMss, qualityScore, decreaseDegree);
    }
    /**
     *  暴力求解器 ： 通过穷举的方法将 每个排序好的Uml变化列表的最优方案 结果 写入到文件中
     */
    @Override
    public OptionalMssWithChange solve(List<UmlAtomicChange> umlAtomicChanges, boolean simpleRule) {
        List<OptionalMssWithChange> results = MainApplication.mapFromUmlToMss(umlAtomicChanges, cloner.deepClone(oldMss), cloner.deepClone(oldUmlModel), newUmlModel, simpleRule);
        OptionalMssWithChange bestOption = null;
        Double minCost = null;
        // 找到最优的演化方案
        for (OptionalMssWithChange optionalMssWithChange : results) {
            if (checkConstraint(optionalMssWithChange)) {
                if (minCost == null) {
                    bestOption = optionalMssWithChange;
                    minCost = mssCost.computeCost(optionalMssWithChange.getMssAtomicChangeList());
                } else {
                    double tempCost = mssCost.computeCost(optionalMssWithChange.getMssAtomicChangeList());
                    if (tempCost < minCost) {
                        bestOption = optionalMssWithChange;
                        minCost = tempCost;
                    }
                }
            }
        }
        if (bestOption == null) {
            return null;
        }
        bestOption.setCost(minCost);
        return bestOption;
    }
}
