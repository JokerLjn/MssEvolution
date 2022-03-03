package hit.ices.jokerljn.importer.uml.sequence;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 21:11
 * @desc
 */
@Data
@NoArgsConstructor
public class MethodCall {
    private int num;
    private MessageType type;
    private String from;
    private String to;
    private String method;

    public enum MessageType {
        SIMPLE,
        REPLY
    }
}
