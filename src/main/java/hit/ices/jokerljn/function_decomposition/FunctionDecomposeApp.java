package hit.ices.jokerljn.function_decomposition;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/12/25 17:07
 * @desc
 */
public class FunctionDecomposeApp {

    public void analyze() {
        Analyze analyzer = new Analyze();
        try {
            analyzer.analyze(2, 1);
            analyzer.analyze(2, 2);
            analyzer.analyze(2, 3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        try {
////            GenerateDotUtils.generateDotFile(1, 1);
////            GenerateDotUtils.generateDotFile(1, 2);
////            GenerateDotUtils.generateDotFile(1, 3);
////            GenerateDotUtils.generateDotFile(2, 1);
////            GenerateDotUtils.generateDotFile(2, 2);
////            GenerateDotUtils.generateDotFile(2, 3);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        FunctionDecomposeApp app = new FunctionDecomposeApp();
        app.analyze();

    }
}
