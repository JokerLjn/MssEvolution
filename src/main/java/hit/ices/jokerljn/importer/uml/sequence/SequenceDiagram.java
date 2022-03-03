package hit.ices.jokerljn.importer.uml.sequence;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 21:12
 * @desc
 */
@Data
@NoArgsConstructor
public class SequenceDiagram {
    private String name;
    private List<String> objects;
    private List<MethodCall> methodCalls;
    private List<CombineClip> combineClips;
}
