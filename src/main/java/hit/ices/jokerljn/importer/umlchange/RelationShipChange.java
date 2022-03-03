package hit.ices.jokerljn.importer.umlchange;

import hit.ices.jokerljn.importer.uml.classmodel.ClassRelation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/9 15:08
 * @desc uml class relation change entity
 */
@Getter
@Setter
@NoArgsConstructor
public class RelationShipChange extends UmlAtomicChange {
    private ClassRelation relation;

    @Override
    public String toString() {
        return "RelationShipChange{" + "optType=" + super.getType() + ", optObj=" + super.getObj() +
                ", relation=" + relation +
                "}" ;
    }
}
