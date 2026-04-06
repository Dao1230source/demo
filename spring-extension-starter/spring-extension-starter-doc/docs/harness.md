# Harness Engineering: Java Doc注释解析器

## F.D.N.R 分析

### Future State (未来状态)

**目标**: 构建一个完整的 Java Doc 注释解析系统，能够：

1. **自动解析** - 自动解析项目中所有 Java 相关的 doc 注释（类、方法、字段、注解）
2. **层级关系** - 建立完整的层级结构：`Module → Class → Method → Field/Param`
3. **类型捕获** - 记录完整的类型信息（全限定名、返回值类型、参数类型等）
4. **数据库集成** - 解析 JPA/MyBatis 注解，关联 Java 实体与数据库表结构
5. **模块识别** - 自动识别 Maven 模块层级结构，追溯到非 SpringBoot 服务为止
6. **多线程解析** - 使用并行流提高大规模代码库的解析效率

**成功标准**:
- ✅ 50+ 单元测试全部通过
- ✅ 支持多父节点 DAG 结构（EnhanceTree）
- ✅ 类型信息捕获完整（FQDN）
- ✅ JPA/MyBatis 注解解析正确
- ✅ 模块层级结构正确识别

---

### Design (设计)

#### 核心架构

```
DocParser (统一入口)
    ├── ModuleParser (模块解析器)
    ├── DocCommentParser (注释解析器)
    ├── RestAnnotationParser (REST注解解析器)
    ├── JpaAnnotationParser (JPA注解解析器)
    ├── DocTagParser (标签解析器)
    └── DocEnhanceTree (文档树)
```

#### 数据模型

**Element 继承体系**:

```
DocElement (抽象基类)
├── ClassDocElement (类doc元素)
│   └── 字段: className, classQualifiedName, moduleName, tableName, isEntity
├── MethodDocElement (方法doc元素)
│   └── 字段: methodName, returnType, classQualifiedName, parameters
├── FieldDocElement (字段doc元素)
│   └── 字段: fieldName, fieldType, classQualifiedName, columnName, isPrimaryKey
├── ParamDocElement (参数doc元素)
│   └── 字段: paramName, paramType, methodId
├── RestDocElement (REST接口元素)
│   └── 字段: path, httpMethod, pathVariables, requestParams
└── ModuleDocElement (模块元素)
    └── 字段: moduleName, modulePath, parentModuleName, isSpringBootModule
```

#### 技术选型

| 组件 | 技术选择 | 理由 |
|------|---------|------|
| 源码解析 | JavaParser | 业界标准，功能完整 |
| 树结构 | EnhanceTree + EnhanceNode | 支持多父节点DAG |
| 注解处理 | JavaParser + 自定义解析器 | 灵活可扩展 |
| 并发处理 | parallelStream | 简单高效 |
| 判空处理 | commons-lang3/commons-collections4 | 企业级标准 |
| 日志框架 | SLF4J + @Slf4j | 行业标准 |

---

### Now State (当前状态)

**已完成**:
- ✅ Phase 1-8: 基础数据模型、层级关系、完整流程、高级特性、测试验证、类型信息捕获、数据库集成、模块层级结构
- ✅ 50 个单元测试全部通过
- ✅ 多线程并行解析实现
- ✅ 统一入口 parseDirectory() 方法
- ✅ 代码规范检查全部通过

**当前问题**:
- 无

**技术债务**:
- 📝 添加更多集成测试
- 📝 优化异常处理
- 📝 性能优化

---

### Remediation (修复方案)

#### 行动计划

**优先级 1 - 代码规范强制执行**:
- [x] 为所有公共类、接口、方法添加 JavaDoc 注释
- [x] 确保所有子类添加 @EqualsAndHashCode(callSuper = true)
- [x] 确保所有重写方法添加 @NonNull 注解
- [x] 使用 commons-lang3/commons-collections4 进行判空

**优先级 2 - 模块层级完善**:
- [x] 修复 ModuleParser 测试中的路径配置问题
- [x] 添加模块层级的集成测试
- [x] 验证 Module → Class → Method → Field 层级结构

**优先级 3 - 文档完善**:
- [ ] 更新 README.md
- [ ] 添加使用示例和最佳实践
- [ ] 完善 API 文档

---

## 项目概述

### 业务价值

**解决的核心问题**:
1. **代码文档自动化** - 自动提取和结构化 JavaDoc 注释
2. **层级关系可视化** - 清晰展示 Module → Class → Method → Field 层级
3. **数据库映射关联** - 自动关联 Java 实体与数据库表结构
4. **模块架构分析** - 自动识别项目模块结构和依赖关系

