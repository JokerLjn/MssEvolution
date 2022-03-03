package hit.ices.jokerljn.user;

import hit.ices.jokerljn.function_decomposition.Analyze;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2022/1/19 14:20
 * @desc 分析设计人员手工结果
 */
public class UserMainApp {
    private static final String USER1_PATH = "user/jokerljn/";
    private static final String USER2_PATH = "user/user2/";
    private static final String USER3_PATH = "user/user3/";

    public static void analyze(int userNum, int caseNum, int choice) {
        Analyze analyzer = null;
        switch (userNum) {
            case 1 :
                analyzer = new Analyze(USER1_PATH);
                break;
            case 2:
                analyzer = new Analyze(USER2_PATH);
                break;
            case 3:
                analyzer = new Analyze(USER3_PATH);
                break;
            default:
                throw new IllegalArgumentException("输入用户编号错误");
        }
        try {
            analyzer.analyze(caseNum, choice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // user1_case1
//        UserMainApp.analyze(1, 1, 1);
//        UserMainApp.analyze(1, 1, 2);
//        UserMainApp.analyze(1, 1, 3);
        // user1_case2
//        UserMainApp.analyze(1, 2, 1);
//        UserMainApp.analyze(1, 2, 2);
//        UserMainApp.analyze(1, 2, 3);
          // 王腾
//        UserMainApp.analyze(2, 2, 1);
//        UserMainApp.analyze(2, 2, 2);
//        UserMainApp.analyze(2, 2, 3);
          // 宿子航
//        UserMainApp.analyze(3, 2, 1);
//        UserMainApp.analyze(3, 2, 2);
        UserMainApp.analyze(3, 2, 3);
    }
}
