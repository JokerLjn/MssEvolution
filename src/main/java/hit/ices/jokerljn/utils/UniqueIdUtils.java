package hit.ices.jokerljn.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 20:00
 * @desc use uuid to generate unique id
 */
public class UniqueIdUtils {
    public static String generate() {
        String uuid = UUID.randomUUID().toString().replace("-","");
        return uuid.substring(0,6);
    }

    public static void main(String[] args) {
        Set<String> set = new HashSet<>();
        // 判断生成 1000 次会不会重复
        int times = 1000;
        for (int i=0; i<times; i++) {
            if (!set.add(generate())) {
                System.out.println(false);
            }
        }
    }
}
