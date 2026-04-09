# Spring Extension Cache Demo 导航说明

> **快速查找各种缓存场景的使用示例**  
> 所有测试用例均通过验证（50 个测试 ✅）

---

## 📋 测试用例索引

### 1. 基础 CRUD 缓存 - [`CacheDemoCrudTest.java`](src/test/java/org/source/spring/cache/CacheDemoCrudTest.java)

| 测试方法                                 | 场景      | 配置            |
|--------------------------------------|---------|---------------|
| `shouldCacheAndRetrieveSingleEntity` | 单实体缓存   | 默认 Redis 缓存   |
| `shouldUpdateAndInvalidateCache`     | 缓存更新和失效 | `@CacheEvict` |
| `shouldBatchInsertAndQuery`          | 批量插入和查询 | 批量缓存          |

**对应 Facade 方法：**

- `getProductById(String id)` - 单实体查询
- `getProductsByIds(Collection<String> ids)` - 批量查询

---

### 2. 批量缓存 - [`CacheDemoBatchTest.java`](src/test/java/org/source/spring/cache/CacheDemoBatchTest.java)

| 测试方法                      | 场景     | 配置                |
|---------------------------|--------|-------------------|
| `shouldCacheBatchResults` | 批量查询缓存 | `returnType=LIST` |

**对应 Facade 方法：**

- `getProductsByIds(Collection<String> ids)` - 批量查询，返回 List

---

### 3. 返回类型支持 - [
`CacheDemoReturnTypeTest.java`](src/test/java/org/source/spring/cache/CacheDemoReturnTypeTest.java)

| 测试方法              | 返回类型              | 配置               |
|-------------------|-------------------|------------------|
| `shouldReturnSet` | `Set<ProductOut>` | `returnType=SET` |

**对应 Facade 方法：**

- `getProductsByIdsAsSet(Collection<String> ids)` - 返回 Set

---

### 4. Map 返回类型 - [`CacheDemoMapTest.java`](src/test/java/org/source/spring/cache/CacheDemoMapTest.java)

| 测试方法              | 返回类型                      | 配置               |
|-------------------|---------------------------|------------------|
| `shouldReturnMap` | `Map<String, ProductOut>` | `returnType=MAP` |

**对应 Facade 方法：**

- `getProductMapByIds(Collection<String> ids)` - 返回 Map

---

### 5. 特殊返回类型 - [
`CacheDemoSpecialReturnTypeTest.java`](src/test/java/org/source/spring/cache/CacheDemoSpecialReturnTypeTest.java) ⭐

#### 5.1 单 key 返回容器

| 测试方法                           | 返回类型                  | 配置               |
|--------------------------------|-----------------------|------------------|
| `shouldReturnListForSingleKey` | `List<ProductOut>`    | `returnType=RAW` |
| `shouldReturnMapForSingleKey`  | `Map<String, String>` | `returnType=RAW` |

**对应 Facade 方法：**

- `getProductsByCategory(String categoryId)` - 单 key 返回 List
- `getProductAttributes(String productId)` - 单 key 返回 Map

#### 5.2 批量 key 返回嵌套容器

| 测试方法                            | 返回类型                               | 配置               |
|---------------------------------|------------------------------------|------------------|
| `shouldSupportMapWithListValue` | `Map<String, List<ProductOut>>`    | `returnType=MAP` |
| `shouldSupportMapWithMapValue`  | `Map<String, Map<String, String>>` | `returnType=MAP` |

**对应 Facade 方法：**

- `getProductsByCategories(Collection<String> categoryIds)` - 批量 key 返回 Map<K, List<V>>
- `getProductAttributesMap(Collection<String> productIds)` - 批量 key 返回 Map<K, Map<KK, V>>

#### 5.3 JVM 缓存支持

| 测试方法                            | 返回类型                  | 配置                                    |
|---------------------------------|-----------------------|---------------------------------------|
| `shouldSupportListWithJvmCache` | `List<ProductOut>`    | `cacheInJvm=true, cacheInRedis=false` |
| `shouldSupportMapWithJvmCache`  | `Map<String, String>` | `cacheInJvm=true, cacheInRedis=false` |

**对应 Facade 方法：**

- `getProductsByCategoryJvm(String categoryId)` - JVM 缓存 List
- `getProductAttributesJvm(String productId)` - JVM 缓存 Map

#### 5.4 二级缓存支持（Redis+JVM）

| 测试方法                                 | 返回类型                  | 配置                                   |
|--------------------------------------|-----------------------|--------------------------------------|
| `shouldSupportListWithTwoLevelCache` | `List<ProductOut>`    | `cacheInJvm=true, cacheInRedis=true` |
| `shouldSupportMapWithTwoLevelCache`  | `Map<String, String>` | `cacheInJvm=true, cacheInRedis=true` |

**对应 Facade 方法：**

- `getProductsByCategoryTwoLevel(String categoryId)` - 二级缓存 List
- `getProductAttributesTwoLevel(String productId)` - 二级缓存 Map

#### 5.5 不支持的场景

