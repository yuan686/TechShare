package com.ruoyi;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @ClassName ChangePicPath
 * @Description 修改指定文件夹中的所有信息
 * @Author faro_z
 * @Date 2022/3/26 5:18 下午
 * @Version 1.0
 **/
public class ChangePicPath {
    private static String folderName = "D:\\桌面\\Jenkins部署文档";
    private static String from = "Jenkins部署文档.assets";
    private static String to = "/Jenkins部署文档.assets";
    private static List<String> errFileNameList = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
        List<String> pathList = getAllFileName(folderName);
        System.out.println("成功获取所有文件名称！");
        System.out.println("第一个文件名称为:"+pathList.get(0));
        // 如果发现文件名称和预期的不一样，别误操作了
        System.out.println("输入任意数字，再按回车键继续...");
        new Scanner(System.in).nextInt();
        int errCount = 0;
        for (String path : pathList) {
            if (!changeFileContent(from,to,path)) {
                errCount++;
            }
        }
        System.out.println("出错文件数为："+errCount);
        System.out.println("所有出错文件名为:");
        for (String errPath : errFileNameList) {
            System.out.println(errPath);
        }
    }

    /**
     * 获取当前文件夹下所有文件名（不包括文件夹名）
     * @param folderName
     * @return
     */
    private static List<String> getAllFileName(String folderName) {
        ArrayList<String> filePathList = new ArrayList<String>();
        dfs(folderName,filePathList);
        return filePathList;
    }

    /**
     * 递归获取文件名
     * @param path
     * @param filePathList
     */
    private static void dfs(String path, List<String>filePathList) {
        File file = new File(path);
        if (file.isFile() && file.getName().endsWith(".md")) {
            filePathList.add(file.getAbsolutePath());
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File tmpFile : files) {
                dfs(tmpFile.getAbsolutePath(),filePathList);
            }
        }
    }

    private static boolean changeFileContent(String from,String to,String filePath) {
        File file = new File(filePath);
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            CharArrayWriter tempStream = new CharArrayWriter();

            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.replaceAll(from,to);
                tempStream.write(line);
                tempStream.append(System.getProperty("line.separator"));
            }

            bufferedReader.close();
            // 将内存中的流 写入 文件
            FileWriter out = new FileWriter(file);
            tempStream.writeTo(out);
            out.close();
        } catch (IOException e) {
            errFileNameList.add(filePath);
            return false;
        }
        return true;
    }
}


