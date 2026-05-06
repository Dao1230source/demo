package org.source.spring.doc.infrastructure.util;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 解析报告
 */
@Data
public class DocParserReport {

    private long startTime;
    private long endTime;
    private long duration;
    private int totalFiles;
    private int totalClasses;
    private int totalMethods;
    private int totalFields;
    private int totalParameters;
    private int totalEndpoints;
    private int totalModules;
    private int totalSpringBeans;
    private int totalSharedVariables;
    private int classesWithoutJavaDoc;
    private int methodsWithoutJavaDoc;
    private int fieldsWithoutJavaDoc;
    private int failedFiles;
    private Map<String, String> failedFilesList = new HashMap<>();

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public void end() {
        this.endTime = System.currentTimeMillis();
        this.duration = endTime - startTime;
    }

    /**
     * 增加文件数量
     */
    public void addFiles(int count) { this.totalFiles += count; }

    /**
     * 增加类数量
     */
    public void addClasses(int count) { this.totalClasses += count; }

    /**
     * 增加方法数量
     */
    public void addMethods(int count) { this.totalMethods += count; }

    /**
     * 增加字段数量
     */
    public void addFields(int count) { this.totalFields += count; }

    /**
     * 增加参数数量
     */
    public void addParameters(int count) { this.totalParameters += count; }

    /**
     * 增加REST接口数量
     */
    public void addEndpoints(int count) { this.totalEndpoints += count; }

    /**
     * 增加模块数量
     */
    public void addModules(int count) { this.totalModules += count; }

    /**
     * 增加Spring Bean数量
     */
    public void addSpringBeans(int count) { this.totalSpringBeans += count; }

    /**
     * 增加共用变量数量
     */
    public void addSharedVariables(int count) { this.totalSharedVariables += count; }

    /**
     * 增加缺少JavaDoc的类数量
     */
    public void addClassesWithoutJavaDoc(int count) { this.classesWithoutJavaDoc += count; }

    /**
     * 增加缺少JavaDoc的方法数量
     */
    public void addMethodsWithoutJavaDoc(int count) { this.methodsWithoutJavaDoc += count; }

    /**
     * 增加缺少JavaDoc的字段数量
     */
    public void addFieldsWithoutJavaDoc(int count) { this.fieldsWithoutJavaDoc += count; }

    public void addFailedFile(String filePath, String error) {
        this.failedFiles++;
        this.failedFilesList.put(filePath, error);
    }

    public String generateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 文档解析报告 ===\n");
        sb.append("解析耗时: ").append(duration).append(" ms\n");
        sb.append("文件总数: ").append(totalFiles).append("\n");
        sb.append("模块总数: ").append(totalModules).append("\n");
        sb.append("类总数: ").append(totalClasses).append(" (缺少JavaDoc: ").append(classesWithoutJavaDoc).append(")\n");
        sb.append("方法总数: ").append(totalMethods).append(" (缺少JavaDoc: ").append(methodsWithoutJavaDoc).append(")\n");
        sb.append("字段总数: ").append(totalFields).append(" (缺少JavaDoc: ").append(fieldsWithoutJavaDoc).append(")\n");
        sb.append("参数总数: ").append(totalParameters).append("\n");
        sb.append("REST接口: ").append(totalEndpoints).append("\n");
        sb.append("Spring Bean: ").append(totalSpringBeans).append("\n");
        sb.append("共用变量: ").append(totalSharedVariables).append("\n");
        if (failedFiles > 0) {
            sb.append("失败文件: ").append(failedFiles).append("\n");
        }
        sb.append("===================");
        return sb.toString();
    }
}