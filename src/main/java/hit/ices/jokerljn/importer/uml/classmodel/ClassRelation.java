package hit.ices.jokerljn.importer.uml.classmodel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 21:07
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassRelation {
    private UmlClass origin;
    private UmlClass destination;
    private RelationType type;

    public enum RelationType {
        // class relation type
        ClassAssociation,
        ClassAggregate,
        ClassComposition,
        ClassDependency,
        ClassInheritance,
        ClassRealization
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ClassRelation) {
            ClassRelation classRelation = (ClassRelation) obj;
            return origin.equals(classRelation.getOrigin()) &&
                    destination.equals(classRelation.getDestination()) &&
                    type.equals(classRelation.getType());
        }
        return false;
    }
}
