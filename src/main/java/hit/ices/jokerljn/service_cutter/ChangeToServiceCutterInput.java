package hit.ices.jokerljn.service_cutter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hit.ices.jokerljn.importer.uml.classmodel.ClassDiagram;
import hit.ices.jokerljn.importer.uml.classmodel.ClassRelation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/12/23 14:37
 * @desc
 */
public class ChangeToServiceCutterInput {

    /**
     * change classDiagram to json which service cutter accepted
     *
     * @param classDiagram
     * @return
     */
    public static String getServiceCutterInput(ClassDiagram classDiagram) {
        JSONObject json = new JSONObject();
        json.put("name", classDiagram.getName());

        // handle class list
        JSONArray classArray = new JSONArray();
        classDiagram.getClassList().forEach(umlClass -> {
            JSONObject classJson = new JSONObject();
            // handle class name
            classJson.put("name", umlClass.getName());
            // handle class attributes
            JSONArray attributes = new JSONArray();
            umlClass.getAttributes().forEach(umlAttribute -> attributes.add(umlAttribute.getName()));

            if (attributes.size() == 0) {
                attributes.add(umlClass.getName());
            }
            classJson.put("nanoentities", attributes);
            // add to classArray
            classArray.add(classJson);
        });
        json.put("entities", classArray);

        // handle class relation
        JSONArray relations = new JSONArray();
        classDiagram.getClassRelationList().forEach(classRelation -> {
            JSONObject relationJson = new JSONObject();
            relationJson.put("origin", classRelation.getOrigin().getName());
            relationJson.put("destination", classRelation.getDestination().getName());
            String relationType = changeRelationName(classRelation.getType());
            if (relationType != null) {
                relationJson.put("type", relationType);
                relations.add(relationJson);
            }
        });
        json.put("relations", relations);

        return json.toJSONString();
    }

    private static String changeRelationName(ClassRelation.RelationType relationType) {
        switch (relationType) {
            case ClassAggregate:
                return "AGGREGATION";
            case ClassAssociation:
            case ClassComposition:
                return "COMPOSITION";
            case ClassInheritance:
                return "INHERITANCE";
            default:
                return null;
        }
    }

}