| 测试方法                                                     | 说明                  |
|----------------------------------------------------------|---------------------|
| `shouldThrowExceptionForUnsupportedNestedListReturnType` | `List<List<V>>` 不支持 |

---

### 6. 二级缓存 - [`CacheDemoTwoLevelTest.java`](src/test/java/org/source/spring/cache/CacheDemoTwoLevelTest.java)

| 测试方法                           | 场景             | 配置                                   |
|--------------------------------|----------------|--------------------------------------|
| `shouldCacheInBothJvmAndRedis` | JVM+Redis 二级缓存 | `cacheInJvm=true, cacheInRedis=true` |

**对应 Facade 方法：**

- `getHotProductById(String id)` - 二级缓存示例

---

### 7. 仅 JVM 缓存 - [`CacheDemoJvmOnlyTest.java`](src/test/java/org/source/spring/cache/CacheDemoJvmOnlyTest.java)

| 测试方法                   | 场景       | 配置                   |
|------------------------|----------|----------------------|
| `shouldCacheInJvmOnly` | 仅 JVM 缓存 | `cacheInRedis=false` |

**对应 Facade 方法：**

- `getProductsJvmOnly(Collection<String> ids)` - 仅 JVM 缓存

---

### 8. Redis 分片缓存 - [`CacheDemoShardTest.java`](src/test/java/org/source/spring/cache/CacheDemoShardTest.java)

#### 8.1 FIXED_SHARD 分片

| 测试方法                                | 场景            | 配置                          |
|-------------------------------------|---------------|-----------------------------|
| `shouldCacheWithFixedShardStrategy` | 按 hashCode 分片 | `shardStrategy=FIXED_SHARD` |

**对应 Facade 方法：**

- `getProductsFixedShardRedis(Collection<String> ids)` - FIXED_SHARD 分片

#### 8.2 FIXED_SIZE 分片

| 测试方法                               | 场景      | 配置                         |
|------------------------------------|---------|----------------------------|
| `shouldCacheWithFixedSizeStrategy` | 按数值范围分片 | `shardStrategy=FIXED_SIZE` |

**对应 Facade 方法：**

- `getProductsFixedSizeRedis(Collection<Long> ids)` - FIXED_SIZE 分片

#### 8.3 二级缓存 + 分片

| 测试方法                              | 场景        | 配置                                           |
|-----------------------------------|-----------|----------------------------------------------|
| `shouldCacheWithTwoLevelAndShard` | 二级缓存 + 分片 | `cacheInJvm=true, shardStrategy=FIXED_SHARD` |

**对应 Facade 方法：**

- `getProductsTwoLevelShard(Collection<String> ids)` - 二级缓存 + 分片

---

### 9. 部分缓存策略 - [
`CacheDemoPartialStrategyTest.java`](src/test/java/org/source/spring/cache/CacheDemoPartialStrategyTest.java)

| 测试方法                        | 策略         | 场景          |
|-----------------------------|------------|-------------|
| `shouldUseTrustStrategy`    | `TRUST`    | 信任缓存，缺失不补查  |
| `shouldUseDistrustStrategy` | `DISTRUST` | 不信任缓存，缺失则重查 |

**对应 Facade 方法：**

- `getProductsByIdsWithTrustStrategy(Collection<String> ids)` - TRUST 策略
- `getProductsByIdsWithDistrustStrategy(Collection<String> ids)` - DISTRUST 策略

---

### 10. 条件缓存 - [`CacheDemoConditionTest.java`](src/test/java/org/source/spring/cache/CacheDemoConditionTest.java)

| 测试方法                          | 场景      | 配置                                               |
|-------------------------------|---------|--------------------------------------------------|
| `shouldCacheWhenConditionMet` | 条件满足时缓存 | `condition="#id != null && #id.startsWith('1')"` |

**对应 Facade 方法：**

- `getProductWithCondition(String id)` - 条件缓存

---

### 11. 缓存失效 - [`CacheDemoEvictTest.java`](src/test/java/org/source/spring/cache/CacheDemoEvictTest.java)

| 测试方法                     | 场景     | 配置                             |
|--------------------------|--------|--------------------------------|
| `shouldEvictSingleCache` | 删除单个缓存 | `@CacheEvict(key="#id")`       |
| `shouldEvictAllCache`    | 删除所有缓存 | `@CacheEvict(allEntries=true)` |

**对应 Facade 方法：**

- `evictProduct(String id)` - 删除单个
- `evictAllProducts()` - 删除所有

---

### 12. 边界场景 - [`CacheDemoEdgeCaseTest.java`](src/test/java/org/source/spring/cache/CacheDemoEdgeCaseTest.java)

| 测试方法                          | 场景       |
|-------------------------------|----------|
| `shouldHandleEmptyCollection` | 空集合输入    |
| `shouldHandleNullValue`       | null 值处理 |
| `shouldHandleLargeBatch`      | 大批量查询    |

**对应 Facade 方法：**

- `getProductsByIds(Collection<String> ids)` - 边界场景测试

---

### 13. 集成测试 - [
`CacheDemoIntegrationTest.java`](src/test/java/org/source/spring/cache/CacheDemoIntegrationTest.java)

