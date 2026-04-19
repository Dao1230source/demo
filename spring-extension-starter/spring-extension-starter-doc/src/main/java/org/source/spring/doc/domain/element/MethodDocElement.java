package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.util.Map;

import java.util.Map;

/**
 * 方法文档元素
 * <p>
 * 表示 Java 方法的文档信息，包含方法名、返回类型、
 * 返回类型的全限定名、JavaDoc 注释内容以及所属类的全限定名。
 * </p>
 * <p>
 * 方法的入参和返回值通过 {@link org.source.spring.doc.domain.tree.DocEnhanceTree} 
 * 维护父子关系，不在此类中直接存储。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MethodDocElement extends DocElement {

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 返回类型（简单名称）
     */
    private String returnType;

    /**
     * 返回类型的全限定名
     */
    private String returnTypeQualifiedName;

    /**
     * JavaDoc 注释内容
     */
    private String docContent;

    /**
     * 所属类的全限定名
     */
    private String classQualifiedName;

    /**
     * 方法参数类型列表
     * <p>
     * 格式：参数类型用逗号分隔，如 "String,int,List&lt;User&gt;"
     * 用于区分重载方法，使方法 ID 唯一
     * </p>
     */
    private String parameterTypes;

    /**
     * 是否为构造函数
     */
    private Boolean isConstructor;

    /**
     * 获取元素的唯一标识
     * <p>
     * 格式：类全限定名#方法名(参数类型列表)
     * 例如：com.example.UserService#getUser(String) 或 com.example.UserService#getUser(Long)
     * </p>
     *
     * @return 方法的唯一标识
     */
    @Override
    public @NonNull String getId() {
        String base = classQualifiedName + "#" + methodName;
        if (parameterTypes != null && !parameterTypes.isEmpty()) {
            return base + "(" + parameterTypes + ")";
        }
        return base;
    }

    /**
     * 获取父元素 ID
     * <p>
     * 返回所属类的全限定名
     * </p>
     *
     * @return 所属类的全限定名
     */
    @Override
    public String getParentId() {
        return classQualifiedName;
    }
}