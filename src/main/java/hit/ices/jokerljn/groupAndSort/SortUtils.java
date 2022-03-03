package hit.ices.jokerljn.groupAndSort;

import hit.ices.jokerljn.importer.uml.sequence.MethodCall;
import hit.ices.jokerljn.importer.umlchange.MessageChange;
import hit.ices.jokerljn.importer.umlchange.RelationShipChange;
import hit.ices.jokerljn.importer.umlchange.UmlAtomicChange;

import java.util.*;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/13 14:19
 * @desc sort for uml changes
 */
public class SortUtils {

    /**
     * sort for a group uml change
     * 类->对象->属性->方法-关系->消息
     * TODO 关系/消息的排序会对后续产生影响，所以需要进一步处理（可能会产生组合爆炸的情况）
     * 分组内排序，如果关系/消息变化有多个，要排序
     */
    public static void sortInGroup(UmlChangeGroup group) {
        List<List<UmlAtomicChange>> allSorted = new ArrayList<>();

        List<UmlAtomicChange> changeList = group.getChangeList();
        if (group.getChangeList().size() == 1) {
            group.setAllSortedChangeList(allSorted);
            return;
        }
        if (group.getChangeType() == UmlAtomicChange.OptType.DELETE) {
            changeList.sort(SortUtils::compareDeleteChange);
        } else {
            changeList.sort(SortUtils::compareAddChange);
        }

        List<UmlAtomicChange> temp = new ArrayList<>();

        List<RelationShipChange> relationChangeList = new ArrayList<>();
        List<List<RelationShipChange>> relationSortList = new ArrayList<>();
        List<MessageChange> messageChangeList = new ArrayList<>();
        List<List<MessageChange>> messageSortList = new ArrayList<>();

        // get all relation/message change
        for (UmlAtomicChange change : changeList) {
            if (change.getObj() == UmlAtomicChange.OptObj.RELATION) {
                relationChangeList.add((RelationShipChange) change);
            } else if (change.getObj() == UmlAtomicChange.OptObj.MESSAGE) {
                messageChangeList.add((MessageChange) change);
            } else {
                temp.add(change);
            }
        }

        // get all possible relation sort
        if (relationChangeList.size() >= 2) {
            List<List<Integer>> indexSort = getAllIndexSort(relationChangeList.size());
            for (List<Integer> sort : indexSort) {
                List<RelationShipChange> tempRelation = new ArrayList<>();
                sort.forEach(integer -> tempRelation.add(relationChangeList.get(integer)));
                relationSortList.add(tempRelation);
            }
        }

        // get all possible message sort
        if (messageChangeList.size() >= 2) {
            List<List<Integer>> indexSort = getAllIndexSort(messageChangeList.size());
            for (List<Integer> sort : indexSort) {
                List<MessageChange> tempMessage = new ArrayList<>();
                sort.forEach(integer -> tempMessage.add(messageChangeList.get(integer)));
                messageSortList.add(tempMessage);
            }
        }

        // get all possible sort combine relation with message
        for (List<RelationShipChange> relationShipChanges : relationSortList) {
            for (List<MessageChange> messageChanges : messageSortList) {
                List<UmlAtomicChange> tempCombineSort = new ArrayList<>(temp);
                tempCombineSort.addAll(relationShipChanges);
                tempCombineSort.addAll(messageChanges);
                allSorted.add(tempCombineSort);
            }
        }

        group.setAllSortedChangeList(allSorted);
    }

    /**
     * sort function between uml changes
     *
     */
    private static int compareAddChange(UmlAtomicChange change1, UmlAtomicChange change2) {
        return getChangeWeight(change1) - getChangeWeight(change2);
    }

    private static int compareDeleteChange(UmlAtomicChange change1, UmlAtomicChange change2) {
        return getChangeWeight(change2) - getChangeWeight(change1);
    }

    /**
     * set weight for each uml change
     */
    private static int getChangeWeight(UmlAtomicChange change) {
        switch (change.getObj()) {
            case CLASS:
                return 1;
            case OBJECT:
                return 2;
            case ATTRIBUTE:
                return 3;
            case METHOD:
                return 4;
            case RELATION:
                return 5;
            case MESSAGE:
                return 6;
            default:
                return 0;
        }
    }

