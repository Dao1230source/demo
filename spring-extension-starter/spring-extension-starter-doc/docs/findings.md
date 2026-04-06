# 研究发现

## ⚠️ coding-rules 核心规范（强制）

### 1. 注释规范（最高优先级）
- **所有公共类、接口、方法必须有 JavaDoc 注释**
- **禁止使用尾行注释**
- JavaDoc 必须包含：类说明、@author、@since、@param、@return 等

### 2. Lombok 注解规范
- 使用 @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
- **继承父类的子类必须添加 @EqualsAndHashCode(callSuper = true)**
- **重写接口方法必须添加 @NonNull 注解**

### 3. 判空处理规范
- 字符串：StringUtils.isNotBlank()
- 集合：CollectionUtils.isNotEmpty()
- Map：MapUtils.isNotEmpty()

### 4. 日志规范
- 使用 @Slf4j 注解
- **禁止使用 System.out.println()**

### 5. 代码质量标准
- Sonar: 无 blocker/critical/major 问题
- Alibaba Java Coding Guidelines
- 方法长度 ≤ 60 行
- 圈复杂度 ≤ 15

---

## JavaParser 使用研究

### 核心API

1. **JavaParser 类**: 主要解析入口
   ```java
   JavaParser parser = new JavaParser();
   ParseResult<CompilationUnit> result = parser.parse(sourceCode);
   ```

2. **CompilationUnit**: 编译单元，包含整个文件内容
   - `getTypes()`: 获取所有类型声明（类、接口、枚举）
   - `getComments()`: 获取所有注释

3. **Doc注释解析**:
   - `node.getJavadoc()`: 获取Javadoc注释
   - `node.getComment()`: 获取行尾注释和块注释

### 支持解析的元素

| 元素类型 | 获取方法 | 说明 |
|----------|----------|------|
| 类/接口 | `compilationUnit.getClassByName()` | 类的doc注释 |
| 方法 | `class.getMethods()` | 方法的doc注释 |
| 字段 | `class.getFields()` | 字段的doc注释 |
| 构造方法 | `class.getConstructors()` | 构造方法的doc注释 |
| 内部类 | `class.getInnerClasses()` | 内部类的doc注释 |

### 注解解析

```java
// 获取节点上的注解
NodeList<AnnotationExpr> annotations = bodyDeclaration.getAnnotations();

// 注解类型判断
if (annotation instanceof MarkerAnnotationExpr) {
    // 无参数注解，如 @Override
} else if (annotation instanceof SingleMemberAnnotationExpr) {
    // 单参数注解，如 @Service("xxx")
} else if (annotation instanceof NormalAnnotationExpr) {
    // 多参数注解，如 @RequestMapping(value="/", method=GET)
}
```

---

## Tree库 集成研究

### EnhanceElement 接口

需要实现以下方法：
```java
public interface EnhanceElement<I> extends Element<I> {
    I getId();
    I getParentId();
    int compareTo(EnhanceElement<I> o);
}
```

### EnhanceNode 使用

```java
// 创建树
Tree<String, DocElement, EnhanceNode<String, DocElement>> tree = 
    Tree.of(new EnhanceNode<>());

// 添加元素
tree.add(elements);

// 查询
EnhanceNode<String, DocElement> node = tree.getById(id);
List<EnhanceNode<String, DocElement>> parents = node.findParents();
```

---

## 常见注解解析

### Spring 注解
- `@Controller`, `@RestController`
- `@Service`, `@Repository`, `@Component`
- `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- `@RequestParam`, `@RequestBody`, `@PathVariable`
- `@Autowired`, `@Qualifier`

### JPA 注解
- `@Entity`, `@Table`
- `@Id`, `@GeneratedValue`
- `@Column`, `@JoinColumn`
- `@OneToOne`, `@OneToMany`, `@ManyToOne`, `@ManyToMany`

### Lombok 注解
- `@Data`, `@Getter`, `@Setter`
- `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- `@Slf4j`, `@Log4j`

### 验证注解
- `@NotNull`, `@NotBlank`, `@NotEmpty`
- `@Size`, `@Min`, `@Max`
- `@Email`, `@Pattern`

---

## Doc注释标签解析

| 标签 | 说明 | 解析方法 |
|------|------|----------|
| `@param` | 方法参数 | `Javadoc.getParam(paramName)` |
| `@return` | 返回值 | `Javadoc.getReturnDescription()` |
| `@throws` | 异常 | `Javadoc.getThrows()` |
| `@see` | 参见 | `Javadoc.getSeeAlso()` |
| `@link` | 链接 | 需要从文本中提取 |
| `@author` | 作者 | `Javadoc.getAuthor()` |
| `@version` | 版本 | `Javadoc.getVersion()` |
| `@since` | 起始版本 | `Javadoc.getSince()` |

---

## 项目现有代码分析

