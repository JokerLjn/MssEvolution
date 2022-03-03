package hit.ices.jokerljn.importer.uml.classmodel;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 20:39
 * @desc class entity
 */
@Data
@NoArgsConstructor
public class ClassDiagram {
    private String name;
    private List<UmlClass> classList;
    private List<ClassRelation> classRelationList;

    public UmlClass getUmlClassByName(String name) {
        for (UmlClass umlClass : classList) {
            if (umlClass.getName().equals(name)) {
                return umlClass;
            }
        }
        return null;
    }
}