    /**
     * sort for umlGroups
     * 通过关系与消息的 起点到终点的调用关系确定相对顺序
     * TODO 暂时不考虑 删除类型
     */
    public static Set<SortRule> sortBetweenGroups(List<UmlChangeGroup> groups, List<UmlAtomicChange> changeList) {
        Set<SortRule> sort = new HashSet<>();
        for (UmlAtomicChange change : changeList) {
            if (change.getObj() == UmlAtomicChange.OptObj.RELATION) {
                RelationShipChange temp = (RelationShipChange) change;
                String from = temp.getRelation().getOrigin().getName();
                String to = temp.getRelation().getDestination().getName();
                UmlChangeGroup groupFrom = getGroupByClassName(groups, from);
                UmlChangeGroup groupTo = getGroupByClassName(groups, to);
                if (groupFrom != null && groupTo != null) {
                    sort.add(new SortRule(groupTo, groupFrom));
                }
                UmlChangeGroup relationGroup = getGroupByChange(groups, temp);
                if (relationGroup == null) {
                    throw new RuntimeException("变化 " + temp + " 找不到所属分组 " + groups);
                }
                if (groupFrom != null && relationGroup != groupFrom && relationGroup != groupTo) {
                    if (temp.getType() == UmlAtomicChange.OptType.DELETE) {
                        sort.add(new SortRule(relationGroup, groupFrom));
                    } else {
                        sort.add(new SortRule(groupFrom, relationGroup));
                    }
                }
                if (groupTo != null && relationGroup != groupFrom && relationGroup != groupTo) {
                    if (temp.getType() == UmlAtomicChange.OptType.DELETE) {
                        sort.add(new SortRule(relationGroup, groupTo));
                    } else {
                        sort.add(new SortRule(groupTo, relationGroup));
                    }
                }
            } else if (change.getObj() == UmlAtomicChange.OptObj.MESSAGE) {
                MessageChange temp = (MessageChange) change;
                if (temp.getMethodCall().getType() == MethodCall.MessageType.SIMPLE) {
                    String from = temp.getMethodCall().getFrom();
                    String to = temp.getMethodCall().getTo();
                    UmlChangeGroup groupFrom = getGroupByClassName(groups, from);
                    UmlChangeGroup groupTo = getGroupByClassName(groups, to);
                    if (groupFrom != null && groupTo != null) {
                        sort.add(new SortRule(groupTo, groupFrom));
                    }
                    UmlChangeGroup relationGroup = getGroupByChange(groups, temp);
                    if (relationGroup == null) {
                        throw new RuntimeException("变化 " + temp + " 找不到所属分组 " + groups);
                    }
                    if (groupFrom != null && relationGroup != groupFrom && relationGroup != groupTo) {
                        if (temp.getType() == UmlAtomicChange.OptType.DELETE) {
                            sort.add(new SortRule(relationGroup, groupFrom));
                        } else {
                            sort.add(new SortRule(groupFrom, relationGroup));
                        }
                    }
                    if (groupTo != null && relationGroup != groupFrom && relationGroup != groupTo) {
                        if (temp.getType() == UmlAtomicChange.OptType.DELETE) {
                            sort.add(new SortRule(relationGroup, groupTo));
                        } else {
                            sort.add(new SortRule(groupTo, relationGroup));
                        }
                    }
                }
             }
        }
        return sort;
    }

    /**
     * find group by class name
     */
    private static UmlChangeGroup getGroupByClassName(List<UmlChangeGroup> groups, String className) {
        for (UmlChangeGroup group : groups) {
            if (group.getClassName().equals(className) && group.getChangeObject() == UmlAtomicChange.OptObj.CLASS) {
                return group;
            }
        }
        return null;
    }

    private static UmlChangeGroup getGroupByChange(List<UmlChangeGroup> groups, UmlAtomicChange change) {
        for (UmlChangeGroup group : groups) {
            for (UmlAtomicChange atomicChange : group.getChangeList()) {
                if (atomicChange == change) {
                    return group;
                }
            }
        }
        return null;
    }

