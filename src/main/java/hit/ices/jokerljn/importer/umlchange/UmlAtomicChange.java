package hit.ices.jokerljn.importer.umlchange;

import lombok.*;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/6 16:36
 * @desc
 */

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UmlAtomicChange {

    public enum OptType {
        // opt type
        ADD,
        DELETE,
        UPDATE,
        COMBINE,
        DECOMPOSE
    }

    public enum OptObj {
        // opt object
        CLASS,
        ATTRIBUTE,
        METHOD,
        RELATION,
        OBJECT,
        MESSAGE
    }

    private OptType type;
    private OptObj obj;
}
