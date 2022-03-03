package hit.ices.jokerljn.importer.mss;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 21:30
 * @desc MicroserviceSystem entity
 */
@Getter
@Setter
@NoArgsConstructor
public class MicroserviceSystem {
    private String name;
    private List<Microservice> microservices;

    @Override
    public String toString() {
        StringBuilder data = new StringBuilder();
        data.append("微服务系统：\n");
        data.append("name = ").append(name).append("\n").append("microservices = ").
                append(microservices).append("\n");
        data.append("----------\n");
        return data.toString();
    }
}
