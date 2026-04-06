# 技术规格文档

## 1. 数据模型规格

### 1.1 Element 继承体系

#### DocElement (抽象基类)

```java
/**
 * 文档元素抽象基类
 * <p>
 * 所有文档元素的父类，定义统一的接口规范
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class DocElement extends EnhanceElement<String> {
    
    /**
     * 比较两个文档元素
     *
     * @param other 另一个文档元素
     * @return 比较结果
     */
    public int compareTo(DocElement other) {
        return this.getId().compareTo(other.getId());
    }
}
```

#### ClassDocElement (类doc元素)

```java
/**
 * 类文档元素
 * <p>
 * 表示 Java 类或接口的文档信息
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClassDocElement extends DocElement {
    
    /**
     * 类名（不含包名）
     */
    private String className;
    
    /**
     * 类的全限定名（含包名）
     */
    private String classQualifiedName;
    
    /**
     * 类修饰符（public, abstract, final等）
     */
    private String modifiers;
    
    /**
     * 类的文档注释内容
     */
    private String docContent;
    
    /**
     * 是否为 JPA 实体类
     */
    private boolean isEntity;
    
    /**
     * JPA 表名（仅当 isEntity 为 true 时有效）
     */
    private String tableName;
    
    /**
     * 所属模块名称（模块路径）
     */
    private String moduleName;
    
    @Override
    public @NonNull String getId() {
        return classQualifiedName;
    }
    
    @Override
    public String getParentId() {
        return moduleName;
    }
}
```

#### MethodDocElement (方法doc元素)

```java
/**
 * 方法文档元素
 * <p>
 * 表示 Java 方法的文档信息
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
     * 返回值类型
     */
    private String returnType;
    
    /**
     * 返回值类型的全限定名
     */
    private String returnTypeQualifiedName;
    
    /**
     * 所属类的全限定名
     */
    private String classQualifiedName;
    
    /**
     * 方法的文档注释内容
     */
    private String docContent;
    
    /**
     * 方法参数列表
     */
    private List<ParamDocElement> parameters;
    
    @Override
    public @NonNull String getId() {
        return classQualifiedName + "#" + methodName;
    }
    
    @Override
    public String getParentId() {
        return classQualifiedName;
    }
}
```

#### FieldDocElement (字段doc元素)

```java
/**
 * 字段文档元素
 * <p>
 * 表示 Java 字段的文档信息
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FieldDocElement extends DocElement {
    
    /**
     * 字段名
     */
    private String fieldName;
    
    /**
     * 字段类型
     */
    private String fieldType;
    
    /**
     * 字段类型的全限定名
     */
    private String fieldTypeQualifiedName;
    
    /**
     * 所属类的全限定名
     */
    private String classQualifiedName;
    
    /**
     * 字段的文档注释内容
     */
    private String docContent;
    
    /**
     * JPA 列名（仅当字段有 @Column 注解时有效）
     */
    private String columnName;
    
    /**
     * 是否为主键字段
     */
    private boolean isPrimaryKey;
    
    @Override
    public @NonNull String getId() {
        return classQualifiedName + "." + fieldName;
    }
    
    @Override
    public String getParentId() {
        return classQualifiedName;
    }
}
```

#### ModuleDocElement (模块元素)

```java
/**
 * 模块文档元素
 * <p>
 * 表示 Maven 模块的文档信息
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ModuleDocElement extends DocElement {
    
    /**
     * 模块名称（从 pom.xml 的 artifactId 获取）
     */
    private String moduleName;
    
    /**
     * 模块路径（pom.xml 所在目录）
     */
    private String modulePath;
    
    /**
     * 父模块名称（可选）
     */
    private String parentModuleName;
    
    /**
     * 父模块路径（可选）
     */
    private String parentModulePath;
    
    /**
     * 是否为 SpringBoot 模块
     */
    private boolean isSpringBootModule;
    
    @Override
    public @NonNull String getId() {
        return modulePath;
    }
    
    @Override
    public String getParentId() {
        if (StringUtils.isNotBlank(parentModulePath)) {
            return parentModulePath;
        }
        return "";
    }
}
```

### 1.2 数据库相关模型

#### ColumnInfo (数据库列信息)

```java
/**
 * 数据库列信息
 * <p>
 * 存储数据库表的列元数据
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnInfo {
    
    /**
     * 列名
     */
    private String columnName;
    
    /**
     * 数据类型
     */
    private String dataType;
    
    /**
     * 是否为主键
     */
    private boolean isPrimaryKey;
    
    /**
     * 是否可空
     */
    private boolean isNullable;
    
    /**
     * 列大小
     */
    private int columnSize;
    
    /**
     * 备注
     */
    private String remarks;
}
```

---

## 2. API 规格

### 2.1 DocParser API

```java
/**
 * 统一文档解析器
 * <p>
 * 整合所有解析功能的统一入口
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class DocParser {
    
    /**
     * 解析整个目录
     * <p>
     * 自动检测并建立完整的层级结构：Module → Class → Method → Field
     * </p>
     *
     * @param directoryPath 目录路径
     * @return 包含所有解析结果的文档树
     * @throws IOException 如果目录读取失败
     */
    public DocEnhanceTree parseDirectory(String directoryPath) throws IOException;
    
    /**
     * 解析单个 Java 文件
     *
     * @param filePath 文件路径
     */
    public void parseJavaFile(String filePath);
    
    /**
     * 获取文档树
     *
     * @return 文档树实例
     */
    public DocEnhanceTree getDocTree();
}
```

### 2.2 ModuleParser API

