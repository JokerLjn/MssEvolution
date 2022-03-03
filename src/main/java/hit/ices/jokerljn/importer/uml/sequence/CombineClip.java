package hit.ices.jokerljn.importer.uml.sequence;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 21:14
 * @desc 组合片段
 */
@Data
@NoArgsConstructor
public class CombineClip {
    private String name;
    private String type;
    private List<Integer> methodNumList;
}
