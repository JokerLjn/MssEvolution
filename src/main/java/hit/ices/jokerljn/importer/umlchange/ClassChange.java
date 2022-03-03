package hit.ices.jokerljn.importer.umlchange;

import hit.ices.jokerljn.importer.uml.classmodel.UmlClass;
import lombok.*;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/7 11:02
 * @desc
 */
@Getter
@Setter
@NoArgsConstructor
public class ClassChange extends UmlAtomicChange {
    private String className;
    private UmlClass.ClassType classType;

    @Override
    public String toString() {
        return "ClassChange{" + "optType=" + super.getType() + ", optObj=" + super.getObj() +
                ", className='" + className + '\'' +
                ", classType=" + classType +
                "} ";
    }
}