**目标用户**:
- Java 开发工程师
- 架构师
- 代码审查人员
- 技术文档编写者

### 核心需求

1. **解析注释的同时记录类型信息**（class全限定名、field类型、方法返回值类型等）
2. **解析JPA/MyBatis数据库实体类的field对应的数据库表字段**
3. **通过数据库连接获取数据表的结构**
4. **建立Java实体与数据库表的关联关系**

### 输出位置

- **结果保存位置**: `/Users/zengfugen/IdeaProjects/dao1230.source/utility/Tree.md`
- **输出目录**: `/Users/zengfugen/IdeaProjects/dao1230.source/demo/spring-extension-starter/spring-extension-starter-doc`

---

## ⚠️ 强制规范：遵循 coding-rules

**所有代码编写必须严格遵循 `coding-rules` 规范，无例外！**

### 核心要求

#### 1. 注释规范（强制）

**所有公共类、接口、方法必须有 JavaDoc 注释，禁止使用尾行注释！**

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
     * 模块名称
     */
    private String moduleName;
    
    /**
     * 获取元素的唯一标识
     *
     * @return 模块路径
     */
    @Override
    public @NonNull String getId() {
        return modulePath;
    }
}
```

#### 2. Lombok 注解规范（强制）

- **所有数据类必须使用 Lombok 注解**（@Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor, @Builder）
- **继承父类的子类必须添加 @EqualsAndHashCode(callSuper = true)**
- **继承接口的重写方法必须添加 @NonNull 注解**

#### 3. 判空处理规范（强制）

**使用 commons-lang3 和 commons-collections4 进行判空：**

```java
// 字符串判空
if (StringUtils.isNotBlank(str)) {
    // 处理非空字符串
}

// 集合判空
if (CollectionUtils.isNotEmpty(list)) {
    // 处理非空集合
}

// Map判空
if (MapUtils.isNotEmpty(map)) {
    // 处理非空Map
}
```

#### 4. 代码质量标准（强制）

- **Sonar 质量规则**: 无 blocker、critical、major 级别问题
- **Alibaba Java Coding Guidelines**: 遵循阿里巴巴 Java 开发手册
- **圈复杂度**: 单个方法不超过 15
- **方法长度**: 不超过 60 行

#### 5. 日志规范（强制）

- 使用 SLF4J 的 `@Slf4j` Lombok 注解
- **禁止使用** `System.out.println()`
- 正确日志级别：DEBUG < INFO < WARN < ERROR

#### 6. 项目结构规范（强制）

遵循标准项目结构：

```
org.source.xxx/
├── domain/
│   ├── entity/       # 数据库实体类
│   └── element/      # 文档元素类
├── infrastructure/
│   ├── util/         # 工具类
│   └── exception/    # 异常类
└── app/              # 应用层
```

### 验证清单

每次编写代码后，必须验证以下项：

- [ ] 所有公共类是否有 JavaDoc 注释？
- [ ] 所有公共方法是否有 JavaDoc 注释？
- [ ] 是否使用 Lombok 注解（@Getter, @Setter）？
- [ ] 继承父类的子类是否添加 @EqualsAndHashCode(callSuper = true)？
- [ ] 重写接口方法是否添加 @NonNull 注解？
- [ ] 判空是否使用 commons-lang3/commons-collections4？
- [ ] 是否避免使用 System.out.println()？
- [ ] 方法长度是否超过 60 行？
- [ ] 圈复杂度是否超过 15？

---

## 关键决策

| 决策点 | 选择 | 理由 | 影响 |
|--------|------|------|------|
| 树结构 | EnhanceTree + EnhanceNode | 支持多父节点DAG | 灵活但复杂度稍高 |
| 并发策略 | parallelStream | 简单高效 | 需要线程安全的解析器实例 |
| 模块识别 | pom.xml解析 | Maven标准 | 兼容性好，但依赖文件格式 |
| 注释解析 | JavaParser | 业界标准 | 功能完整，学习曲线平缓 |

---

## 风险与缓解

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| 大规模代码库性能问题 | 高 | 中 | 使用并行流、分批处理 |
| 复杂注解解析错误 | 中 | 低 | 完善测试用例、异常处理 |
| 模块层级识别失败 | 中 | 低 | 多种识别策略、降级处理 |
| 代码规范执行不严格 | 高 | 高 | 强制code review、自动化检查 |

---

*创建时间: 2026-04-05*
*版本: 1.0.0*