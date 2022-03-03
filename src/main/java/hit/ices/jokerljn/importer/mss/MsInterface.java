package hit.ices.jokerljn.importer.mss;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/6 16:00
 * @desc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MsInterface {
    private String url;
    private String type;
    private String className;
    private String methodName;
    private Set<String> msNameList;

    public MsInterface(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
        this.msNameList = new HashSet<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MsInterface) {
            MsInterface msInterface = (MsInterface) obj;
            return this.className.equals(msInterface.getClassName()) && this.methodName.equals(msInterface.getMethodName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName);
    }
}
