package hit.ices.jokerljn.importer.mss;
import hit.ices.jokerljn.importer.uml.classmodel.ClassRelation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/14 10:11
 * @desc
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MssAtomicChange {
    private OptType type;
    private OptObj obj;
    private String msName;
    private Map<String, Object> valueMap;

    public enum OptType {
        // mss opt type
        ADD,
        UPDATE,
        DELETE,
        MOVE
    }

    public enum OptObj {
        // mss opt obj
        MICROSERVICE,
        INTERFACE,
        INTERFACE_CALL,
        CLASS,
        ATTRIBUTE,
        METHOD,
        RELATION
    }

    public MssAtomicChange(OptType type, OptObj obj, String msName) {
        this.type = type;
        this.obj = obj;
        this.msName = msName;
    }

    @Override
    public String toString() {
        StringBuilder data = new StringBuilder();
        data.append("MssAtomicChange{" + "type=").append(type).append(", obj=").append(obj).append(", msName='").append(msName).append('\'').
                append(",\n        valueMap=").append(valueMap).append("\n");
//        valueMap.forEach((k, v) -> {
//            switch (k) {
//                case "relation":
//                    data.append(k).append(" = ").append().append("\n");
//                    break;
//                default:
//                    data.append(k).append(" = ").append(v).append("\n");
//            }
//        });
        return data.toString();
    }
}