    /**
     * 检查当前分组排序是否符合规则
     */
    private static boolean checkGroupSort(Set<SortRule> sortRules, List<UmlChangeGroup> groups) {
        if (sortRules == null || sortRules.size() == 0) {
            return true;
        }
        for (SortRule rule : sortRules) {
            if(groups.indexOf(rule.getBefore()) > groups.indexOf(rule.getAfter())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取所有可能排序的Uml分组间排序结果
     * TODO 不太可行，会出现组合爆炸的情况
     */
    private static List<List<UmlChangeGroup>> getAllSortedGroups(Set<SortRule> sortRules, List<UmlChangeGroup> groups) {
        List<List<UmlChangeGroup>> allSortedGroups = new ArrayList<>();

        List<List<Integer>> indexSort = getAllIndexSort(groups.size());

        for (List<Integer> indexList : indexSort) {
            List<UmlChangeGroup> temp = new ArrayList<>();
            for (int x : indexList) {
                temp.add(groups.get(x));
            }
            if (checkGroupSort(sortRules, temp)) {
                allSortedGroups.add(temp);
            }
        }
        return allSortedGroups;
    }

    /**
     * TODO 选取适当的规则，比较两个UML分组排序，判断是否重复
    */
    public boolean checkSortedGroupsRepeat(List<UmlChangeGroup> groups1, List<UmlChangeGroup> groups2) {
        return false;
    }

    /**
     * 获得 0-size 的全排列
     */
    private static List<List<Integer>> getAllIndexSort(int size) {
        if (size > 10) {
            throw new RuntimeException("要排序的分组过多，会造成堆溢出问题： " + size);
        }
        int[] nums = new int[size];
        for (int i=0; i<size; i++) {
            nums[i] = i;
        }

        List<List<Integer>> indexSort = new ArrayList<>();
        List<Integer> tempIndex = new ArrayList<>();
        loop(indexSort, nums, tempIndex);

        return indexSort;
    }

    /**
     * 回溯获得 0-size 的一个排列，并加到集合 indexSort 中
     */
    private static void loop(List<List<Integer>> indexSort, int[] nums, List<Integer> temp) {
        if (temp.size() == nums.length) {
            indexSort.add(new ArrayList<>(temp));
            return;
        }
        for (int i=0; i<nums.length; i++) {
            int tempNum = nums[i];
            if (tempNum != -1) {
                temp.add(tempNum);
                nums[i] = -1;
                loop(indexSort, nums, temp);

                // 回溯
                temp.remove(temp.size() - 1);
                nums[i] = tempNum;
            }
        }
    }

    /**
     * 遍历每个group，判断是否不包含类、关系、消息变化，如果是，则无需排序
     */
    private static List<UmlChangeGroup>[] classifyGroup(List<UmlChangeGroup> groups) {
        List<UmlChangeGroup>[] classifyGroups = new List[2];
        List<UmlChangeGroup> needSort = new ArrayList<>();
        List<UmlChangeGroup> notSort = new ArrayList<>();

        for (UmlChangeGroup group : groups) {
            boolean find = false;
            for (UmlAtomicChange change : group.getChangeList()) {
                if ((change.getObj() == UmlAtomicChange.OptObj.CLASS ||
                change.getObj() == UmlAtomicChange.OptObj.MESSAGE ||
                change.getObj() == UmlAtomicChange.OptObj.RELATION) && change.getType() == UmlAtomicChange.OptType.ADD) {
                    find = true;
                    needSort.add(group);
                    break;
                }
            }
            if (!find) {
                notSort.add(group);
            }
        }
        classifyGroups[0] = needSort;
        classifyGroups[1] = notSort;

        return classifyGroups;
    }

    private static void sortBetweenGroups(Set<SortRule> groupSortConstraint, List<UmlChangeGroup> groups) {
        for (SortRule rule : groupSortConstraint) {
            if (groups.contains(rule.getBefore()) && groups.contains(rule.getAfter())) {
                int beforeIndex = groups.indexOf(rule.getBefore());
                int afterIndex = groups.indexOf(rule.getAfter());
                if (beforeIndex > afterIndex) {
                    swapInList(groups, beforeIndex, afterIndex);
                }
            }
        }
    }

    private static void swapInList(List<UmlChangeGroup> groups, int beforeIndex, int afterIndex) {
        UmlChangeGroup after = groups.remove(afterIndex);
        groups.add(after);
    }

    /**
     * violence get all sorted group
     */
    public static List<List<UmlChangeGroup>> violenceGetAllSortedGroup(Set<SortRule> groupSortConstraint, List<UmlChangeGroup> groups) {
        List<UmlChangeGroup> [] classifyGroup = classifyGroup(groups);
        if (classifyGroup[1].size() == 0) {
            return SortUtils.getAllSortedGroups(groupSortConstraint, groups);
        } else {
            List<List<UmlChangeGroup>> sortedGroups = SortUtils.getAllSortedGroups(groupSortConstraint, classifyGroup[0]);
            sortBetweenGroups(groupSortConstraint, classifyGroup[1]);
            for (List<UmlChangeGroup> sortedGroup : sortedGroups) {
                sortedGroup.addAll(classifyGroup[1]);
            }
            return sortedGroups;
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        List<List<Integer>> indexSort = getAllIndexSort(11);
        long end = System.currentTimeMillis();
        System.out.println((end - start) / 1000 / 60);
    }
}
