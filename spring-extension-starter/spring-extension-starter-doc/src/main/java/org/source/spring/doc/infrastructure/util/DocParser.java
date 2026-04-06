package org.source.spring.doc.infrastructure.util;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.source.spring.doc.domain.element.*;
import org.source.spring.doc.domain.object.DocObjectProcessor;
import org.source.spring.doc.domain.object.DocObjectTypeEnum;
import org.source.spring.doc.domain.object.DocValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 统一文档解析器
 * <p>
 * 整合所有解析功能的统一入口，支持：
 * <ul>
 *     <li>JavaDoc 注释解析</li>
 *     <li>REST 注解解析（@GetMapping、@PostMapping 等）</li>
 *     <li>JPA 注解解析（@Entity、@Column 等）</li>
 *     <li>JavaDoc 标签解析（@param、@return 等）</li>
 * </ul>
 * </p>
 * <p>
 * 方法的入参和返回值作为独立的 ParameterVariableElement 解析，
 * 通过元素 ID 和 parentId 维护父子关系。
 * </p>
 * <p>
 * 变量通过共用变量元素（SharedVariableElement）实现共享，
 * 同一变量在多处使用时共享同一个实例。
 * </p>
 * <p>
 * 支持多线程并行解析，提高大规模代码库的解析效率。
 * </p>
 * <p>
 * 解析结果通过 DocObjectProcessor 保存到数据库，支持增量更新和版本管理。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Getter
public class DocParser {

    private final DocObjectProcessor objectProcessor;

    public DocParser(DocObjectProcessor objectProcessor) {
        this.objectProcessor = objectProcessor;
    }

    /**
     * 解析目录并保存到数据库
     * <p>
     * 解析完成后，通过 DocObjectProcessor 保存到数据库。
     * </p>
     *
     * @param directoryPath 目录路径
     * @throws IOException 如果读取失败
     */
    public void parseDirectory(String directoryPath) throws IOException {
        if (objectProcessor == null) {
            throw new IllegalStateException("DocObjectProcessor not configured");
        }

        ModuleParser moduleParser = new ModuleParser();
        List<ModuleDocElement> modules = moduleParser.parseProjectModules(directoryPath);

        List<DocValue> allValues = Collections.synchronizedList(new ArrayList<>());

        modules.parallelStream().forEach(module -> {
            String modulePath = module.getModulePath();
            try {
                DocValue moduleValue = convertElementToValue(module);
                if (moduleValue != null) {
                    allValues.add(moduleValue);
                }

                List<String> javaFiles = collectJavaFiles(modulePath);
                javaFiles.forEach(javaFile -> {
                    List<DocValue> values = parseJavaFileToValues(javaFile, modulePath);
                    allValues.addAll(values);
                });
            } catch (IOException e) {
                System.err.println("Error parsing module: " + modulePath + " - " + e.getMessage());
            }
        });

        if (!allValues.isEmpty()) {
            objectProcessor.save(allValues);
        }
    }

    /**
     * 解析单个 Java 文件并转换为 DocValue 列表
     *
     * @param filePath 文件路径
     * @param modulePath 模块路径
     * @return DocValue 列表
     */
    private List<DocValue> parseJavaFileToValues(String filePath, String modulePath) {
        DocCommentParser localCommentParser = new DocCommentParser();
        JpaAnnotationParser localJpaParser = new JpaAnnotationParser();
        DocTagParser localTagParser = new DocTagParser();
        RestAnnotationParser localRestParser = new RestAnnotationParser();
        List<DocValue> values = new ArrayList<>();
        try {
            Path path = Paths.get(filePath);
            String sourceCode = Files.readString(path);
            String fileName = path.getFileName().toString();
            String className = fileName.replace(".java", "");

            String packageName = extractPackageName(sourceCode);
            String qualifiedName = StringUtils.isBlank(packageName) ? className : packageName + "." + className;

            ClassDocElement classElement = localCommentParser.parseClassDoc(sourceCode, qualifiedName);
            if (classElement != null) {
                classElement = localJpaParser.parseJpaAnnotations(sourceCode, classElement);
                if (modulePath != null) {
                    classElement.setModuleName(modulePath);
                }
                DocValue classValue = convertElementToValue(classElement);
                if (classValue != null) {
                    values.add(classValue);
                }
            }

            List<MethodDocElement> methods = localCommentParser.parseAllMethods(sourceCode, qualifiedName);
            for (MethodDocElement method : methods) {
                if (StringUtils.isNotBlank(method.getDocContent())) {
                    var tags = localTagParser.parseAllTags(method.getDocContent());
                    method.setDocContent((String) tags.getOrDefault("return", method.getDocContent()));
                }
                DocValue methodValue = convertElementToValue(method);
                if (methodValue != null) {
                    values.add(methodValue);
                }

                List<ParameterVariableElement> params = localCommentParser.parseMethodParameters(
                        sourceCode, qualifiedName, method.getMethodName());
                for (ParameterVariableElement param : params) {
                    DocValue paramValue = convertElementToValue(param);
                    if (paramValue != null) {
                        values.add(paramValue);
                    }
                }

                ParameterVariableElement returnValue = localCommentParser.parseMethodReturnValue(
                        sourceCode, qualifiedName, method.getMethodName());
                if (returnValue != null) {
                    DocValue returnValueDoc = convertElementToValue(returnValue);
                    if (returnValueDoc != null) {
                        values.add(returnValueDoc);
                    }
                }
            }

            List<MemberVariableElement> fields = localCommentParser.parseAllMemberVariables(sourceCode, qualifiedName);
            for (MemberVariableElement field : fields) {
                JpaColumnVariableElement jpaColumn = localJpaParser.parseJpaColumnVariable(
                        sourceCode, qualifiedName, field.getVariableName(), field.getSharedVariable());
                DocElement fieldElement = Objects.requireNonNullElse(jpaColumn, field);
                DocValue fieldValue = convertElementToValue(fieldElement);
                if (fieldValue != null) {
                    values.add(fieldValue);
                }
            }

            List<RestDocElement> endpoints = localRestParser.parseRestEndpoints(sourceCode, qualifiedName);
            for (RestDocElement endpoint : endpoints) {
                DocValue endpointValue = convertElementToValue(endpoint);
                if (endpointValue != null) {
                    values.add(endpointValue);
                }
            }

            Collection<SharedVariableElement> sharedVariables = localCommentParser.getSharedVariableCache().values();
            for (SharedVariableElement sharedVar : sharedVariables) {
                DocValue sharedValue = convertElementToValue(sharedVar);
                if (sharedValue != null) {
                    values.add(sharedValue);
                }
            }
        } catch (IOException e) {
            System.err.println("Error parsing file: " + filePath + " - " + e.getMessage());
        }
        return values;
    }

