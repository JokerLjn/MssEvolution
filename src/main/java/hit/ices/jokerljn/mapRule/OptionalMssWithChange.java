package hit.ices.jokerljn.mapRule;

import hit.ices.jokerljn.importer.mss.MicroserviceSystem;
import hit.ices.jokerljn.importer.mss.MssAtomicChange;
import hit.ices.jokerljn.importer.umlchange.UmlAtomicChange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/19 16:57
 * @desc 记录当前 MSS（微服务系统）状态及产生的微服务变化列表
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptionalMssWithChange {
    private MicroserviceSystem mss;
    private List<MssAtomicChange> mssAtomicChangeList;
    private List<UmlToMssMap> umlToMssMaps;
    private double cost;
    private double qualityScore;

    public OptionalMssWithChange(MicroserviceSystem mss, List<MssAtomicChange> mssAtomicChangeList) {
        this.mss = mss;
        this.mssAtomicChangeList = mssAtomicChangeList;
        this.umlToMssMaps = new ArrayList<>();
    }
}
