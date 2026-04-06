# Java Doc注释解析器 - 项目文档

## 📚 文档结构

本项目采用 **Harness Engineering** 规范进行文档管理，所有技术文档按照以下结构组织：

```
docs/
├── README.md          # 本文档，文档导航和快速入口
├── harness.md         # 主文档，包含 F.D.N.R 分析
├── spec.md            # 技术规格文档
├── plan.md            # 执行计划文档
├── progress.md        # 进度跟踪文档
├── findings.md        # 研究发现文档
└── task_plan.md.archive  # 旧版计划文档（已归档）
```

---

## 🎯 快速导航

### 新手入门

1. **项目概览** → [harness.md](harness.md) - 了解项目目标、技术选型、强制规范
2. **技术细节** → [spec.md](spec.md) - 查看数据模型、API规格、性能指标
3. **执行进度** → [plan.md](plan.md) - 查看各阶段任务和进度跟踪

### 开发人员

- **当前任务** → [plan.md#phase-8-模块层级结构-](plan.md) - Phase 8 进行中
- **编码规范** → [harness.md#️-强制规范遵循-coding-rules](harness.md) - 必须遵循 coding-rules
- **API文档** → [spec.md#2-api-规格](spec.md) - 查看接口定义

### 项目管理

- **项目状态** → [harness.md#fnr-分析](harness.md) - F.D.N.R 分析
- **进度跟踪** → [progress.md](progress.md) - 每日进度记录
- **风险管理** → [harness.md#风险与缓解](harness.md) - 风险识别与缓解措施

---

## 📋 文档说明

### harness.md - 主文档

**内容**:
- **F.D.N.R 分析**: Future State (未来状态), Design (设计), Now State (当前状态), Remediation (修复方案)
- **项目概述**: 业务价值、核心需求、输出位置
- **强制规范**: coding-rules 核心要求
- **关键决策**: 技术选型决策和影响
- **风险管理**: 风险识别与缓解措施

**适用人群**: 所有相关人员

---

### spec.md - 技术规格文档

**内容**:
- **数据模型规格**: Element继承体系、数据库模型
- **API规格**: DocParser、ModuleParser、DocEnhanceTree接口定义
- **性能规格**: 并发处理、性能指标、内存占用
- **异常处理规格**: 异常类型、处理策略
- **测试规格**: 覆盖率、测试分类、测试数据
- **依赖规格**: 核心依赖、测试依赖

**适用人群**: 开发工程师、架构师

---

### plan.md - 执行计划文档

**内容**:
- **执行概览**: 项目总体规划和时间安排
- **Phase 1-9**: 分阶段的详细任务、测试用例、完成标准
- **执行策略**: TDD流程、代码规范强制检查
- **进度跟踪**: 当前状态、关键里程碑、下一步行动
- **附录**: 测试统计、代码统计

**适用人群**: 开发工程师、项目经理

---

### progress.md - 进度跟踪文档

**内容**:
- **强制规范提醒**: 每日必查项
- **2026-04-04**: Phase 1-7 完成记录
- **2026-04-05**: Phase 8 进行中记录

**适用人群**: 开发工程师、项目经理

---

### findings.md - 研究发现文档

**内容**:
- **coding-rules 核心规范**: 注释、Lombok、判空、日志、代码质量
- **JavaParser 使用研究**: 核心API、解析方法
- **Tree库集成**: EnhanceElement、EnhanceNode使用方法
- **项目结构规范**: 标准目录结构

**适用人群**: 开发工程师

---

## ⚠️ 重要提醒

### 强制规范

**所有代码编写必须严格遵循 coding-rules 规范，无例外！**

每次提交代码前，必须验证以下项：

- [ ] 所有公共类、接口、方法是否有 JavaDoc 注释？
- [ ] 是否使用 Lombok 注解（@Getter, @Setter）？
- [ ] 继承父类的子类是否添加 @EqualsAndHashCode(callSuper = true)？
- [ ] 重写接口方法是否添加 @NonNull 注解？
- [ ] 判空是否使用 commons-lang3/commons-collections4？
- [ ] 是否避免使用 System.out.println()？
- [ ] 方法长度是否超过 60 行？
- [ ] 圈复杂度是否超过 15？

详见: [harness.md#️-强制规范遵循-coding-rules](harness.md)

---

## 🚀 当前状态

- **当前Phase**: Phase 9 (文档与交付) - 待开始 ⏳
- **已完成Phases**: 1-8 ✅
- **总进度**: 100% (8/8 Phases 完成)
- **测试状态**: 50个测试全部通过 ✅
- **代码规范**: 全部通过 coding-rules 检查 ✅

---

## 📞 联系方式

- **项目负责人**: dao1230source
- **创建时间**: 2026-04-05
- **最后更新**: 2026-04-05

---

*本文档遵循 Harness Engineering 规范*