package org.source.spring.doc.facade.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.source.spring.doc.domain.entity.UserEntity;
import org.source.spring.doc.facade.input.UserIn;
import org.source.spring.doc.facade.output.UserOut;
import org.source.utility.mapstruct.ThreeMapper;


/**
 * 用户对象转换器
 *
 * <p>基于 MapStruct 实现用户相关对象之间的转换。</p>
 * <p>继承 {@link ThreeMapper} 接口，支持三种对象之间的双向转换：</p>
 * <ul>
 *   <li>{@link UserIn} - 用户输入DTO，用于接收前端数据</li>
 *   <li>{@link UserEntity} - 用户实体类，用于数据库持久化</li>
 *   <li>{@link UserOut} - 用户输出VO，用于返回前端数据</li>
 * </ul>
 *
 * @author System Admin
 * @since 1.0.0
 * @see ThreeMapper
 */
@Mapper
public interface UserMapper extends ThreeMapper<UserIn, UserEntity, UserOut> {

    /**
     * 单例实例
     *
     * <p>通过 MapStruct 工厂获取的转换器实例，用于手动调用转换方法。</p>
     */
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

}
