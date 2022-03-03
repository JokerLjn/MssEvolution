package hit.ices.jokerljn.mapRule;

import hit.ices.jokerljn.importer.uml.UmlModel;
import hit.ices.jokerljn.importer.uml.classmodel.UmlAttribute;
import hit.ices.jokerljn.importer.uml.classmodel.UmlClass;
import hit.ices.jokerljn.importer.uml.sequence.SequenceDiagram;
import hit.ices.jokerljn.importer.umlchange.*;

import java.util.ArrayList;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/19 14:40
 * @desc 根据 uml 原子变化更新 UML model
 */
public class UpdateUmlModel {

    public static void updateUml(UmlModel umlModel, UmlAtomicChange change) {
        switch (change.getObj()) {
            case CLASS:
                updateClassChange(umlModel, (ClassChange) change);
                break;
            case ATTRIBUTE:
                updateAttributeChange(umlModel, (AttributeChange) change);
                break;
            case METHOD:
                updateMethodChange(umlModel, (MethodChange) change);
                break;
            case RELATION:
                updateRelationChange(umlModel, (RelationShipChange) change);
                break;
            case OBJECT:
                updateObjectChange(umlModel, (ObjectChange) change);
                break;
            case MESSAGE:
                updateMessageChange(umlModel, (MessageChange) change);
                break;
            default:
        }
    }

    private static void updateClassChange(UmlModel umlModel, ClassChange change) {
        if (change.getType() == UmlAtomicChange.OptType.ADD) {
            UmlClass temp = new UmlClass(
                    change.getClassName(),
                    change.getClassType(),
                    new ArrayList<>(),
                    new ArrayList<>()
            );
            umlModel.getClassDiagram().getClassList().add(temp);
        } else if (change.getType() == UmlAtomicChange.OptType.DELETE) {
            // delete class
            umlModel.getClassDiagram().getClassList().removeIf(umlClass ->
                    umlClass.getName().equals(change.getClassName()));
            // delete relation
            umlModel.getClassDiagram().getClassRelationList().removeIf(classRelation ->
                    classRelation.getOrigin().getName().equals(change.getClassName()) ||
                            classRelation.getDestination().getName().equals(change.getClassName()));
        }
    }

    private static void updateAttributeChange(UmlModel umlModel, AttributeChange change) {
        UmlClass temp = findClassByName(umlModel, change.getClassName());

        if (temp == null) {
            throw new IllegalArgumentException("当前属性变化找不到所属类" + change.getClassName());
        }

        if (change.getType() == UmlAtomicChange.OptType.ADD) {

            if (temp.getAttributes() == null) {
                temp.setAttributes(new ArrayList<>());
            }
            temp.getAttributes().add(change.getAttribute());
        } else if (change.getType() == UmlAtomicChange.OptType.UPDATE) {
            temp.getAttributes().removeIf(umlAttribute -> umlAttribute.equals(change.getAttribute()));
            temp.getAttributes().add(change.getAttribute());
        } else {
            temp.getAttributes().removeIf(umlAttribute -> umlAttribute.equals(change.getAttribute()));
        }
    }

    private static void updateMethodChange(UmlModel umlModel, MethodChange change) {
        UmlClass temp = findClassByName(umlModel, change.getClassName());

        if (temp == null) {
            throw new IllegalArgumentException("当前属性变化找不到所属类" + change.getClassName());
        }

        if (change.getType() == UmlAtomicChange.OptType.ADD) {
            if (temp.getMethods() == null) {
                temp.setMethods(new ArrayList<>());
            }
            temp.getMethods().add(change.getMethod());
        } else if (change.getType() == UmlAtomicChange.OptType.UPDATE) {
            temp.getMethods().removeIf(umlMethod -> umlMethod.equals(change.getMethod()));
            temp.getMethods().add(change.getMethod());
        } else {
            temp.getMethods().removeIf(umlMethod -> umlMethod.equals(change.getMethod()));
        }
    }

    private static void updateRelationChange(UmlModel umlModel, RelationShipChange change) {
        if (change.getType() == UmlAtomicChange.OptType.ADD) {
            umlModel.getClassDiagram().getClassRelationList().add(change.getRelation());
        } else if (change.getType() == UmlAtomicChange.OptType.UPDATE) {
            umlModel.getClassDiagram().getClassRelationList().removeIf(classRelation ->
                    classRelation.equals(change.getRelation()));
            umlModel.getClassDiagram().getClassRelationList().add(change.getRelation());
        } else {
            umlModel.getClassDiagram().getClassRelationList().removeIf(classRelation ->
                    classRelation.equals(change.getRelation()));
        }
    }

    private static void updateObjectChange(UmlModel umlModel, ObjectChange change) {

        SequenceDiagram sequenceDiagram = null;
        for (SequenceDiagram diagram : umlModel.getSequenceDiagramList()) {
            if (diagram.getName().equals(change.getSequenceName())) {
                sequenceDiagram = diagram;
                break;
            }
        }

        if (sequenceDiagram == null) {
            throw new IllegalArgumentException("找不到object变化所在的时序图 " + change.getSequenceName());
        }

        if (change.getType() == UmlAtomicChange.OptType.ADD) {
            if (sequenceDiagram.getObjects() == null) {
                sequenceDiagram.setObjects(new ArrayList<>());
            }
            sequenceDiagram.getObjects().add(change.getClassName());
        } else if (change.getType() == UmlAtomicChange.OptType.DELETE) {
            sequenceDiagram.getObjects().removeIf(s -> s.equals(change.getClassName()));
        }
    }

    private static void updateMessageChange(UmlModel umlModel, MessageChange change) {
        SequenceDiagram sequenceDiagram = null;
        for (SequenceDiagram diagram : umlModel.getSequenceDiagramList()) {
            if (diagram.getName().equals(change.getSequenceName())) {
                sequenceDiagram = diagram;
                break;
            }
        }

        if (sequenceDiagram == null) {
            throw new IllegalArgumentException("找不到message变化所在的时序图 " + change.getSequenceName());
        }

        if (change.getType() == UmlAtomicChange.OptType.ADD) {
            if (sequenceDiagram.getMethodCalls() == null) {
                sequenceDiagram.setMethodCalls(new ArrayList<>());
            }
            sequenceDiagram.getMethodCalls().add(change.getMethodCall());
        } else if (change.getType() == UmlAtomicChange.OptType.DELETE) {
            sequenceDiagram.getMethodCalls().removeIf(methodCall ->
                methodCall.equals(change.getMethodCall()));
        }
    }

    private static UmlClass findClassByName(UmlModel umlModel, String className) {
        for (UmlClass x : umlModel.getClassDiagram().getClassList()) {
            if (x.getName().equals(className)) {
                return x;
            }
        }
        return null;
    }

}
