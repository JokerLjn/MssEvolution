package hit.ices.jokerljn.groupAndSort;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/12/20 14:30
 * @desc uml change group sort rule
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SortRule {
    private UmlChangeGroup before;
    private UmlChangeGroup after;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SortRule) {
            SortRule rule = (SortRule) obj;
            return before == rule.getBefore() && after == rule.getAfter();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return before.hashCode() + after.hashCode();
    }
}
