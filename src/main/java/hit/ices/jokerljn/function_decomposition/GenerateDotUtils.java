package hit.ices.jokerljn.function_decomposition;

import hit.ices.jokerljn.importer.ImportObject;
import hit.ices.jokerljn.importer.uml.classmodel.ClassDiagram;
import hit.ices.jokerljn.importer.uml.classmodel.ClassRelation;
import hit.ices.jokerljn.importer.uml.classmodel.UmlClass;
import hit.ices.jokerljn.utils.FileUtils;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/12/25 16:14
 * @desc generate dot file from class json
 */
public class GenerateDotUtils {
    private final static String CASE1_INPUT_PATH = "service_cutter/case1/";
    private final static String CASE2_INPUT_PATH = "service_cutter/case2/";
    private final static String CASE1_OUTPUT_PATH = "src/main/resources/function_decompose/case1/";
    private final static String CASE2_OUTPUT_PATH = "src/main/resources/function_decompose/case2/";

    public static void generateDotFile(int caseNum, int choice) throws IOException, URISyntaxException {
        String inputPath = "";
        String outPath = "";
        String fileName = "class" + choice;
        if (caseNum == 1) {
            inputPath = CASE1_INPUT_PATH;
            outPath = CASE1_OUTPUT_PATH;
        } else {
            inputPath = CASE2_INPUT_PATH;
            outPath = CASE2_OUTPUT_PATH;
        }
        inputPath += fileName + ".json";

        // get class diagram data
        ClassDiagram classDiagram = ImportObject.importClassDiagram(inputPath);
        // generate dot data
        String dotData = generateDotData(classDiagram, fileName);
        // write to file
        FileUtils.writeToFile(outPath, dotData, fileName + ".dot");
    }

    private static String generateDotData(ClassDiagram classDiagram, String fileName) {
        StringBuilder data = new StringBuilder();
        data.append("graph ").append(fileName).append(" {\n");

        // generate body data
        data.append(generateBody()).append("\n");
        // generate node data
        data.append(generateNode(classDiagram)).append("\n");
        // generate edge data
        data.append(generateEdge(classDiagram)).append("\n");

        data.append("}");
        return data.toString();
    }

    /**
     * generate body data
     */
    private static String generateBody() {
        return "fontname=\"Microsoft YaHei\";\n" +
                "\tedge [fontname=\"Microsoft YaHei\", len=3];\n" +
                "\tnode [fontname=\"Microsoft YaHei\"];\n";
    }

    private static String generateNode(ClassDiagram classDiagram) {
        StringBuilder nodeData = new StringBuilder();

        for (UmlClass umlClass : classDiagram.getClassList()) {
            StringBuilder temp = new StringBuilder();
            temp.append("\t").append(umlClass.getName()).append(" [label=\"").
                    append(umlClass.getName()).append("\"];\n");
            nodeData.append(temp);
        }

        return nodeData.toString();
    }

    private static String generateEdge(ClassDiagram classDiagram) {
        StringBuilder edgeData = new StringBuilder();

        for (ClassRelation relation : classDiagram.getClassRelationList()) {
            StringBuilder temp = new StringBuilder();
            temp.append("\t").append(relation.getOrigin().getName()).
                    append(" -- ").append(relation.getDestination().getName());
            if (relation.getType() == ClassRelation.RelationType.ClassInheritance ||
            relation.getType() == ClassRelation.RelationType.ClassComposition ||
                    relation.getType() == ClassRelation.RelationType.ClassAggregate) {
                temp.append(" [weight=2, label=\"2\"];\n");
            } else {
                temp.append(";\n");
            }
            edgeData.append(temp);
        }

        return edgeData.toString();
    }

}
