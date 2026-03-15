package org.source.spring.doc.infrastructure.util;

import org.springframework.boot.system.ApplicationHome;

import java.io.File;

public class Test {
    public static void main(String[] args) {
        ApplicationHome home = new ApplicationHome(Test.class);
        File jarFile = home.getSource();  // 获取 JAR 文件路径（打包后有效）
        System.out.printf("jarFile: %s\n", jarFile.getAbsolutePath());
        System.out.printf("javaHome: %s\n", home.getDir());
        String modulePath = home.getDir().getAbsolutePath();  // 获取模块根目录
        System.out.println("Module Path: " + modulePath);
    }
}
