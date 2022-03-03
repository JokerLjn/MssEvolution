package hit.ices.jokerljn.quality;

import hit.ices.jokerljn.importer.mss.Microservice;
import hit.ices.jokerljn.importer.uml.classmodel.ClassDiagram;
import hit.ices.jokerljn.importer.uml.classmodel.ClassRelation;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/16 21:12
 * @desc
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class QualityScore {
    private double cohesionWeight = 0.5;
    private double coupingWeight = 0.5;

    /**
     * compute couping degree of ms divide solution
     *
     * @param microserviceList microserviceList
     */
    public double getCoupingDegree(List<Microservice> microserviceList) {
        double degree = microserviceList.stream().mapToDouble(microservice -> microservice.getInterfaceList().stream().
                mapToDouble(msInterface -> {
                    int msNum = msInterface.getMsNameList().size();
                    return msNum == 0 ? 1 : msNum;
                }).sum()).sum();

        return degree / microserviceList.size();
    }

    /**
     * compute couping degree with class diagram
     *
     * @param microserviceList msList
     * @param classDiagram class diagram
     * @return degree
     */
    public double getCoupingDegree(List<Microservice> microserviceList, ClassDiagram classDiagram) {
        double degree = 0d;
        List<ClassRelation> relationList = classDiagram.getClassRelationList();
        for (Microservice microservice : microserviceList) {
            for (ClassRelation classRelation : microservice.getClassRelationList()) {
                if (relationList.contains(classRelation)) {
                    degree++;
                }
            }
        }

        degree = classDiagram.getClassRelationList().size() - degree;
        return degree / microserviceList.size();
    }

    /**
     * compute cohesion degree of ms divide solution
     *
     * @param microserviceList microserviceList
     * @return cohesion degree
     */
    public double getCohesionDegree(List<Microservice> microserviceList) {
        double degree = microserviceList.stream().mapToDouble(microservice -> {
            if (microservice.getClassRelationList() == null) {
                microservice.setClassRelationList(new ArrayList<>());
            }
            if (microservice.getClassList() == null || microservice.getClassList().size() == 0) {
                return 0;
            }
            return (microservice.getClassRelationList().size() + 1d) / microservice.getClassList().size();
        }).sum();

        degree /= microserviceList.size();
        return degree;
    }

    /**
     * compute quality score only depends on mss
     */
    public double getQualityScoreByMss(List<Microservice> microserviceList) {
        double cohesion = getCohesionDegree(microserviceList);
        double couping = getCoupingDegree(microserviceList);

        return 1 + cohesion * cohesionWeight - couping * coupingWeight;
    }

    /**
     * compute quality score depends on mss and class diagram
     */
    public double getQualityScoreByUml(List<Microservice> microserviceList, ClassDiagram classDiagram) {
        double cohesion = getCohesionDegree(microserviceList);
        double couping = getCoupingDegree(microserviceList, classDiagram);

        return 5 + cohesion * cohesionWeight - couping * coupingWeight;
    }
}
