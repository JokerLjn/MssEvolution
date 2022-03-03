package hit.ices.jokerljn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.cloning.Cloner;
import hit.ices.jokerljn.groupAndSort.GroupUtils;
import hit.ices.jokerljn.groupAndSort.SortRule;
import hit.ices.jokerljn.groupAndSort.SortUtils;
import hit.ices.jokerljn.groupAndSort.UmlChangeGroup;
import hit.ices.jokerljn.importer.ImportObject;
import hit.ices.jokerljn.importer.InputData;
import hit.ices.jokerljn.importer.mss.MicroserviceSystem;
import hit.ices.jokerljn.importer.uml.UmlModel;
import hit.ices.jokerljn.importer.uml.classmodel.ClassDiagram;
import hit.ices.jokerljn.importer.uml.sequence.SequenceDiagram;
import hit.ices.jokerljn.importer.umlchange.UmlAtomicChange;
import hit.ices.jokerljn.mapRule.MapRule;
import hit.ices.jokerljn.mapRule.OptionalMssWithChange;
import hit.ices.jokerljn.mapRule.UmlToMssMap;
import hit.ices.jokerljn.mapRule.UpdateUmlModel;
import hit.ices.jokerljn.quality.QualityScore;
import hit.ices.jokerljn.solve.Solver;
import hit.ices.jokerljn.solve.ViolenceSolver;
import hit.ices.jokerljn.utils.FileUtils;
import hit.ices.jokerljn.utils.MagicNoteUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static hit.ices.jokerljn.groupAndSort.SortUtils.violenceGetAllSortedGroup;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 16:48
 * @desc
 */
@Slf4j
public class MainApplication {

    public static InputData init(String casePath, int choice) {
        InputData inputData = new InputData();

        inputData.setOldUmlModel(importOldUml(casePath));
//        inputData.setNewUmlModel(importNewUml(casePath));
        inputData.setUmlChangeList(importUmlChangeList(casePath, choice));
        inputData.setOldMss(importOldMss(casePath));
        UmlModel newUmlModel = new Cloner().deepClone(inputData.getOldUmlModel());
        for (UmlAtomicChange atomicChange : inputData.getUmlChangeList()) {
            UpdateUmlModel.updateUml(newUmlModel, atomicChange);
        }
        inputData.setNewUmlModel(newUmlModel);

        log.info("输入数据导入成功！");
        return inputData;
    }

