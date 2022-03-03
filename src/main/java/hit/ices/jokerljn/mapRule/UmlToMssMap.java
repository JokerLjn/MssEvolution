package hit.ices.jokerljn.mapRule;

import hit.ices.jokerljn.importer.mss.MssAtomicChange;
import hit.ices.jokerljn.importer.umlchange.UmlAtomicChange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/24 11:25
 * @desc
 */
@Getter
@Setter
@AllArgsConstructor
public class UmlToMssMap {
    UmlAtomicChange umlChange;
    List<MssAtomicChange> mssAtomicChanges;
}
