package hit.ices.jokerljn.importer.uml;

import hit.ices.jokerljn.importer.uml.classmodel.ClassDiagram;
import hit.ices.jokerljn.importer.uml.sequence.SequenceDiagram;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 20:37
 * @desc uml model entity
 */
@Data
@NoArgsConstructor
public class UmlModel {
    private ClassDiagram classDiagram;
    private List<SequenceDiagram> sequenceDiagramList;
}
