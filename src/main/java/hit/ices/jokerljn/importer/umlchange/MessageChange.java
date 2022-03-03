package hit.ices.jokerljn.importer.umlchange;

import hit.ices.jokerljn.importer.uml.sequence.MethodCall;
import lombok.Getter;
import lombok.Setter;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/9 15:16
 * @desc sequence message change
 */
@Getter
@Setter
public class MessageChange extends UmlAtomicChange {
    private String sequenceName;
    private MethodCall methodCall;

    @Override
    public String toString() {
        return "MessageChange{" + "optType=" + super.getType() + ", optObj=" + super.getObj() +
                ", sequenceName='" + sequenceName + '\'' +
                ", methodCall=" + methodCall +
                "} ";
    }
}
