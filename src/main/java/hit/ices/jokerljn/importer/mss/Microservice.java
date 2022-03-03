package hit.ices.jokerljn.importer.mss;

import hit.ices.jokerljn.importer.uml.classmodel.ClassRelation;
import hit.ices.jokerljn.importer.uml.classmodel.UmlClass;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 21:30
 * @desc microservice entity
 */
@Data
@NoArgsConstructor
public class Microservice {
    private String name;
    private List<UmlClass> classList;
    private List<ClassRelation> classRelationList;
    private Set<MsInterface> interfaceList;

    public Microservice(String name, List<UmlClass> classList) {
        this.name = name;
        this.classList = classList;
        this.classRelationList = new ArrayList<>();
        this.interfaceList = new HashSet<>();
    }

    @Override
    public String toString() {
        return "\n微服务" + name + "\n\t classList = " + classList +
                "\n\t classRelationList = " + classRelationList +
                "\n\t interfaceList = " + interfaceList +
                "\n";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Microservice) {
            return ((Microservice) obj).getName().equals(name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