### JavaParserUtil 现有功能

1. `parseAndPrintCommentsAndAnnotations(String filePath)` - 解析单个文件
2. `parseDirectory(String directoryPath)` - 解析整个目录
3. 提取注释：`extractAndPrintComments(Node node)` - 递归提取所有注释
4. 提取注解：`extractAndPrintAnnotations(Node node)` - 递归提取所有注解

### 现有代码局限性

1. 仅打印日志，未存储结构化数据
2. 未建立层级关系
3. 未支持Tree库集成
4. 未输出到Markdown文件

---

## JPA 实体注解解析

### 核心注解

| 注解 | 位置 | 说明 |
|------|------|------|
| `@Entity` | 类 | 标记为JPA实体 |
| `@Table(name="xxx")` | 类 | 指定映射的数据库表名 |
| `@Id` | 字段 | 主键字段 |
| `@Column(name="xxx")` | 字段 | 列名映射 |
| `@GeneratedValue` | 字段 | 主键生成策略 |
| `@Transient` | 字段 | 不持久化 |

### 关系注解

| 注解 | 说明 |
|------|------|
| `@OneToOne` | 一对一关系 |
| `@OneToMany` | 一对多关系 |
| `@ManyToOne` | 多对一关系 |
| `@ManyToMany` | 多对多关系 |
| `@JoinColumn` | 外键列 |

### 示例（来自项目）

```java
// UserEntity.java
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
}

// OrderEntity.java
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    private String id;
    private String productName;
    private Integer quantity;
    private Double price;
}
```

---

## MyBatis 注解解析

### 核心注解

| 注解 | 说明 |
|------|------|
| `@Select` | 查询SQL |
| `@Insert` | 插入SQL |
| `@Update` | 更新SQL |
| `@Delete` | 删除SQL |
| `@Results` | 结果映射 |
| `@Result` | 单个结果映射 |
| `@One` | 一对一嵌套查询 |
| `@Many` | 一对多嵌套查询 |
| `@MapKey` | 结果映射为Map |

### @Result 示例

```java
@Results({
    @Result(id = true, column = "id", property = "id"),
    @Result(column = "username", property = "username"),
    @Result(column = "email", property = "email")
})
@Select("SELECT * FROM users WHERE id = #{id}")
UserEntity findById(Long id);
```

---

## 数据库连接与元数据

### JDBC获取表结构

```java
// 获取数据库连接
Connection conn = DriverManager.getConnection(url, user, password);

// 获取表元数据
DatabaseMetaData metaData = conn.getMetaData();

// 获取表的所有列
ResultSet columns = metaData.getColumns(null, null, "users", null);
while (columns.next()) {
    String columnName = columns.getString("COLUMN_NAME");
    String columnType = columns.getString("TYPE_NAME");
    int dataType = columns.getInt("DATA_TYPE");
    String isNullable = columns.getString("IS_NULLABLE");
    String columnDef = columns.getString("COLUMN_DEF");
}

// 获取主键信息
ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, "users");
```

### 常用列信息

| 字段 | 说明 |
|------|------|
| COLUMN_NAME | 列名 |
| TYPE_NAME | 数据类型 |
| DATA_TYPE | 数据类型（Java SQL类型） |
| COLUMN_SIZE | 列大小 |
| DECIMAL_DIGITS | 小数位数 |
| IS_NULLABLE | 是否可空 |
| COLUMN_DEF | 默认值 |
| IS_AUTOINCREMENT | 是否自增 |

---

## 类型信息捕获

### 常用类型映射

| Java类型 | 全限定名 |
|----------|----------|
| String | java.lang.String |
| Integer | java.lang.Integer |
| Long | java.lang.Long |
| Double | java.lang.Double |
| Boolean | java.lang.Boolean |
| List | java.util.List |
| Set | java.util.Set |
| Map | java.util.Map |
| LocalDateTime | java.time.LocalDateTime |
| BigDecimal | java.math.BigDecimal |

### JavaParser获取类型

```java
// 获取字段类型
FieldDeclaration field = ...;
Type fieldType = field.getCommonType();
String typeName = fieldType.asString();  // 简单类型名
String qualifiedName = fieldType.resolve().describe();  // 全限定名

// 获取方法返回类型
MethodDeclaration method = ...;
Type returnType = method.getType();
String returnTypeName = returnType.asString();

// 获取方法参数类型
NodeList<Parameter> parameters = method.getParameters();
for (Parameter param : parameters) {
    String paramName = param.getNameAsString();
    String paramType = param.getType().asString();
}
```

---

## 参考资料

1. [JavaParser官方文档](https://www.javaparser.org/)
2. [JavaParser GitHub](https://github.com/java-parser/java-parser)
3. [Tree库文档](/Users/zengfugen/IdeaProjects/dao1230.source/utility/Tree.md)

---

*更新时间: 2026-04-04*