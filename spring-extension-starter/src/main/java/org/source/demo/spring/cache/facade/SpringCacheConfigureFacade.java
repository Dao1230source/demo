package org.source.demo.spring.cache.facade;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.source.demo.spring.cache.facade.mapper.StudentMapper;
import org.source.demo.spring.cache.facade.param.StudentParam;
import org.source.demo.spring.cache.facade.view.StudentView;
import org.source.spring.cache.configure.CacheInJvm;
import org.source.spring.cache.configure.CacheInRedis;
import org.source.spring.cache.configure.ConfigureCache;
import org.source.spring.cache.configure.ReturnTypeEnum;
import org.source.spring.cache.strategy.PartialCacheStrategyEnum;
import org.source.spring.expression.VariableConstants;
import org.source.utility.utils.Jsons;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SpringCacheConfigureFacade {

    /**
     * <pre>
     * 默认缓存在redis
     * 本地缓存使用 caffeine
     * 以下所有的 {@literal <key, value>}
     * key如果缓存在redis，指得是cacheName和 {@literal ConfigureCache#key()}的值组合的redis key
     *  如果缓存在本地内存，key就是 {@link Cache} 的key
     * value指缓存的值
     * </pre>
     *
     * @param str str
     * @return {@literal <str, str>}
     */
    @ConfigureCache(cacheNames = "str2str")
    public String str2str(String str) {
        log.info("str2str {}", str);
        return str;
    }

    /**
     * 二级缓存，同时缓存在本地内存和redis
     *
     * @param str str
     * @return {@literal <str, str>}
     */
    @ConfigureCache(cacheNames = "str2strCacheInJvm", cacheInJvm = @CacheInJvm(enable = true))
    public String str2strCacheInJvm(String str) {
        log.info("str2strCacheInJvm {}", str);
        return str;
    }

    /**
     * <pre>
     * {@literal @ConfigureCache}是{@literal @Cacheable}的增强，支持多样化的配置，
     * 同时spring cache注解{@literal @Cacheable、@Caching、@CacheEvict、@CachePut} 照旧使用
     *
     * 如果数据也缓存在本地，删除缓存时会通过redis的pub/sub机制同时删除集群所有实例的本地缓存
     * </pre>
     *
     * @param str str
     * @return {@literal <str, str>}
     */
    @ConfigureCache(cacheNames = "str2strAndCaching", cacheInJvm = @CacheInJvm(enable = true))
    @Caching(evict = {@CacheEvict(cacheNames = "str2strAndCaching")})
    public String str2strAndCaching(String str) {
        log.info("str2strAndCaching {}", str);
        return str;
    }

    /**
     * str2View
     *
     * @param str str
     * @return {@literal <str, StudentView>}
     */
    @ConfigureCache(cacheNames = "str2View")
    public StudentView str2View(String str) {
        log.info("str2View {}", str);
        return StudentView.builder().name(str).build();
    }

    /**
     * key=输入参数 StudentParam的 id
     * 如果不设置 {@literal ConfigureCache.key()} 默认的key就是param的json字符串
     *
     * @param param param
     * @return {@literal <str, StudentView>}
     */
    @ConfigureCache(cacheNames = "param2View", key = "#param.name")
    public StudentView param2View(StudentParam param) {
        log.info("param2View {}", Jsons.str(param));
        return StudentMapper.INSTANCE.x2y(param);
    }

    /**
     * 如果返回值是{@literal List<E>/Set<E>}就需要指定 E 中和参数 [P]中 P 一一对应的值，比如这里参数name，对应StudentView.name
     * <br>
     * {@literal #P} 是固定语法，代指 E
     *
     * @param names names
     * @return {@literal [<str, StudentView>]}
     */
    @ConfigureCache(cacheNames = "strings2ViewList", key = "#names",
            cacheKeySpEl = VariableConstants.RESULT_SP_EL + ".name")
    public List<StudentView> strings2ViewList(Collection<String> names) {
        log.info("strings2ViewList {}", Jsons.str(names));
        return names.stream().map(n -> StudentView.builder().name(n).build()).toList();
    }

    /**
     * 如果缓存在本地，key不是默认的String类型，需要指定 {@literal CacheInJvm.keyClass()}，这里是Integer类型
     *
     * @param ids ids
     * @return {@literal [<Integer, StudentView>]}
     */
    @ConfigureCache(cacheNames = "str2ViewListJvmIntegerKey", key = "#ids",
            cacheKeySpEl = VariableConstants.RESULT_SP_EL + ".id",
            cacheInRedis = @CacheInRedis(enable = false),
            cacheInJvm = @CacheInJvm(enable = true, keyClass = Integer.class))
    public List<StudentView> str2ViewListJvmIntegerKey(Collection<Integer> ids) {
        log.info("str2ViewListJvmIntegerKey {}", Jsons.str(ids));
        return ids.stream().map(n -> StudentView.builder().id(n).build()).toList();
    }

    /**
     * 如果返回值是{@literal Map<K,V>}，K必须和参数[P]中的P一一对应
     *
     * @param names names
     * @return {@literal [<str, StudentView>]}
     */
    @ConfigureCache(cacheNames = "str2ViewMap", key = "#names")
    public Map<String, StudentView> str2ViewMap(Collection<String> names) {
        log.info("str2ViewMap {}", Jsons.str(names));
        return names.stream().map(n -> StudentView.builder().name(n).build())
                .collect(Collectors.toMap(StudentView::getName, Function.identity()));
    }

    /**
     * <pre>
     * 如果是一对多的关系，比如一个班级有多个学生
     * 需要指定 {@literal returnType = ReturnTypeEnum.RAW}，默认情况下会自动判断value的类型，无需指定
     * </pre>
     *
     * @param className className
     * @return {@literal <str, List<StudentView>>}
     */
    @ConfigureCache(cacheNames = "param2ViewList", key = "#className", returnType = ReturnTypeEnum.RAW,
            cacheInRedis = @CacheInRedis(valueClasses = {List.class, StudentView.class}))
    public List<StudentView> str2ViewList(String className) {
        log.info("str2ViewList {}", Jsons.str(className));
        return List.of(StudentView.builder().name("Tom").age(18).className(className).build(),
                StudentView.builder().name("Jim").age(19).className(className).build());
    }

    /**
     * <pre>
     * 同样一对多不缓存在redis，而缓存在本地内存
     * </pre>
     *
     * @param className className
     * @return {@literal <str, List<StudentView>>}
     */
    @ConfigureCache(cacheNames = "str2ViewListCacheInJvm", key = "#className", returnType = ReturnTypeEnum.RAW,
            cacheInRedis = @CacheInRedis(enable = false),
            cacheInJvm = @CacheInJvm(enable = true))
    public List<StudentView> str2ViewListCacheInJvm(String className) {
        log.info("str2ViewListCacheInJvm {}", Jsons.str(className));
        return List.of(StudentView.builder().name("Tom").age(18).className(className).build(),
                StudentView.builder().name("Jim").age(19).className(className).build());
    }

    /**
     * <pre>
     * 指获取到部分缓存时，可以配置不同的处理策略，比如输入["Tom","Jim"]获取到缓存值[StudentView(name="Tom")]
     *  {@literal partialCache = PartialCacheStrategyEnum.TRUST}表示信任缓存数据，返回结果
     *  {@literal partialCache = PartialCacheStrategyEnum.DISTRUST}表示不信任缓存数据，返回null
     *  {@literal partialCache = PartialCacheStrategyEnum.PARTIAL_TRUST}表示信任缓存数据，
     *      但部分未获取到值的key重新请求，即重新从数据库获取"Jim"的数据
     *
     * </pre>
     *
     * @param names names 该入参不能是不可变集合
     * @return {@literal [<str, StudentView>]}
     */
    @ConfigureCache(cacheNames = "partialCacheStrategyPartialTrust", key = "#names",
            cacheKeySpEl = VariableConstants.RESULT_SP_EL + ".name",
            partialCacheStrategy = PartialCacheStrategyEnum.PARTIAL_TRUST)
    public List<StudentView> partialCacheStrategyPartialTrust(Collection<String> names) {
        log.info("partialCacheStrategyPartialTrust {}", Jsons.str(names));
        return names.stream().map(n -> StudentView.builder().name(n).build()).toList();
    }

    @ConfigureCache(cacheNames = "partialCacheStrategyTrust", key = "#names",
            cacheKeySpEl = VariableConstants.RESULT_SP_EL + ".name",
            partialCacheStrategy = PartialCacheStrategyEnum.TRUST)
    public List<StudentView> partialCacheStrategyTrust(Collection<String> names) {
        log.info("partialCacheStrategyTrust {}", Jsons.str(names));
        return names.stream().map(n -> StudentView.builder().name(n).build()).toList();
    }
}
