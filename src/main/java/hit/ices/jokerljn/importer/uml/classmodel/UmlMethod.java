package hit.ices.jokerljn.importer.uml.classmodel;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 20:46
 * @desc
 */
@Data
@NoArgsConstructor
public class UmlMethod {
    /**
     * public / protected / private
     */
    private String name;
    private List<UmlAttribute> params;
    private String returnType;
    private String accessRight;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof UmlMethod) {
            return name.equals(((UmlMethod) obj).getName());
        }
        return false;
    }
}
