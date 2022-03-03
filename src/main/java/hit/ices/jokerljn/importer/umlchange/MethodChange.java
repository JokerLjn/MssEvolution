package hit.ices.jokerljn.importer.umlchange;

import hit.ices.jokerljn.importer.uml.classmodel.UmlMethod;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/9 15:03
 * @desc method change
 */
@Getter
@Setter
@NoArgsConstructor
public class MethodChange extends UmlAtomicChange {
    private String className;
    private UmlMethod method;

    @Override
    public String toString() {
        return "MethodChange{" + "optType=" + super.getType() + ", optObj=" + super.getObj() +
                ", className='" + className + '\'' +
                ", method=" + method +
                "} ";
    }
}