    public static UmlModel importOldUml(String casePath) {
        UmlModel oldUmlModel = new UmlModel();
        String oldPath = casePath + "/olduml/";

        try {
            // handle old uml
            oldUmlModel.setClassDiagram(ImportObject.importClassDiagram(oldPath + "class.json"));
            ArrayList<SequenceDiagram> tempList = new ArrayList<>();
            tempList.add(ImportObject.importSequenceDiagram(oldPath + "sequence.json"));
            oldUmlModel.setSequenceDiagramList(tempList);
            log.info("旧UML模型初始化导入成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oldUmlModel;
    }

    public static List<UmlAtomicChange> importUmlChangeList(String casePath, int choice) {
        // import uml change list
        List<UmlAtomicChange> umlChangeList = null;
        String changeFile = "minChanges.json";
        if (choice == 2) {
            changeFile = "midChanges.json";
        } else if (choice == 3) {
            changeFile = "maxChanges.json";
        }
        String changePath = casePath + "/umlChange/" + changeFile;
        try {
            umlChangeList = ImportObject.importUmlChange(changePath);
            log.info("UML 原子变化列表初始化成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return umlChangeList;
    }

    public static MicroserviceSystem importOldMss(String casePath) {
        // import oldMSS
        MicroserviceSystem system = null;
        String path = casePath + "/mss/oldMss.json";
        try {
            system = ImportObject.importMicroserviceSystem(path);
            log.info("旧 MSS 初始化成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return system;
    }

    /**
     * 将变化后的 class diagram 导出为json格式的字符串
     */
    public static void generateNewClassJson(String casePath, int choice, String outPath, String fileName) throws JsonProcessingException {
        UmlModel oldUml = importOldUml(casePath);
        List<UmlAtomicChange> changes = importUmlChangeList(casePath, choice);
        for (UmlAtomicChange change : changes) {
            UpdateUmlModel.updateUml(oldUml, change);
        }
        ClassDiagram newClassDiagram = oldUml.getClassDiagram();
        ObjectMapper objectMapper = new ObjectMapper();
        String classJson = objectMapper.writeValueAsString(newClassDiagram);
        FileUtils.writeToFile(outPath, classJson, fileName);
    }

    /**
     * 对 输入的 uml 变化列表 进行分组、排序，得到一组排序后的UML列表
     */
    public static List<List<UmlAtomicChange>> groupAndSort(InputData data) {
        List<List<UmlAtomicChange>> finalResult = new ArrayList<>();

        List<UmlChangeGroup> changeGroups = GroupUtils.group(data.getUmlChangeList());

        // TODO 分组间排序
        Set<SortRule> groupSort = SortUtils.sortBetweenGroups(changeGroups, data.getUmlChangeList());

        // TODO 获取所有可行的分组排序(现在默认实现为暴力穷举)
        List<List<UmlChangeGroup>> sortedGroups = violenceGetAllSortedGroup(groupSort, changeGroups);

        // 分组内排序
        changeGroups.forEach(SortUtils::sortInGroup);

        for (List<UmlChangeGroup> groups : sortedGroups) {

            List<List<UmlAtomicChange>> sortResult = new ArrayList<>();
            // 合并多个分组内排序结果得到最终 Uml 序列
            for (UmlChangeGroup group : groups) {
                List<List<UmlAtomicChange>> tempResults = new ArrayList<>();
                if (sortResult.size() == 0) {
                    if (group.getAllSortedChangeList().size() == 0) {
                        sortResult.add(group.getChangeList());
                    } else {
                        sortResult.addAll(group.getAllSortedChangeList());
                    }
                } else {
                    for (List<UmlAtomicChange> changeList : sortResult) {
                        if (group.getAllSortedChangeList().size() == 0) {
                            List<UmlAtomicChange> changeListTemp = new ArrayList<>(changeList);
                            changeListTemp.addAll(group.getChangeList());
                            tempResults.add(changeListTemp);
                        } else {
                            for (List<UmlAtomicChange> changeSortList : group.getAllSortedChangeList()) {
                                List<UmlAtomicChange> changeListTemp = new ArrayList<>(changeList);
                                changeListTemp.addAll(changeSortList);
                                tempResults.add(changeListTemp);
                            }
                        }
                    }
                    sortResult = tempResults;
                }
            }
            // add each group list result to final result
            finalResult.addAll(sortResult);
        }
        return finalResult;
    }

    /**
     * 将 UML 变化 映射到 MSS 变化上
     */
    public static List<OptionalMssWithChange> mapFromUmlToMss(List<UmlAtomicChange> changes, MicroserviceSystem oldMss, UmlModel umlModel, UmlModel newUmlModel, boolean simpleRule) {
        List<OptionalMssWithChange> results = new ArrayList<>();
        MapRule mapRule = new MapRule(oldMss, umlModel, newUmlModel);
        Cloner cloner = new Cloner();

        for (UmlAtomicChange change : changes) {
            if (change.getObj() == UmlAtomicChange.OptObj.OBJECT) {
                continue;
            }
            List<OptionalMssWithChange> tempResultList = new ArrayList<>();
            if (results.size() != 0) {
                for (OptionalMssWithChange tempOp : results) {
                    mapRule.setMss(cloner.deepClone(tempOp.getMss()));
                    mapRule.setUmlModel(umlModel);
                    List<OptionalMssWithChange> mapResults;
                    if (simpleRule) {
                        mapResults = mapRule.changeMssInSimpleRule(change);
                    } else {
                        mapResults = mapRule.changeMss(change);
                    }


                    for (OptionalMssWithChange mapResult : mapResults) {
                        OptionalMssWithChange newTempResult = cloner.deepClone(tempOp);
                        newTempResult.setMss(mapResult.getMss());
                        newTempResult.getMssAtomicChangeList().addAll(mapResult.getMssAtomicChangeList());
                        newTempResult.getUmlToMssMaps().add(new UmlToMssMap(change, mapResult.getMssAtomicChangeList()));
                        tempResultList.add(newTempResult);
                    }
                }
            } else {
                List<OptionalMssWithChange> mapResults;
                if (simpleRule) {
                    mapResults = mapRule.changeMssInSimpleRule(change);
                } else {
                    mapResults = mapRule.changeMss(change);
                }
                mapResults.forEach(mapResult -> {
                    mapResult.getUmlToMssMaps().add(new UmlToMssMap(change, new ArrayList<>(mapResult.getMssAtomicChangeList())));
                });
                tempResultList.addAll(mapResults);
            }
            // update umlModel
            UpdateUmlModel.updateUml(umlModel, change);

            // update results
            results = tempResultList;
        }

        return results;
    }

}
