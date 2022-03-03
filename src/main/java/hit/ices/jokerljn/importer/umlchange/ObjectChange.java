package hit.ices.jokerljn.importer.umlchange;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/9 15:15
 * @desc sequence object change
 */
@Getter
@Setter
@NoArgsConstructor
public class ObjectChange extends UmlAtomicChange {
    private String sequenceName;
    private String className;

    @Override
    public String toString() {
        return "ObjectChange{" + "optType=" + super.getType() + ", optObj=" + super.getObj() +
                ", sequenceName='" + sequenceName + '\'' +
                ", className='" + className + '\'' +
                "} ";
    }
}
