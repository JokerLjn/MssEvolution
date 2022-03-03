package hit.ices.jokerljn.importer.umlchange;

import hit.ices.jokerljn.importer.uml.classmodel.UmlAttribute;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/7 11:20
 * @desc attribute change
 */
@Getter
@Setter
@NoArgsConstructor
public class AttributeChange extends UmlAtomicChange {
    private String className;
    private UmlAttribute attribute;

    @Override
    public String toString() {
        return "AttributeChange{" + "optType=" + super.getType() + ", optObj=" + super.getObj() +
                ", className='" + className + '\'' +
                ", attribute=" + attribute +
                "} ";
    }
}