```java
/**
 * 模块解析器
 * <p>
 * 解析 Maven 模块结构，建立模块层级关系
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class ModuleParser {
    
    /**
     * 解析项目根目录下的所有模块
     *
     * @param projectRootPath 项目根路径
     * @return 模块元素列表
     * @throws IOException 如果目录读取失败
     */
    public List<ModuleDocElement> parseProjectModules(String projectRootPath) throws IOException;
    
    /**
     * 根据类的全限定名查找所属模块
     *
     * @param classQualifiedName 类的全限定名
     * @param modules 模块列表
     * @return 所属模块元素，如果未找到则返回 null
     */
    public ModuleDocElement findModuleByClassQualifiedName(String classQualifiedName, List<ModuleDocElement> modules);
    
    /**
     * 查找模块树的根节点（非 SpringBoot 模块）
     *
     * @param modules 模块列表
     * @return 根模块元素，如果未找到则返回 null
     */
    public ModuleDocElement findRootModule(List<ModuleDocElement> modules);
}
```

### 2.3 DocEnhanceTree API

```java
/**
 * 文档增强树
 * <p>
 * 管理文档元素的树形结构，支持多父节点DAG
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class DocEnhanceTree {
    
    /**
     * 添加单个元素到树中
     *
     * @param element 文档元素
     */
    public void addElement(DocElement element);
    
    /**
     * 批量添加元素到树中
     *
     * @param elements 元素列表
     */
    public void addElements(List<DocElement> elements);
    
    /**
     * 根据 ID 获取节点
     *
     * @param id 元素ID
     * @return 节点实例，如果未找到则返回 null
     */
    public EnhanceNode<String, DocElement> getById(String id);
    
    /**
     * 查找符合条件的所有节点
     *
     * @param predicate 条件谓词
     * @return 符合条件的节点列表
     */
    public List<EnhanceNode<String, DocElement>> find(Predicate<EnhanceNode<String, DocElement>> predicate);
    
    /**
     * 获取所有类元素
     *
     * @return 类元素列表
     */
    public List<ClassDocElement> getClasses();
    
    /**
     * 获取所有方法元素
     *
     * @return 方法元素列表
     */
    public List<MethodDocElement> getMethods();
    
    /**
     * 获取所有字段元素
     *
     * @return 字段元素列表
     */
    public List<FieldDocElement> getFields();
    
    /**
     * 获取所有 REST 接口元素
     *
     * @return REST接口元素列表
     */
    public List<RestDocElement> getRestEndpoints();
}
```

---

## 3. 性能规格

### 3.1 并发处理

- **并行策略**: 使用 `parallelStream()` 进行并行解析
- **线程安全**: 每个线程创建独立的解析器实例
- **批量处理**: 使用 `Collections.synchronizedList` 收集结果

### 3.2 性能指标

| 操作 | 时间复杂度 | 说明 |
|------|-----------|------|
| parseDirectory() | O(n·m) | n=文件数，m=平均解析时间 |
| getById() | O(1) | 按ID查询 |
| find() | O(n) | 遍历所有节点 |
| addElement() | O(1) | 添加单个元素 |
| addElements() | O(n) | 批量添加元素 |

### 3.3 内存占用

- **单个Element**: 约 200-500 bytes
- **树结构开销**: 约 100 bytes/node
- **预估**: 10000个文件 ≈ 10MB 内存

---

## 4. 异常处理规格

### 4.1 异常类型

```java
/**
 * 文档解析异常
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Getter
public class DocParseException extends RuntimeException {
    
    private final String filePath;
    private final String operation;
    
    public DocParseException(String message, String filePath, String operation) {
        super(message);
        this.filePath = filePath;
        this.operation = operation;
    }
    
    public DocParseException(String message, String filePath, String operation, Throwable cause) {
        super(message, cause);
        this.filePath = filePath;
        this.operation = operation;
    }
}
```

### 4.2 异常处理策略

| 异常场景 | 处理策略 | 日志级别 |
|---------|---------|---------|
| 文件读取失败 | 记录错误，跳过该文件 | ERROR |
| JavaParser解析失败 | 记录警告，跳过该文件 | WARN |
| 模块识别失败 | 使用默认值，继续解析 | WARN |
| 数据库连接失败 | 记录错误，禁用数据库功能 | ERROR |

---

## 5. 测试规格

### 5.1 单元测试覆盖率

- **目标**: 80%+ 代码覆盖率
- **关键路径**: 100% 覆盖率
- **异常路径**: 70%+ 覆盖率

### 5.2 测试分类

| 测试类型 | 数量 | 说明 |
|---------|------|------|
| Element 创建测试 | 7 | 验证元素创建和字段 |
| 解析器测试 | 10 | 验证注释解析功能 |
| 标签解析测试 | 10 | 验证标签解析功能 |
| REST接口测试 | 10 | 验证REST注解解析 |
| 模块解析测试 | 10 | 验证模块层级结构 |
| 集成测试 | 3 | 端到端测试 |

### 5.3 测试数据

- **使用真实代码**: src/main/java 下的实际代码
- **覆盖场景**: 
  - 普通类、接口、枚举
  - JPA实体类
  - REST控制器
  - 复杂继承关系
  - 多模块项目

---

## 6. 依赖规格

### 6.1 核心依赖

```xml
<dependencies>
    <!-- Java Parser -->
    <dependency>
        <groupId>com.github.javaparser</groupId>
        <artifactId>javaparser-symbol-solver-core</artifactId>
        <version>3.26.1</version>
    </dependency>
    
    <!-- Utility Library -->
    <dependency>
        <groupId>io.github.dao1230source</groupId>
        <artifactId>utility</artifactId>
        <version>0.0.12</version>
    </dependency>
    
    <!-- Null Safety -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-collections4</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
    
    <!-- SLF4J -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
    </dependency>
</dependencies>
```

### 6.2 测试依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

*创建时间: 2026-04-05*
*版本: 1.0.0*