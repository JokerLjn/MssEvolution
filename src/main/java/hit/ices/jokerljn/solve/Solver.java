package hit.ices.jokerljn.solve;

import com.rits.cloning.Cloner;
import hit.ices.jokerljn.cost.MssCost;
import hit.ices.jokerljn.importer.mss.MicroserviceSystem;
import hit.ices.jokerljn.importer.mss.MssAtomicChange;
import hit.ices.jokerljn.importer.uml.UmlModel;
import hit.ices.jokerljn.importer.umlchange.UmlAtomicChange;
import hit.ices.jokerljn.mapRule.OptionalMssWithChange;
import hit.ices.jokerljn.mapRule.UmlToMssMap;
import hit.ices.jokerljn.quality.QualityScore;
import hit.ices.jokerljn.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/23 10:49
 * @desc 0 1 规划问题求解器
 */
@Slf4j
public abstract class Solver {
    protected final UmlModel oldUmlModel;
    protected final UmlModel newUmlModel;
    protected final MicroserviceSystem oldMss;
    protected final Cloner cloner;
    protected final QualityScore qualityScore;
    protected final MssCost mssCost;
    protected final double cohesion;
    protected final double couping;
    protected final double score;
    protected final double decreasePercent;

    public Solver(UmlModel oldUmlModel, UmlModel newUmlModel, MicroserviceSystem oldMss, QualityScore qualityScore, double decreaseDegree) {
        this.oldUmlModel = oldUmlModel;
        this.newUmlModel = newUmlModel;
        this.oldMss = oldMss;
        this.decreasePercent = decreaseDegree;
        this.cloner = new Cloner();
        this.mssCost = new MssCost();
        this.qualityScore = qualityScore;
        this.score = qualityScore.getQualityScoreByUml(oldMss.getMicroservices(), oldUmlModel.getClassDiagram());
        this.cohesion = qualityScore.getCohesionDegree(oldMss.getMicroservices());
        this.couping = qualityScore.getCoupingDegree(oldMss.getMicroservices(), oldUmlModel.getClassDiagram());
    }

    /**
     * find best solution
     */
    public abstract OptionalMssWithChange solve(List<UmlAtomicChange> umlAtomicChanges, boolean simpleRule);

    /**
     * 检查是否符合约束条件，如符合生成要写入文件的字符串
     */
    public boolean checkConstraint(OptionalMssWithChange result) {
        double resultScore = qualityScore.getQualityScoreByMss(
                result.getMss().getMicroservices()
        );
        result.setQualityScore(resultScore);
        double tempDecrease = (score - resultScore) / score;

        return tempDecrease <= decreasePercent;
    }

    protected String getQualityScoreString(OptionalMssWithChange result) {

        double newCohesion = qualityScore.getCohesionDegree(
                result.getMss().getMicroservices()
        );
        double newCouping = qualityScore.getCoupingDegree(
                result.getMss().getMicroservices(), newUmlModel.getClassDiagram()
        );
        double resultScore = qualityScore.getQualityScoreByUml(
                result.getMss().getMicroservices(), newUmlModel.getClassDiagram()
        );

        double tempDecrease = (score - resultScore) / score;

        StringBuilder data = new StringBuilder();
        data.append(getMssChangesString(result.getMssAtomicChangeList()));
        data.append(result.getMss());
        data.append("---------------------------------------------\n\n");
        data.append("oldQualityScore = ").append(score).append(", cohesion = ").append(cohesion).append(", couping = ").append(couping).
                append("\nnewQualityScore = ").append(resultScore).append(", cohesion = ").append(newCohesion).append(", couping = ").append(newCouping).
                append("\ndecreasePercent = ").append(tempDecrease).append("\n\n");
        return data.toString();
    }

    /**
     * 生成演化方案的字符串
     */
    protected String getMssChangesString(List<MssAtomicChange> mssAtomicChangeList) {
        StringBuilder data = new StringBuilder();
        data.append("演化方案为：\n");
        mssAtomicChangeList.forEach(data::append);
        data.append("-----------------------------------------------------\n\n");
        return data.toString();
    }

    /**
     * 生成 uml 原子变化 到 MSS 原子变化的 映射 字符串
     */
    protected String getUmlToMssMapString(List<UmlToMssMap> mapList) {
        StringBuilder builder = new StringBuilder();
        builder.append("uml 原子变化到 mss 原子变化的映射：\n");
        mapList.forEach(umlToMssMap -> {
            builder.append(umlToMssMap.getUmlChange()).append("\n");
            umlToMssMap.getMssAtomicChanges().forEach(builder::append);
            builder.append("--------------\n");
        });
        builder.append("----------------------------------------\n\n");
        return builder.toString();
    }

    public void writeBestSolutionToFile(OptionalMssWithChange bestOption, String path, String fileName, double costTime) {
        StringBuilder writeData = new StringBuilder();
        // write uml change and map to mss change
        writeData.append(getUmlToMssMapString(bestOption.getUmlToMssMaps()));

        String bestData = getQualityScoreString(bestOption);
        // write change cost
        writeData.append(bestData).append("mssChangeCost = ").append(bestOption.getCost()).
                append("\n---------------------------------------------------\n").
                append("共用时 ").append(costTime).append(" min\n");
        FileUtils.writeAppendToFile(path, writeData.toString(), fileName);
        log.info("成功找到最优的演化方案!");
    }
}
