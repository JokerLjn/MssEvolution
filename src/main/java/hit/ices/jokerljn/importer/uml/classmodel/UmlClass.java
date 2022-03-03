package hit.ices.jokerljn.importer.uml.classmodel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 20:40
 * @desc class entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UmlClass {
    private String name;
    private ClassType type;
    private List<UmlAttribute> attributes;
    private List<UmlMethod> methods;

    public enum ClassType {
        // type
        Entity,
        Control
    }

    public UmlClass(final String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UmlClass) {
            return this.name.equals(((UmlClass) obj).getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