| 测试方法                        | 场景                  |
|-----------------------------|---------------------|
| `shouldWorkWithSpringCache` | Spring Cache 原生注解兼容 |

---

## 🔍 快速查找指南

### 按缓存类型查找

| 缓存类型            | 测试类                       | 示例方法                         |
|-----------------|---------------------------|------------------------------|
| **单实体缓存**       | `CacheDemoCrudTest`       | `getProductById`             |
| **批量缓存 (List)** | `CacheDemoBatchTest`      | `getProductsByIds`           |
| **批量缓存 (Map)**  | `CacheDemoMapTest`        | `getProductMapByIds`         |
| **批量缓存 (Set)**  | `CacheDemoReturnTypeTest` | `getProductsByIdsAsSet`      |
| **仅 JVM 缓存**    | `CacheDemoJvmOnlyTest`    | `getProductsJvmOnly`         |
| **二级缓存**        | `CacheDemoTwoLevelTest`   | `getHotProductById`          |
| **Redis 分片**    | `CacheDemoShardTest`      | `getProductsFixedShardRedis` |

### 按返回类型查找

| 返回类型                               | 测试类                              | 示例方法                      |
|------------------------------------|----------------------------------|---------------------------|
| `ProductOut`                       | `CacheDemoCrudTest`              | `getProductById`          |
| `List<ProductOut>`                 | `CacheDemoBatchTest`             | `getProductsByIds`        |
| `Map<String, ProductOut>`          | `CacheDemoMapTest`               | `getProductMapByIds`      |
| `Set<ProductOut>`                  | `CacheDemoReturnTypeTest`        | `getProductsByIdsAsSet`   |
| `List<ProductOut>` (RAW)           | `CacheDemoSpecialReturnTypeTest` | `getProductsByCategory`   |
| `Map<String, String>` (RAW)        | `CacheDemoSpecialReturnTypeTest` | `getProductAttributes`    |
| `Map<String, List<ProductOut>>`    | `CacheDemoSpecialReturnTypeTest` | `getProductsByCategories` |
| `Map<String, Map<String, String>>` | `CacheDemoSpecialReturnTypeTest` | `getProductAttributesMap` |

### 按特殊功能查找

| 功能               | 测试类                              | 示例方法                                |
|------------------|----------------------------------|-------------------------------------|
| **条件缓存**         | `CacheDemoConditionTest`         | `getProductWithCondition`           |
| **部分缓存策略**       | `CacheDemoPartialStrategyTest`   | `getProductsByIdsWithTrustStrategy` |
| **缓存失效**         | `CacheDemoEvictTest`             | `evictProduct`                      |
| **JVM+Redis 复合** | `CacheDemoSpecialReturnTypeTest` | `getProductsByCategoryTwoLevel`     |
| **Redis 分片**     | `CacheDemoShardTest`             | `getProductsFixedShardRedis`        |

---

## 🏷️ 配置说明

### @ConfigureCache 核心配置

```java
@ConfigureCache(
        cacheNames = "products",           // 缓存名称
        key = "#id",                       // 缓存 key
        cacheKeySpEl = "#R.id",           // 批量缓存时的单条 key 提取
        returnType = ReturnTypeEnum.AUTO,  // 返回类型
        partialCacheStrategy = PartialCacheStrategyEnum.DISTRUST,  // 部分缓存策略
        cacheInRedis = @CacheInRedis(      // Redis 配置
                ttl = 3600,
                valueClasses = {ProductOut.class},
                shardStrategy = ShardStrategyEnum.NONE
        ),
        cacheInJvm = @CacheInJvm(          // JVM 配置
                enable = true,
                ttl = 300,
                jvmMaxSize = 10000
        )
)
```

### ReturnTypeEnum 枚举

| 值      | 适用场景                           |
|--------|--------------------------------|
| `AUTO` | 自动判断（默认）                       |
| `LIST` | 返回 `List<E>`                   |
| `SET`  | 返回 `Set<E>`                    |
| `MAP`  | 返回 `Map<K,V>`                  |
| `RAW`  | 返回容器包装类（如 `List<ProductView>`） |

### PartialCacheStrategyEnum 枚举

| 值               | 说明                    |
|-----------------|-----------------------|
| `TRUST`         | 信任缓存，缺失不补查            |
| `DISTRUST`      | 不信任缓存，缺失则重查（默认）       |
| `PARTIAL_TRUST` | 智能处理：缓存命中 + 对缺失数据重新查询 |

### ShardStrategyEnum 枚举

| 值             | 说明            |
|---------------|---------------|
| `NONE`        | 不分片（默认）       |
| `FIXED_SHARD` | 按 hashCode 分片 |
| `FIXED_SIZE`  | 按数值范围分片       |

---

## 📊 测试统计

- **总测试数**: 50
- **通过**: 50 ✅
- **失败**: 0
- **跳过**: 0

---

## 🔗 相关文档

- [spring-cache.md](https://github.com/Dao1230source/spring-extension-starter/blob/main/spring-extension-starter-cache/spring-cache.md) -
  完整功能文档
