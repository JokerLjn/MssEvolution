package hit.ices.jokerljn.groupAndSort;

import hit.ices.jokerljn.MainApplication;
import hit.ices.jokerljn.importer.uml.sequence.MethodCall;
import hit.ices.jokerljn.importer.umlchange.*;

import java.util.*;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/11 11:09
 * @desc group for uml changes
 */
public class GroupUtils {

    /**
     * 对一组 uml 原子变化进行分组
     */
    public static List<UmlChangeGroup> group(List<UmlAtomicChange> changeList) {
        List<UmlChangeGroup> changeGroups = new ArrayList<>();
        // 预处理
        preHandle(changeList, changeGroups);

        for (UmlAtomicChange change : changeList) {
            // 过滤掉类变化
            if (change.getObj() != UmlAtomicChange.OptObj.CLASS) {
                addChangeToGroup(change, changeGroups);
            }
        }
        return changeGroups;
    }

    /**
     * 预处理，得到所有类层次的变化，并以此构建基本分组
     *
     */
    private static void preHandle(List<UmlAtomicChange> changeList, List<UmlChangeGroup> groups) {
        for (UmlAtomicChange change : changeList) {
            if (change.getObj() == UmlAtomicChange.OptObj.CLASS) {
                ClassChange temp = (ClassChange) change;
                UmlChangeGroup group = new UmlChangeGroup(
                        temp.getClassName(),
                        temp.getType(),
                        temp.getObj(),
                        new ArrayList<>(Collections.singleton(temp))
                );
                groups.add(group);
            }
        }
    }

    /**
     * 将某个变化加入到 现有分组 或 新建分组 中
     */
    private static void addChangeToGroup(UmlAtomicChange change, List<UmlChangeGroup> changeGroups) {
        switch (change.getObj()) {
            case ATTRIBUTE:
                AttributeChange attributeChange = (AttributeChange) change;
                if (!addToGroup(attributeChange, attributeChange.getClassName(), attributeChange.getType(), changeGroups)) {
                    changeGroups.add(new UmlChangeGroup(
                       attributeChange.getClassName(),
                       attributeChange.getType(),
                       attributeChange.getObj(),
                       new ArrayList<>(Collections.singleton(attributeChange))
                    ));
                }
                break;
            case METHOD:
                MethodChange methodChange = (MethodChange) change;
                if (!addToGroup(methodChange, methodChange.getClassName(), methodChange.getType(), changeGroups)) {
                    changeGroups.add(new UmlChangeGroup(
                            methodChange.getClassName(),
                            methodChange.getType(),
                            methodChange.getObj(),
                            new ArrayList<>(Collections.singleton(methodChange))
                    ));
                }
                break;
            case RELATION:
                RelationShipChange relationShipChange = (RelationShipChange) change;
                if (!addToGroup(relationShipChange, relationShipChange.getRelation().getOrigin().getName(), relationShipChange.getType(), changeGroups)) {
                    changeGroups.add(new UmlChangeGroup(
                            relationShipChange.getRelation().getOrigin().getName(),
                            relationShipChange.getType(),
                            relationShipChange.getObj(),
                            new ArrayList<>(Collections.singleton(relationShipChange))
                    ));
                }
                break;
            case OBJECT:
                ObjectChange objectChange = (ObjectChange) change;
                if (!addToGroup(objectChange, objectChange.getClassName(), objectChange.getType(), changeGroups)) {
                    changeGroups.add(new UmlChangeGroup(
                            objectChange.getClassName(),
                            objectChange.getType(),
                            objectChange.getObj(),
                            new ArrayList<>(Collections.singleton(objectChange))
                    ));
                }
                break;
            case MESSAGE:
                // message 的变化应该与 它对应的 方法的变化放到一起
                MessageChange messageChange = (MessageChange) change;
                if (messageChange.getMethodCall().getType() != MethodCall.MessageType.REPLY) {
                    if (messageChange.getType() == UmlAtomicChange.OptType.DELETE) {
                        if (!addToGroup(messageChange, messageChange.getMethodCall().getTo(), messageChange.getType(), changeGroups)) {
                            changeGroups.add(new UmlChangeGroup(
                                    messageChange.getMethodCall().getTo(),
                                    messageChange.getType(),
                                    messageChange.getObj(),
                                    new ArrayList<>(Collections.singleton(messageChange))
                            ));
                        }
                    } else {
                        if (!addToGroup(messageChange, messageChange.getMethodCall().getFrom(), messageChange.getType(), changeGroups)) {
                            changeGroups.add(new UmlChangeGroup(
                                    messageChange.getMethodCall().getFrom(),
                                    messageChange.getType(),
                                    messageChange.getObj(),
                                    new ArrayList<>(Collections.singleton(messageChange))
                            ));
                        }
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("输入uml原子变化错误，找不到匹配类型！");
        }
    }

    /**
     * 根据变化的类名确定是否应该放到某个变化分组中
     *
     */
    private static boolean addToGroup(UmlAtomicChange change, String name, UmlAtomicChange.OptType type, List<UmlChangeGroup> changeGroups) {
        boolean addSuccess = false;
        for (UmlChangeGroup group : changeGroups) {
            if (name.equals(group.getClassName()) && type == group.getChangeType()) {
                group.getChangeList().add(change);
                addSuccess = true;
                break;
            }
        }
        return addSuccess;
    }

    public static void main(String[] args) {
        List<UmlAtomicChange> changeList = MainApplication.importUmlChangeList("input/case1", 2);
        List<UmlChangeGroup> changeGroups = group(changeList);
        changeGroups.forEach(SortUtils::sortInGroup);
        Set<SortRule> groupSort = SortUtils.sortBetweenGroups(changeGroups, changeList);
        System.out.println(changeGroups);
    }

}
