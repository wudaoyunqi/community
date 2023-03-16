package com.nowcoder.community;

/**
 * @Projectname: community
 * @Filename: WkTests
 * @Author: yunqi
 * @Date: 2023/3/10 13:07
 * @Description: TODO
 */
public class WkTests {

    public static void main(String[] args) {
        String cmd = "D:/ProgramFiles/wkhtmltopdf/bin/wkhtmltoimage -- quality 75 https://www.nowcoder.com D:\\ProgramFiles\\data\\wk-images\\1.jpg";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("OK");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
