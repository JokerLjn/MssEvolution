package hit.ices.jokerljn.groupAndSort;

import hit.ices.jokerljn.importer.umlchange.UmlAtomicChange;
import lombok.*;

import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/11 15:23
 * @desc uml atomic change group
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UmlChangeGroup {
    private String className;
    private UmlAtomicChange.OptType changeType;
    private UmlAtomicChange.OptObj changeObject;
    private List<UmlAtomicChange> changeList;
    private List<List<UmlAtomicChange>> allSortedChangeList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("className = ").append(className).
                append("\nchangeType = ").append(changeType).
                append("\nchangeObj = ").append(changeObject).
                append("\nchangeList = ").append(changeList).append("\n---------------\n");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof UmlChangeGroup) {
            UmlChangeGroup temp = (UmlChangeGroup) obj;
            if (temp.getClassName().equals(className) && temp.getChangeType() == changeType && temp.getChangeObject() == changeObject) {
                return true;
            }
        }
        return false;
    }

    public UmlChangeGroup(String className, UmlAtomicChange.OptType changeType, UmlAtomicChange.OptObj changeObject, List<UmlAtomicChange> changeList) {
        this.className = className;
        this.changeType = changeType;
        this.changeObject = changeObject;
        this.changeList = changeList;
    }
}
