package hit.ices.jokerljn.mapRule;

import com.rits.cloning.Cloner;
import hit.ices.jokerljn.importer.mss.Microservice;
import hit.ices.jokerljn.importer.mss.MicroserviceSystem;
import hit.ices.jokerljn.importer.mss.MsInterface;
import hit.ices.jokerljn.importer.mss.MssAtomicChange;
import hit.ices.jokerljn.importer.uml.UmlModel;
import hit.ices.jokerljn.importer.uml.classmodel.ClassRelation;
import hit.ices.jokerljn.importer.uml.classmodel.UmlAttribute;
import hit.ices.jokerljn.importer.uml.classmodel.UmlClass;
import hit.ices.jokerljn.importer.uml.classmodel.UmlMethod;
import hit.ices.jokerljn.importer.uml.sequence.MethodCall;
import hit.ices.jokerljn.importer.uml.sequence.SequenceDiagram;
import hit.ices.jokerljn.importer.umlchange.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/13 19:42
 * @desc TODO 根据当前的 MSS与 umlModel，针对输入的 Uml变化，给出可选的微服务变化及作用后的 MSS 列表
 */
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class MapRule {

    private MicroserviceSystem mss;
    private UmlModel umlModel;
    private UmlModel newUmlModel;
    private Cloner cloner;

    public MapRule (MicroserviceSystem mss) {
        this.mss = mss;
        this.cloner = new Cloner();
    }

    public MapRule (MicroserviceSystem mss, UmlModel umlModel) {
        this.mss = mss;
        this.umlModel = umlModel;
        this.cloner = new Cloner();
    }

    public MapRule (MicroserviceSystem mss, UmlModel umlModel, UmlModel newUmlModel) {
        this.mss = mss;
        this.umlModel = umlModel;
        this.newUmlModel = newUmlModel;
        this.cloner = new Cloner();
    }

    /**
     * find ms by classname
     */
    private Microservice getMsByClassName(String name) {
        for (Microservice ms : mss.getMicroservices()) {
            for (UmlClass umlClass : ms.getClassList()) {
                if (umlClass.getName().equals(name)) {
                    return ms;
                }
            }
        }
        return null;
    }

    /**
     * find ms by classname
     */
    private Microservice getMsByMsAndClassName(MicroserviceSystem mss, String name) {
        for (Microservice ms : mss.getMicroservices()) {
            for (UmlClass umlClass : ms.getClassList()) {
                if (umlClass.getName().equals(name)) {
                    return ms;
                }
            }
        }
        return null;
    }

    /**
     * find umlClass by class name
     */
    private UmlClass getClass(Microservice ms, String name) {
        for (UmlClass umlClass : ms.getClassList()) {
            if (umlClass.getName().equals(name)) {
                return umlClass;
            }
        }
        return null;
    }

    /**
     * 将 uml 原子变化作用于 mss 上
     */
    public List<OptionalMssWithChange> changeMss(UmlAtomicChange change) {
        List<OptionalMssWithChange> optionalMssChange = new ArrayList<>();

        switch (change.getObj()) {
            case CLASS:
                optionalMssChange = classChangeToMss((ClassChange) change);
                break;
            case OBJECT:
                // 时序图中 object 的变化不需要作用在 mss 上
                optionalMssChange.add(new OptionalMssWithChange());
                break;
            case ATTRIBUTE:
                optionalMssChange = attributeChangeToMss((AttributeChange) change);
                break;
            case METHOD:
                optionalMssChange = methodChangeToMss((MethodChange) change);
                break;
            case RELATION:
                optionalMssChange = relationChangeToMss((RelationShipChange) change);
                break;
            case MESSAGE:
                optionalMssChange = messageChangeToMss((MessageChange) change);
                break;
            default:
        }
        return optionalMssChange;
    }

    /**
     * 类的变化作用于 mss 上
     * TODO delete 的情况待完成
     */
    private List<OptionalMssWithChange> classChangeToMss(ClassChange change) {
        List<OptionalMssWithChange> optionalMssWithChanges = new ArrayList<>();
        String name = change.getClassName();

        if (change.getType() == UmlAtomicChange.OptType.ADD) {
            // 增加类：新增微服务/把类放到现有的某个微服务中

            // choice1：增加微服务
            // 复制 mss
            MicroserviceSystem mss1 = cloner.deepClone(mss);
            List<MssAtomicChange> mssChangeList1 = new ArrayList<>();

            Microservice newMs = new Microservice();
            newMs.setName(name + "Service");
            newMs.setClassList(new ArrayList<>(Collections.singletonList(
                    new UmlClass(name)
            )));
            newMs.setClassRelationList(new ArrayList<>());
            newMs.setInterfaceList(new HashSet<>());

            mss1.getMicroservices().add(newMs);

            // generate add ms change
            MssAtomicChange mssMsChange = new MssAtomicChange(
                    MssAtomicChange.OptType.ADD,
                    MssAtomicChange.OptObj.MICROSERVICE,
                    newMs.getName(),
                    new HashMap<>(Collections.singletonMap("msName", newMs.getName()))
            );
            mssChangeList1.add(mssMsChange);

            // generate add class change
            MssAtomicChange mssClassChange = new MssAtomicChange(
                    MssAtomicChange.OptType.ADD,
                    MssAtomicChange.OptObj.CLASS,
                    name,
                    new HashMap<>(Collections.singletonMap("className", name))
            );
            mssChangeList1.add(mssClassChange);
            optionalMssWithChanges.add(new OptionalMssWithChange(mss1, mssChangeList1));

            // choice2: 根据Uml类图/时序图判断把类放到哪个服务中
                // 1. 获取可以放到的 MS 列表
            Set<Microservice> msSet = new HashSet<>();
                // search in relation list
            for (ClassRelation relation : newUmlModel.getClassDiagram().getClassRelationList()) {
                if (relation.getOrigin().getName().equals(name)) {
                    msSet.add(getMsByClassName(relation.getDestination().getName()));
                } else if (relation.getDestination().getName().equals(name)) {
                    msSet.add(getMsByClassName(relation.getOrigin().getName()));
                }
            }
                // search in methodCall list
            for (SequenceDiagram sequenceDiagram : newUmlModel.getSequenceDiagramList()) {
                for (MethodCall call : sequenceDiagram.getMethodCalls()) {
                    if (call.getFrom().equals(name)) {
                        msSet.add(getMsByClassName(call.getTo()));
                    } else if (call.getTo().equals(name)) {
                        msSet.add(getMsByClassName(call.getFrom()));
                    }
                }
            }

            if (msSet.size() == 0) {
                return optionalMssWithChanges;
            }
            // 遍历复制 mss， 将类加入到对应的服务msTemp中，同时生成mssAtomicChange
            for (Microservice ms : msSet) {
                if (ms == null) {
                    continue;
                }
                // 复制 mss
                MicroserviceSystem mssTemp = cloner.deepClone(mss);
                List<MssAtomicChange> mssChangeListTemp = new ArrayList<>();

                for (Microservice ms1 : mssTemp.getMicroservices()) {
                    if (ms1.equals(ms)) {
                        // generate add class change
                        ms1.getClassList().add(new UmlClass(name));
                        MssAtomicChange mssClassChangeTemp = new MssAtomicChange(
                                MssAtomicChange.OptType.ADD,
                                MssAtomicChange.OptObj.CLASS,
                                ms1.getName(),
                                new HashMap<>(Collections.singletonMap("className", name))
                        );
                        mssChangeListTemp.add(mssClassChangeTemp);
                        // 保存这次选择的结果
                        optionalMssWithChanges.add(new OptionalMssWithChange(mssTemp, mssChangeListTemp));
                    }
                }
            }
        } else {
            // 删除类: 对应在微服务中删除这个类，类间的关系，类提供的接口
            List<MssAtomicChange> mssAtomicChangeList = new ArrayList<>();

            Microservice ms = getMsByClassName(name);

            if (ms == null) {
                throw new IllegalArgumentException("当前变化所属类找不到所在的微服务: " + name);
            }

            // 生成删除类对应的 mss change
            MssAtomicChange mssClassChange = new MssAtomicChange(
                    MssAtomicChange.OptType.DELETE,
                    MssAtomicChange.OptObj.CLASS,
                    ms.getName(),
                    new HashMap<>(Collections.singletonMap("className", name))
            );
            mssAtomicChangeList.add(mssClassChange);

            // 删除类
            ms.getClassList().removeIf(umlClass -> umlClass.getName().equals(name));

            // 删除关系
            ms.getClassRelationList().removeIf(relation -> {
                if (relation.getOrigin().getName().equals(name) ||
                        relation.getDestination().getName().equals(name)) {
                    MssAtomicChange mssRelationChange = new MssAtomicChange(
                            MssAtomicChange.OptType.DELETE,
                            MssAtomicChange.OptObj.RELATION,
                            ms.getName(),
                            new HashMap<>(Collections.singletonMap("relation", relation))
                    );
                    mssAtomicChangeList.add(mssRelationChange);
                    return true;
                }
                return false;
            });
            // 删除接口
            ms.getInterfaceList().removeIf(msInterface -> {
                if (msInterface.getClassName().equals(name)) {
                    MssAtomicChange mssInterfaceChange = new MssAtomicChange(
                            MssAtomicChange.OptType.DELETE,
                            MssAtomicChange.OptObj.INTERFACE,
                            ms.getName(),
                            new HashMap<>(Collections.singletonMap("interface", msInterface))
                    );
                    mssAtomicChangeList.add(mssInterfaceChange);
                    return true;
                }
                return false;
            });
            optionalMssWithChanges.add(new OptionalMssWithChange(mss, mssAtomicChangeList));
        }

        return optionalMssWithChanges;
    }

    /**
     * uml 属性变化映射到 mss 上
     *
     * @param change attribute change
     */
    private List<OptionalMssWithChange> attributeChangeToMss(AttributeChange change) {
        List<OptionalMssWithChange> optionalMssWithChanges = new ArrayList<>();
        List<MssAtomicChange> mssAtomicChangeList = new ArrayList<>();
        // 找到变化所在类的所属微服务
        Microservice ms = getMsByClassName(change.getClassName());
        if (ms == null) {
            log.warn("当前变化所属类找不到所在的微服务: " + change.getClassName());
            optionalMssWithChanges.add(new OptionalMssWithChange(mss, mssAtomicChangeList));
            return optionalMssWithChanges;
        }
        UmlClass umlClass = getClass(ms, change.getClassName());
        if (umlClass.getAttributes() == null) {
            umlClass.setAttributes(new ArrayList<>());
        }
        List<UmlAttribute> attributeList = umlClass.getAttributes();

        // generate mss change
        MssAtomicChange mssChange = new MssAtomicChange();
        mssChange.setMsName(ms.getName());
        mssChange.setObj(MssAtomicChange.OptObj.ATTRIBUTE);
        Map<String, Object> map = new HashMap<>(16);

        // TODO 对属性变化的处理暂时只考虑了类内部的变化，未考虑影响到的方法/关系变化或数据库
        switch (change.getType()) {
            case ADD:
                umlClass.getAttributes().add(change.getAttribute());
                mssChange.setType(MssAtomicChange.OptType.ADD);
                map.put("attribute", change.getAttribute());
                break;
            case UPDATE:
                mssChange.setType(MssAtomicChange.OptType.UPDATE);
                attributeList.removeIf(attribute -> {
                    if (attribute.equals(change.getAttribute())) {
                        map.put("attribute", attribute);
                        return true;
                    }
                    return false;
                });
                attributeList.add(change.getAttribute());
                break;
            case DELETE:
                mssChange.setType(MssAtomicChange.OptType.DELETE);
                attributeList.removeIf(attribute -> {
                    if (attribute.equals(change.getAttribute())) {
                        map.put("attribute", attribute);
                        return true;
                    }
                    return false;
                });
                break;
            default:
        }

        mssChange.setValueMap(map);
        mssAtomicChangeList.add(mssChange);

        optionalMssWithChanges.add(new OptionalMssWithChange(mss, mssAtomicChangeList));
        return optionalMssWithChanges;
    }

    /**
     * method change to mss
     * TODO update 的情况待完成
     */
    private List<OptionalMssWithChange> methodChangeToMss(MethodChange change) {
        List<OptionalMssWithChange> optionalMssWithChangeList = new ArrayList<>();

        List<MssAtomicChange> mssAtomicChangeList = new ArrayList<>();
        // 找到变化所在类的所属微服务
        Microservice ms = getMsByClassName(change.getClassName());
        if (ms == null) {
            throw new IllegalArgumentException("当前变化所属类" + change.getClassName() + " 找不到所在的微服务！");
        }
        UmlClass umlClass = getClass(ms, change.getClassName());
        if (umlClass.getMethods() == null) {
            umlClass.setMethods(new ArrayList<>());
        }
        List<UmlMethod> methodList = umlClass.getMethods();
        UmlMethod method = change.getMethod();

        switch(change.getType()) {
            case ADD:
                methodList.add(method);
                MssAtomicChange mssAddChange = new MssAtomicChange(
                        MssAtomicChange.OptType.ADD,
                        MssAtomicChange.OptObj.METHOD,
                        ms.getName(),
                        new HashMap<>(Collections.singletonMap("method", method))
                );
                mssAtomicChangeList.add(mssAddChange);
                break;
            case UPDATE:
                // TODO 未处理修改方法后对应修改接口及微服务间接口调用关系
                methodList.removeIf(umlMethod -> umlMethod.equals(method));
                methodList.add(change.getMethod());
                MssAtomicChange mssUpdateChange = new MssAtomicChange(
                        MssAtomicChange.OptType.UPDATE,
                        MssAtomicChange.OptObj.METHOD,
                        ms.getName(),
                        new HashMap<>(Collections.singletonMap("method", method))
                );
                mssAtomicChangeList.add(mssUpdateChange);
                break;
            case DELETE:
                // 删除方法
                methodList.removeIf(umlMethod -> umlMethod.equals(method));
                methodList.add(method);
                MssAtomicChange mssDeleteChange = new MssAtomicChange(
                        MssAtomicChange.OptType.DELETE,
                        MssAtomicChange.OptObj.METHOD,
                        ms.getName(),
                        new HashMap<>(Collections.singletonMap("method", method))
                );
                mssAtomicChangeList.add(mssDeleteChange);
                // 删除接口及微服务间接口调用关系
                if (ms.getInterfaceList() == null) {
                    ms.setInterfaceList(new HashSet<>());
                }
                ms.getInterfaceList().removeIf(msInterface -> {
                    if (msInterface.getMethodName().equals(method.getName())) {
                        MssAtomicChange mssInterfaceChange = new MssAtomicChange(
                                MssAtomicChange.OptType.DELETE,
                                MssAtomicChange.OptObj.INTERFACE,
                                ms.getName(),
                                new HashMap<>(Collections.singletonMap("interface", msInterface))
                        );
                        mssAtomicChangeList.add(mssInterfaceChange);
                        return true;
                    }
                    return false;
                });
                break;
            default:
        }

        optionalMssWithChangeList.add(new OptionalMssWithChange(mss, mssAtomicChangeList));
        return optionalMssWithChangeList;
    }

    /**
     * 增加关系/消息时移动类（类A放到类B所在服务中/类B放到类A所在服务中）的处理方式
     */
    private OptionalMssWithChange moveClassBetweenMsByChange(MicroserviceSystem mss, Microservice fromMs, Microservice toMs, UmlAtomicChange change, String fromClass, String toClass, boolean positive) {
        // choice1: 将类A加入到类B所在的服务中
        List<MssAtomicChange> changes = new ArrayList<>();
        UmlClass A = getClass(fromMs, fromClass);
        UmlClass B = getClass(toMs, toClass);

        // move class
        fromMs.getClassList().removeIf(umlClass -> umlClass.equals(A));
        toMs.getClassList().add(A);
        Map<String, Object> moveClassMap1 = new HashMap<>();
        moveClassMap1.put("fromMs", fromMs.getName());
        moveClassMap1.put("toMs", toMs.getName());
        moveClassMap1.put("className", A.getName());
        changes.add(new MssAtomicChange(
                MssAtomicChange.OptType.MOVE,
                MssAtomicChange.OptObj.CLASS,
                toMs.getName(),
                moveClassMap1)
        );
        // 如果移动类之后，fromMs不包含其他类的话，删除微服务fromMs
        if (fromMs.getClassList().size() == 0) {
            mss.getMicroservices().removeIf(microservice -> microservice.getName().equals(fromMs.getName()));
            changes.add(new MssAtomicChange(
                    MssAtomicChange.OptType.DELETE,
                    MssAtomicChange.OptObj.MICROSERVICE,
                    fromMs.getName(),
                    new HashMap<>(Collections.singletonMap("msName", fromMs.getName())))
            );
        }

        // delete relation
        if (fromMs.getClassRelationList() != null) {
            fromMs.getClassRelationList().removeIf(classRelation -> {
                if (classRelation.getOrigin().equals(A)
                        || classRelation.getDestination().equals(A)) {
                    changes.add(new MssAtomicChange(
                            MssAtomicChange.OptType.DELETE,
                            MssAtomicChange.OptObj.RELATION,
                            fromMs.getName(),
                            new HashMap<>(Collections.singletonMap("relation", classRelation)))
                    );
                    return true;
                }
                return false;
            });
        }

        // remove interface and add interface
        if (fromMs.getInterfaceList() != null) {
            fromMs.getInterfaceList().removeIf(msInterface -> {
                if (msInterface.getClassName().equals(A.getName())) {
                    if (toMs.getInterfaceList() == null) {
                        toMs.setInterfaceList(new HashSet<>());
                    }
                    toMs.getInterfaceList().add(msInterface);
                    Map<String, Object> moveInterfaceMap = new HashMap<>();
                    moveInterfaceMap.put("fromMs", fromMs.getName());
                    moveInterfaceMap.put("toMs", toMs.getName());
                    moveInterfaceMap.put("interface", msInterface);
                    changes.add(new MssAtomicChange(
                            MssAtomicChange.OptType.MOVE,
                            MssAtomicChange.OptObj.INTERFACE,
                            toMs.getName(),
                            moveInterfaceMap)
                    );
                    return true;
                }
                return false;
            });
        }

        if (change.getObj() == UmlAtomicChange.OptObj.RELATION) {
            RelationShipChange relationChange = (RelationShipChange) change;
            // 考虑到将类B加入类A所在服务时，A->B的关系顺序会反转，通过 positive 变量进行控制
            ClassRelation addRelation = null;
            if (positive) {
                addRelation = new ClassRelation(
                        A, B, relationChange.getRelation().getType()
                );
            } else {
                addRelation = new ClassRelation(
                        B, A, relationChange.getRelation().getType()
                );
            }
            if (toMs.getClassRelationList() == null) {
                toMs.setClassRelationList(new ArrayList<>());
            }
            toMs.getClassRelationList().add(addRelation);
            changes.add(new MssAtomicChange(
                    MssAtomicChange.OptType.ADD,
                    MssAtomicChange.OptObj.RELATION,
                    toMs.getName(),
                    new HashMap<>(Collections.singletonMap("relation", addRelation)))
            );
        }

        return new OptionalMssWithChange(mss, changes);
    }

    /**
     * 增加关系/消息时合并服务的处理方式
     */
    private OptionalMssWithChange mergeTwoMs(MicroserviceSystem mss, Microservice fromMs, Microservice toMs, UmlAtomicChange change) {
        // TODO 根据两个服务类的多少决定移动哪个服务
        Microservice moveFrom, moveTo;
        if (fromMs.getClassList().size() > toMs.getClassList().size()) {
            moveFrom = toMs;
            moveTo = fromMs;
        } else {
            moveFrom = fromMs;
            moveTo = toMs;
        }

        List<MssAtomicChange> changes = new ArrayList<>();

        // move class
        moveFrom.getClassList().forEach(umlClass -> {
            moveTo.getClassList().add(umlClass);
            Map<String, Object> moveClassMap = new HashMap<>();
            moveClassMap.put("fromMs", fromMs.getName());
            moveClassMap.put("toMs", toMs.getName());
            moveClassMap.put("className", umlClass.getName());
            MssAtomicChange temp = new MssAtomicChange(
                    MssAtomicChange.OptType.MOVE,
                    MssAtomicChange.OptObj.CLASS,
                    moveTo.getName(),
                    moveClassMap
            );
            changes.add(temp);
        });
        // move class relation
        if (moveFrom.getClassRelationList() != null) {
            moveFrom.getClassRelationList().forEach(classRelation -> {
                moveTo.getClassRelationList().add(classRelation);
                MssAtomicChange temp = new MssAtomicChange(
                        MssAtomicChange.OptType.MOVE,
                        MssAtomicChange.OptObj.RELATION,
                        moveTo.getName(),
                        new HashMap<>(Collections.singletonMap("relation", classRelation))
                );
                changes.add(temp);
            });
        }

        // move interface
        if (moveFrom.getInterfaceList() != null) {
            moveFrom.getInterfaceList().forEach(msInterface -> {
                if (moveTo.getInterfaceList() == null) {
                    moveTo.setInterfaceList(new HashSet<>());
                }
                moveTo.getInterfaceList().add(msInterface);
                MssAtomicChange temp = new MssAtomicChange(
                        MssAtomicChange.OptType.MOVE,
                        MssAtomicChange.OptObj.INTERFACE,
                        moveTo.getName(),
                        new HashMap<>(Collections.singletonMap("interface", msInterface))
                );
                changes.add(temp);
            });
        }

        // delete fromMs
        mss.getMicroservices().removeIf(microservice -> microservice.getName().equals(moveFrom.getName()));
        OptionalMssWithChange optionalMssWithChange = new OptionalMssWithChange(mss, changes);

        // add relation if change is relationChange
        if (change.getObj() == UmlAtomicChange.OptObj.RELATION) {
            RelationShipChange relationChange = (RelationShipChange) change;
            UmlClass A = getClass(fromMs, relationChange.getRelation().getOrigin().getName());
            UmlClass B = getClass(toMs, relationChange.getRelation().getDestination().getName());
            ClassRelation addRelation = new ClassRelation(
                        A, B, relationChange.getRelation().getType()
            );
            toMs.getClassRelationList().add(addRelation);
            changes.add(new MssAtomicChange(
                    MssAtomicChange.OptType.ADD,
                    MssAtomicChange.OptObj.RELATION,
                    toMs.getName(),
                    new HashMap<>(Collections.singletonMap("relation", addRelation)))
            );
        }

        return optionalMssWithChange;
    }

    /**
     * class relation change to mss
     * 同一服务中删除关系部分未完成
     */
    private List<OptionalMssWithChange> relationChangeToMss(RelationShipChange change) {
        List<OptionalMssWithChange> optionalMssWithChanges = new ArrayList<>();

        List<MssAtomicChange> mssAtomicChangeList = new ArrayList<>();
        ClassRelation relation = change.getRelation();
        String fromClassName = change.getRelation().getOrigin().getName();
        String toClassName = change.getRelation().getDestination().getName();
        // 找到变化所在类的所属微服务
        Microservice fromMs = getMsByClassName(fromClassName);
        Microservice toMs = getMsByClassName(toClassName);
        if (fromMs == null || toMs == null) {
            throw new IllegalArgumentException("当前变化所属类找不到所在的微服务！" + change);
        }

        // 起点类和终点类在不同服务中
        if (!fromMs.equals(toMs)) {
            // 在不同微服务的类间增加关系，可能会导致局部微服务重构
            if (change.getType() == UmlAtomicChange.OptType.ADD) {
                // choice1: 将类A加入到类B所在的服务中
                MicroserviceSystem mss1 = cloner.deepClone(mss);
                Microservice fromMs1 = getMsByMsAndClassName(mss1, fromClassName);
                Microservice toMs1 = getMsByMsAndClassName(mss1, toClassName);
                OptionalMssWithChange optionalMssWithChange1 = moveClassBetweenMsByChange(mss1, fromMs1, toMs1, change, fromClassName, toClassName, true);
                optionalMssWithChanges.add(optionalMssWithChange1);


                // choice2：将类B加入到类A所在的服务中
                MicroserviceSystem mss2 = cloner.deepClone(mss);
                Microservice fromMs2 = getMsByMsAndClassName(mss2, fromClassName);
                Microservice toMs2 = getMsByMsAndClassName(mss2, toClassName);
                OptionalMssWithChange optionalMssWithChange2= moveClassBetweenMsByChange(mss2, toMs2, fromMs2, change, toClassName, fromClassName, false);
                optionalMssWithChanges.add(optionalMssWithChange2);


                // choice3：将类A与类B所在的服务合并为一个服务
                MicroserviceSystem mss3 = cloner.deepClone(mss);
                Microservice fromMs3 = getMsByMsAndClassName(mss3, fromClassName);
                Microservice toMs3 = getMsByMsAndClassName(mss3, toClassName);
                OptionalMssWithChange optionalMssWithChange3 = mergeTwoMs(mss3, fromMs3, toMs3, change);
                optionalMssWithChanges.add(optionalMssWithChange3);
            } else {
                optionalMssWithChanges.add(new OptionalMssWithChange(mss, new ArrayList<>()));
            }
            return optionalMssWithChanges;
        }

        // 起点类与终点类在相同服务中
        List<ClassRelation> list = fromMs.getClassRelationList();

        switch(change.getType()) {
            case ADD:
                list.add(change.getRelation());
                MssAtomicChange addChange = new MssAtomicChange(
                        MssAtomicChange.OptType.ADD,
                        MssAtomicChange.OptObj.RELATION,
                        fromMs.getName(),
                        new HashMap<>(Collections.singletonMap("relation", relation))
                );
                mssAtomicChangeList.add(addChange);
                break;
            case UPDATE:
                list.removeIf(classRelation -> classRelation.equals(relation));
                list.add(relation);
                MssAtomicChange updateChange = new MssAtomicChange(
                        MssAtomicChange.OptType.UPDATE,
                        MssAtomicChange.OptObj.RELATION,
                        fromMs.getName(),
                        new HashMap<>(Collections.singletonMap("relation", relation))
                );
                mssAtomicChangeList.add(updateChange);
                break;
            case DELETE:
                // TODO 删除起点类与终点类处于同一个服务中的关系，考虑局部重构
                list.removeIf(classRelation -> classRelation.equals(relation));
                MssAtomicChange deleteChange = new MssAtomicChange(
                        MssAtomicChange.OptType.DELETE,
                        MssAtomicChange.OptObj.RELATION,
                        fromMs.getName(),
                        new HashMap<>(Collections.singletonMap("relation", relation))
                );
                mssAtomicChangeList.add(deleteChange);
                break;
            default:
        }

        optionalMssWithChanges.add(new OptionalMssWithChange(mss, mssAtomicChangeList));
        return optionalMssWithChanges;
    }

    /**
     * message change to mss
     */
    private List<OptionalMssWithChange> messageChangeToMss(MessageChange change) {
        List<OptionalMssWithChange> optionalMssWithChangeList = new ArrayList<>();
        List<MssAtomicChange> mssAtomicChangeList = new ArrayList<>();
        // 找到消息的起点与终点所在类的所属微服务
        Microservice fromMs = getMsByClassName(change.getMethodCall().getFrom());
        Microservice toMs = getMsByClassName(change.getMethodCall().getTo());
        String fromClassName = change.getMethodCall().getFrom();
        String toClassName = change.getMethodCall().getTo();

        if (fromMs == null || toMs == null) {
            log.warn("当前变化所属类找不到所在的微服务：" + change);
            optionalMssWithChangeList.add(new OptionalMssWithChange(mss, mssAtomicChangeList));
            return optionalMssWithChangeList;
        }
        if (fromMs.equals(toMs)) {
            // 在同一个服务中增加消息传递，MSS 无需任何变化
            // TODO 在同一个服务中删除消息传递，考虑对起点、终点类所在服务进行重构
            optionalMssWithChangeList.add(new OptionalMssWithChange(mss, mssAtomicChangeList));
            return optionalMssWithChangeList;
        }

        // 在不同服务间增加消息传递，可能会导致微服务局部重构
        switch(change.getType()) {
            case ADD:
                // choice1: 增加服务间接口调用
                MicroserviceSystem mss1 = cloner.deepClone(mss);
                List<MssAtomicChange> mssChanges1 = new ArrayList<>();
                Microservice toMs1 = getMsByMsAndClassName(mss1, change.getMethodCall().getTo());
                MsInterface msAddInterface = getInterface(toMs1, change.getMethodCall().getMethod());
                if (msAddInterface == null) {
                    // add interface
                    msAddInterface = new MsInterface(
                        "/" + change.getMethodCall().getMethod(),
                        "POST",
                        change.getMethodCall().getTo(),
                        change.getMethodCall().getMethod(),
                        new HashSet<>()
                    );
                    toMs1.getInterfaceList().add(msAddInterface);
                    // generate interfaceChange
                    MssAtomicChange interfaceChange = new MssAtomicChange(
                        MssAtomicChange.OptType.ADD,
                        MssAtomicChange.OptObj.INTERFACE,
                        toMs.getName(),
                        new HashMap<>(Collections.singletonMap("interface", msAddInterface))
                    );
                    mssAtomicChangeList.add(interfaceChange);
                }
                msAddInterface.getMsNameList().add(fromMs.getName());
                // generate interfaceCall mss change
                MssAtomicChange interfaceCallChange = new MssAtomicChange(
                        MssAtomicChange.OptType.ADD,
                        MssAtomicChange.OptObj.INTERFACE_CALL,
                        toMs.getName(),
                        new HashMap<>(Collections.emptyMap())
                );
                interfaceCallChange.getValueMap().put("fromMs", fromMs);
                interfaceCallChange.getValueMap().put("toMs", toMs1);
                interfaceCallChange.getValueMap().put("interface", msAddInterface);
                mssChanges1.add(interfaceCallChange);
                optionalMssWithChangeList.add(new OptionalMssWithChange(mss1, mssChanges1));


                // choice2: 将 fromClassA 移到 toClassB 所在的服务中
                MicroserviceSystem mss2 = cloner.deepClone(mss);
                Microservice fromMs2 = getMsByMsAndClassName(mss1, fromClassName);
                Microservice toMs2 = getMsByMsAndClassName(mss1, toClassName);
                OptionalMssWithChange optionalMssWithChange1 = moveClassBetweenMsByChange(mss2, fromMs2, toMs2, change, fromClassName, toClassName, true);
                optionalMssWithChangeList.add(optionalMssWithChange1);


                // choice3：将类B加入到类A所在的服务中
                MicroserviceSystem mss3 = cloner.deepClone(mss);
                Microservice fromMs3 = getMsByMsAndClassName(mss3, fromClassName);
                Microservice toMs3 = getMsByMsAndClassName(mss3, toClassName);
                OptionalMssWithChange optionalMssWithChange2 = moveClassBetweenMsByChange(mss3, toMs3, fromMs3, change, toClassName, fromClassName, false);
                optionalMssWithChangeList.add(optionalMssWithChange2);


                // choice4：将类A与类B所在的服务合并为一个服务
                MicroserviceSystem mss4 = cloner.deepClone(mss);
                Microservice fromMs4 = getMsByMsAndClassName(mss4, fromClassName);
                Microservice toMs4 = getMsByMsAndClassName(mss4, toClassName);
                OptionalMssWithChange optionalMssWithChange3 = mergeTwoMs(mss4, fromMs4, toMs4, change);
                optionalMssWithChangeList.add(optionalMssWithChange3);
                break;
            case DELETE:
                MsInterface msInterface = getInterface(toMs, change.getMethodCall().getMethod());
                if (msInterface == null) {
                    optionalMssWithChangeList.add(new OptionalMssWithChange(mss, new ArrayList<>()));
                    return optionalMssWithChangeList;
                }
                msInterface.getMsNameList().remove(fromMs.getName());
                // generate delete MSS atomic change
                MssAtomicChange interfaceCallDeleteChange = new MssAtomicChange(
                        MssAtomicChange.OptType.DELETE,
                        MssAtomicChange.OptObj.INTERFACE_CALL,
                        toMs.getName(),
                        new HashMap<>(Collections.emptyMap())
                );
                interfaceCallDeleteChange.getValueMap().put("fromMs", fromMs);
                interfaceCallDeleteChange.getValueMap().put("toMs", toMs);
                interfaceCallDeleteChange.getValueMap().put("interface", msInterface);
                mssAtomicChangeList.add(interfaceCallDeleteChange);
                // 调用此接口的微服务数量为0，删除此接口
                if (msInterface.getMsNameList().size() == 0) {
                    toMs.getInterfaceList().removeIf(temp -> temp.getMsNameList().size() == 0);
                    // delete interface mss change
                    MssAtomicChange interfaceDeleteChange = new MssAtomicChange(
                            MssAtomicChange.OptType.DELETE,
                            MssAtomicChange.OptObj.INTERFACE,
                            toMs.getName(),
                            new HashMap<>(Collections.singletonMap("interface", msInterface))
                    );
                    mssAtomicChangeList.add(interfaceDeleteChange);
                }

                optionalMssWithChangeList.add(new OptionalMssWithChange(mss, mssAtomicChangeList));
                break;
            default:
        }

        return optionalMssWithChangeList;
    }

    private MsInterface getInterface(Microservice ms, String methodName) {
        if (ms.getInterfaceList() == null) {
            ms.setInterfaceList(new HashSet<>());
            return null;
        }
        for (MsInterface msInterface : ms.getInterfaceList()) {
            if (msInterface.getMethodName().equals(methodName)) {
                return msInterface;
            }
        }
        return null;
    }

    public List<OptionalMssWithChange> changeMssInSimpleRule(UmlAtomicChange change) {
        List<OptionalMssWithChange> optionalMssChange = new ArrayList<>();

        switch (change.getObj()) {
            case CLASS:
                optionalMssChange = classChangeToMss((ClassChange) change);
                break;
            case OBJECT:
                // 时序图中 object 的变化不需要作用在 mss 上
                optionalMssChange.add(new OptionalMssWithChange());
                break;
            case ATTRIBUTE:
                optionalMssChange = attributeChangeToMss((AttributeChange) change);
                break;
            case METHOD:
                optionalMssChange = methodChangeToMss((MethodChange) change);
                break;
            case RELATION:
                optionalMssChange = relationChangeToMssInSimpleRule((RelationShipChange) change);
                break;
            case MESSAGE:
                optionalMssChange = messageChangeToMssInSimpleRule((MessageChange) change);
                break;
            default:
        }
        return optionalMssChange;
    }

    /**
     * class relation change to mss in simple rule
     */
    private List<OptionalMssWithChange> relationChangeToMssInSimpleRule(RelationShipChange change) {
        List<OptionalMssWithChange> optionalMssWithChanges = new ArrayList<>();

        List<MssAtomicChange> mssAtomicChangeList = new ArrayList<>();
        ClassRelation relation = change.getRelation();
        String fromClassName = change.getRelation().getOrigin().getName();
        String toClassName = change.getRelation().getDestination().getName();
        // 找到变化所在类的所属微服务
        Microservice fromMs = getMsByClassName(fromClassName);
        Microservice toMs = getMsByClassName(toClassName);
        if (fromMs == null || toMs == null) {
            throw new IllegalArgumentException("当前变化所属类找不到所在的微服务！" + change);
        }

        // 起点类和终点类在不同服务中，什么也不做
        if (!fromMs.equals(toMs)) {
            optionalMssWithChanges.add(new OptionalMssWithChange(mss, new ArrayList<>()));
            return optionalMssWithChanges;
        }

        // 起点类与终点类在相同服务中
        List<ClassRelation> list = fromMs.getClassRelationList();

        switch(change.getType()) {
            case ADD:
                list.add(change.getRelation());
                MssAtomicChange addChange = new MssAtomicChange(
                        MssAtomicChange.OptType.ADD,
                        MssAtomicChange.OptObj.RELATION,
                        fromMs.getName(),
                        new HashMap<>(Collections.singletonMap("relation", relation))
                );
                mssAtomicChangeList.add(addChange);
                break;
            case UPDATE:
                list.removeIf(classRelation -> classRelation.equals(relation));
                list.add(relation);
                MssAtomicChange updateChange = new MssAtomicChange(
                        MssAtomicChange.OptType.UPDATE,
                        MssAtomicChange.OptObj.RELATION,
                        fromMs.getName(),
                        new HashMap<>(Collections.singletonMap("relation", relation))
                );
                mssAtomicChangeList.add(updateChange);
                break;
            case DELETE:
                list.removeIf(classRelation -> classRelation.equals(relation));
                MssAtomicChange deleteChange = new MssAtomicChange(
                        MssAtomicChange.OptType.DELETE,
                        MssAtomicChange.OptObj.RELATION,
                        fromMs.getName(),
                        new HashMap<>(Collections.singletonMap("relation", relation))
                );
                mssAtomicChangeList.add(deleteChange);
                break;
            default:
        }

        optionalMssWithChanges.add(new OptionalMssWithChange(mss, mssAtomicChangeList));
        return optionalMssWithChanges;
    }

    /**
     * message change to mss in simple rule
     */
    private List<OptionalMssWithChange> messageChangeToMssInSimpleRule(MessageChange change) {
        List<OptionalMssWithChange> optionalMssWithChangeList = new ArrayList<>();
        List<MssAtomicChange> mssAtomicChangeList = new ArrayList<>();
        // 找到消息的起点与终点所在类的所属微服务
        Microservice fromMs = getMsByClassName(change.getMethodCall().getFrom());
        Microservice toMs = getMsByClassName(change.getMethodCall().getTo());
        String fromClassName = change.getMethodCall().getFrom();
        String toClassName = change.getMethodCall().getTo();

        if (fromMs == null || toMs == null) {
            log.warn("当前变化所属类找不到所在的微服务：" + change);
            optionalMssWithChangeList.add(new OptionalMssWithChange(mss, mssAtomicChangeList));
            return optionalMssWithChangeList;
        }
        if (fromMs.equals(toMs)) {
            // 在同一个服务中增加消息传递，MSS 无需任何变化
            optionalMssWithChangeList.add(new OptionalMssWithChange(mss, mssAtomicChangeList));
            return optionalMssWithChangeList;
        }

        switch(change.getType()) {
            case ADD:
                // 增加服务间接口调用
                MicroserviceSystem mss1 = cloner.deepClone(mss);
                List<MssAtomicChange> mssChanges1 = new ArrayList<>();
                Microservice toMs1 = getMsByMsAndClassName(mss1, change.getMethodCall().getTo());
                MsInterface msAddInterface = getInterface(toMs1, change.getMethodCall().getMethod());
                if (msAddInterface == null) {
                    // add interface
                    msAddInterface = new MsInterface(
                            "/" + change.getMethodCall().getMethod(),
                            "POST",
                            change.getMethodCall().getTo(),
                            change.getMethodCall().getMethod(),
                            new HashSet<>()
                    );
                    toMs1.getInterfaceList().add(msAddInterface);
                    // generate interfaceChange
                    MssAtomicChange interfaceChange = new MssAtomicChange(
                            MssAtomicChange.OptType.ADD,
                            MssAtomicChange.OptObj.INTERFACE,
                            toMs.getName(),
                            new HashMap<>(Collections.singletonMap("interface", msAddInterface))
                    );
                    mssAtomicChangeList.add(interfaceChange);
                }
                msAddInterface.getMsNameList().add(fromMs.getName());
                // generate interfaceCall mss change
                MssAtomicChange interfaceCallChange = new MssAtomicChange(
                        MssAtomicChange.OptType.ADD,
                        MssAtomicChange.OptObj.INTERFACE_CALL,
                        toMs.getName(),
                        new HashMap<>(Collections.emptyMap())
                );
                interfaceCallChange.getValueMap().put("fromMs", fromMs);
                interfaceCallChange.getValueMap().put("toMs", toMs1);
                interfaceCallChange.getValueMap().put("interface", msAddInterface);
                mssChanges1.add(interfaceCallChange);
                optionalMssWithChangeList.add(new OptionalMssWithChange(mss1, mssChanges1));
                break;
            case DELETE:
                MsInterface msInterface = getInterface(toMs, change.getMethodCall().getMethod());
                if (msInterface == null) {
                    optionalMssWithChangeList.add(new OptionalMssWithChange(mss, new ArrayList<>()));
                    return optionalMssWithChangeList;
                }
                msInterface.getMsNameList().remove(fromMs.getName());
                // generate delete MSS atomic change
                MssAtomicChange interfaceCallDeleteChange = new MssAtomicChange(
                        MssAtomicChange.OptType.DELETE,
                        MssAtomicChange.OptObj.INTERFACE_CALL,
                        toMs.getName(),
                        new HashMap<>(Collections.emptyMap())
                );
                interfaceCallDeleteChange.getValueMap().put("fromMs", fromMs);
                interfaceCallDeleteChange.getValueMap().put("toMs", toMs);
                interfaceCallDeleteChange.getValueMap().put("interface", msInterface);
                mssAtomicChangeList.add(interfaceCallDeleteChange);
                // 调用此接口的微服务数量为0，删除此接口
                if (msInterface.getMsNameList().size() == 0) {
                    toMs.getInterfaceList().removeIf(temp -> temp.getMsNameList().size() == 0);
                    // delete interface mss change
                    MssAtomicChange interfaceDeleteChange = new MssAtomicChange(
                            MssAtomicChange.OptType.DELETE,
                            MssAtomicChange.OptObj.INTERFACE,
                            toMs.getName(),
                            new HashMap<>(Collections.singletonMap("interface", msInterface))
                    );
                    mssAtomicChangeList.add(interfaceDeleteChange);
                }

                optionalMssWithChangeList.add(new OptionalMssWithChange(mss, mssAtomicChangeList));
                break;
            default:
        }

        return optionalMssWithChangeList;
    }

}
