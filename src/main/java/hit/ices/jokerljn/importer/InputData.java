package hit.ices.jokerljn.importer;

import hit.ices.jokerljn.importer.mss.MicroserviceSystem;
import hit.ices.jokerljn.importer.uml.UmlModel;
import hit.ices.jokerljn.importer.umlchange.UmlAtomicChange;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/8 14:42
 * @desc all input data
 */
@Data
@NoArgsConstructor
public class InputData {
    /**
     * 旧 UML 模型
     */
    private UmlModel oldUmlModel;
    /**
     * 新 UML 模型
     */
    private UmlModel newUmlModel;
    /**
     * 旧 微服务系统
     */
    private MicroserviceSystem oldMss;
    /**
     * UML 原子变化
     */
    private List<UmlAtomicChange> umlChangeList;
}
