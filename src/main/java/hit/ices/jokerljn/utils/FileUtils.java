package hit.ices.jokerljn.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jokerLjn
 * @version 1.0
 * @date 2021/11/3 19:53
 * @desc handle read and write file
 */
@Slf4j
public class FileUtils {
    /**
     * 读取文件得到字符串
     *
     * @param filePath 文件路径
     * @return 字符串
     */
    public static String readFile(String filePath) throws IOException, URISyntaxException {

        if (filePath==null || filePath.length() == 0) {
            log.error("文件路径不能为空");
            throw new NullPointerException("文件路径不能为空！");
        }

        // 获取资源的绝对路径的url
        URL url = FileUtils.class.getClassLoader().getResource(filePath);

        if (url == null) {
            log.error("{} 路径错误，或路径指向的文件不存在", filePath);
            throw new NullPointerException("文件路径错误或未找到此文件: " + filePath);
        }

        // 将url转为uri再转为path
        Path resPath = Paths.get(url.toURI());
        String fileString = new String(Files.readAllBytes(resPath), StandardCharsets.UTF_8);

        log.info("路径为 {} 的文件读取成功", filePath);
        return fileString;
    }

    /**
     * 读取 path 目录下的所有文件数据
     */
    public static Map<String, String> readAllFilesBehindPath(String path) throws IOException {
        File file = new File(path);
        File[] files = file.listFiles();
        Map<String, String> dataMap = new HashMap<>();
        if (files == null) {
            throw new IllegalArgumentException("当前目录下文件为空: " + file);
        }
        for (File f : files) {
            if(f.isFile()) {
                dataMap.put(delSuffix(f.getName()), readFile(f));
            }
        }

        return dataMap;
    }

    /**
     * 删除文件名中的后缀
     */
    public static String delSuffix(String fileName) {
        int index = fileName.lastIndexOf(".");
        return fileName.substring(0, index);
    }

    /**
     * 读取指定目录下的文件
     * @param path 文件的路径
     * @return 文件内容
     */
    public static String readFile(File path) throws IOException{
        //创建一个输入流对象
        InputStream is = new FileInputStream(path);
        //定义一个缓冲区
        byte[] bytes = new byte[1024];
        //通过输入流使用read方法读取数据
        int len = is.read(bytes);
        //System.out.println("字节数:"+len);
        StringBuilder builder = new StringBuilder();
        while(len!=-1){
            //把数据转换为字符串
            String str = new String(bytes, 0, len);
            builder.append(str);
            //System.out.println(str);
            //继续进行读取
            len = is.read(bytes);
        }
        //释放资源
        is.close();
        return builder.toString();
    }

    /**
     * 覆盖写文件
     *
     * @param pathString 要写文件的路径(从 src 开始的全路径)
     * @param data write data
     * @param name file name
     */
    public static void writeToFile(String pathString, String data, String name) {
        String allPathString = pathString + "\\" + name;
        Path path = Paths.get(allPathString);
        // 使用newBufferedWriter创建文件并写文件
        // 这里使用了try-with-resources方法来关闭流，不用手动关闭
        try (BufferedWriter writer =
                     Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(data);
            log.info("文件 {} 写入成功！", allPathString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 追加写文件
     *
     * @param pathString 要写文件的路径(从 src 开始的全路径)
     * @param data write data
     * @param name file name
     */
    public static void writeAppendToFile(String pathString, String data, String name) {
        String allPathString = pathString + "\\" + name;
        Path path = Paths.get(allPathString);
        // 使用newBufferedWriter创建文件并写文件
        // 追加写模式
        // 这里使用了try-with-resources方法来关闭流，不用手动关闭
        try (BufferedWriter writer =
                     Files.newBufferedWriter(path,
                             StandardCharsets.UTF_8,
                             StandardOpenOption.APPEND)){
            writer.write(data);
            log.info("文件 {} 写入成功！", allPathString);
        } catch (IOException e) {
            // 文件不存在， 创建
            writeToFile(pathString, data, name);
        }


    }

    /**
     * delete all files under path
     *
     * @param path path
     * @return true: success, false: fail
     */
    public static boolean delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        if (!file.isDirectory()) {
            return false;
        }
        String[] tempList = file.list();
        if (tempList == null) {
            throw new IllegalArgumentException("目录为空！");
        }

        File temp = null;

        for (String s : tempList) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + s);
            } else {
                temp = new File(path + File.separator + s);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
//                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
//                delFolder(path + "/" + tempList[i]);//再删除空文件夹
//                flag = true;
                throw new IllegalArgumentException("目录内不应包含文件夹！");

            }
        }
        log.info("路径 {} 下的文件删除成功！", path);
        return true;
    }

    /**
     *
     * @param filePath file path like src/main/resources/output/case1/result.txt
     */
    public static boolean delFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }
}
