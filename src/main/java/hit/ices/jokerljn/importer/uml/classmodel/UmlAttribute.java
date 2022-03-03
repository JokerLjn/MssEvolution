package hit.ices.jokerljn.importer.uml.classmodel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 20:42
 * @desc attribute entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UmlAttribute {
    /**
     * int / double / Object / ...
     */
    private String name;
    private String type;
    /**
     * public / protected / private
     */
    private String accessRight;

    public UmlAttribute(String name, String type) {
        this.name  = name;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof UmlAttribute) {
            return ((UmlAttribute) obj).getName().equals(name);
        }
        return false;
    }
}