    /**
     * 将单个 DocElement 转换为 DocValue
     *
     * @param element 文档元素
     * @return DocValue，如果转换失败返回 null
     */
    private DocValue convertElementToValue(DocElement element) {
        if (element == null) {
            return null;
        }

        DocValue value = new DocValue();
        value.setObjectId(element.getId());
        value.setName(element.getId());
        value.setRelationType(null);

        if (element instanceof ClassDocElement classElement) {
            convertClassElement(classElement, value);
        } else if (element instanceof MethodDocElement methodElement) {
            convertMethodElement(methodElement, value);
        } else if (element instanceof MemberVariableElement memberElement) {
            convertMemberVariableElement(memberElement, value);
        } else if (element instanceof SharedVariableElement sharedElement) {
            convertSharedVariableElement(sharedElement, value);
        } else if (element instanceof RestDocElement restElement) {
            convertRestElement(restElement, value);
        } else if (element instanceof ModuleDocElement moduleElement) {
            convertModuleElement(moduleElement, value);
        } else if (element instanceof ParameterVariableElement paramElement) {
            convertParameterElement(paramElement, value);
        }

        return value;
    }

    private void convertClassElement(ClassDocElement element, DocValue value) {
        value.setName(element.getClassName());
        value.setSorted(element.getClassName());
    }

    private void convertMethodElement(MethodDocElement element, DocValue value) {
        value.setName(element.getMethodName());
        value.setSorted(element.getMethodName());
    }

    private void convertMemberVariableElement(MemberVariableElement element, DocValue value) {
        value.setName(element.getVariableName());
        value.setSorted(element.getVariableName());

        if (element instanceof JpaColumnVariableElement jpaElement) {
            value.setSorted(jpaElement.getColumnName());
        }
    }

    private void convertSharedVariableElement(SharedVariableElement element, DocValue value) {
        value.setName(element.getVariableName());
        value.setSorted(element.getVariableName());
    }

    private void convertRestElement(RestDocElement element, DocValue value) {
        value.setName(element.getHttpMethod() + ":" + element.getPath());
        value.setSorted(element.getPath());
    }

    private void convertModuleElement(ModuleDocElement element, DocValue value) {
        value.setName(element.getModuleName());
        value.setSorted(element.getModuleName());
    }

    private void convertParameterElement(ParameterVariableElement element, DocValue value) {
        value.setName(element.getVariableName());
        String orderStr = String.valueOf(element.getParameterOrder());
        value.setSorted(orderStr.length() == 1 ? "0" + orderStr : orderStr);
    }

    private List<String> collectJavaFiles(String directoryPath) throws IOException {
        Path srcMainJavaPath = Paths.get(directoryPath, "src", "main", "java");
        if (!Files.exists(srcMainJavaPath)) {
            return Collections.emptyList();
        }
        try (Stream<Path> paths = Files.walk(srcMainJavaPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(string -> string.endsWith(".java"))
                    .toList();
        }
    }

    private String extractPackageName(String sourceCode) {
        for (String line : sourceCode.split("\n")) {
            line = line.trim();
            if (line.startsWith("package ")) {
                int start = "package ".length();
                int end = line.indexOf(";");
                if (end > start) {
                    return line.substring(start, end).trim();
                }
            }
        }
        return "";
    }
}