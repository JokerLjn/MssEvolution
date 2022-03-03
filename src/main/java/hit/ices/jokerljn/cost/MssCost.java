package hit.ices.jokerljn.cost;

import hit.ices.jokerljn.importer.mss.MssAtomicChange;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/14 16:53
 * @desc TODO compute atomic change cost
 */
@Getter
@Setter
@NoArgsConstructor
public class MssCost {
    /**
     * basic weight
     */
    private double MS_WEIGHT = 1.2;
    private double CLASS_WEIGHT = 0.8;
    /**
     * ms 实例 重新打包部署运行的代价
    */
    private double RECONSTRUCT_COST = 1.5;

    public MssCost(double msWeight, double classWeight, double reconstructCost) {
        MS_WEIGHT = msWeight;
        CLASS_WEIGHT = classWeight;
        RECONSTRUCT_COST = reconstructCost;
    }

    /**
     * opt weight
    */
    private final static double ADD_WEIGHT = 4.0;
    private final static double UPDATE_WEIGHT = 2.0;
    private final static double DELETE_WEIGHT = 1.0;
    private final static double MOVE_WEIGHT = 1.5;

    /**
     * class cost
     */
    private final static double CLASS_COST = 1.0;
    private final static double ATTRIBUTE_COST = 0.6;
    private final static double METHOD_COST = 0.8;
    private final static double RELATION_COST = 0.2;

    /**
     * microservice cost
     */
    private final static double MICROSERVICE_COST = 1.0;
    private final static double INTERFACE_COST = 0.8;
    private final static double INTERFACE_CALL_COST = 0.5;

    private double computeCost(MssAtomicChange mssAtomicChange) {
        double cost;
        double weight;
        switch (mssAtomicChange.getType()) {
            case ADD:
                weight = ADD_WEIGHT;
                break;
            case UPDATE:
                weight = UPDATE_WEIGHT;
                break;
            case DELETE:
                weight = DELETE_WEIGHT;
                break;
            case MOVE:
                weight = MOVE_WEIGHT;
                break;
            default:
                weight = 0.0d;
        }
        switch (mssAtomicChange.getObj()) {
            case CLASS:
                cost = CLASS_COST * CLASS_WEIGHT;
                break;
            case ATTRIBUTE:
                cost = ATTRIBUTE_COST * CLASS_WEIGHT;
                break;
            case METHOD:
                cost = METHOD_COST * CLASS_WEIGHT;
                break;
            case RELATION:
                cost = RELATION_COST * CLASS_WEIGHT;
                break;
            case MICROSERVICE:
                cost = MICROSERVICE_COST * MS_WEIGHT;
                break;
            case INTERFACE:
                cost = INTERFACE_COST * MS_WEIGHT;
                break;
            case INTERFACE_CALL:
                cost = INTERFACE_CALL_COST * MS_WEIGHT;
                break;
            default:
                cost = 0;
        }
        return cost * weight;
    }

    public double computeCost(List<MssAtomicChange> mssAtomicChangeList) {
        if (mssAtomicChangeList == null || mssAtomicChangeList.size() == 0) {
            return 0;
        }
        double ans = 0;
        Set<String> msNameSet = new HashSet<>();
        for (MssAtomicChange mssAtomicChange : mssAtomicChangeList) {
            ans += computeCost(mssAtomicChange);
            msNameSet.add(mssAtomicChange.getMsName());
            if (mssAtomicChange.getType() == MssAtomicChange.OptType.MOVE) {
                msNameSet.add((String) mssAtomicChange.getValueMap().get("fromMS"));
                msNameSet.add((String) mssAtomicChange.getValueMap().get("toMS"));
            }
        }
        return ans + msNameSet.size() * RECONSTRUCT_COST;
    }
}
