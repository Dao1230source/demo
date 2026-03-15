package org.source.spring.doc.facade.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.source.spring.doc.domain.entity.UserEntity;
import org.source.spring.doc.facade.input.UserIn;
import org.source.spring.doc.facade.output.UserOut;
import org.source.utility.mapstruct.ThreeMapper;


@Mapper
public interface UserMapper extends ThreeMapper<UserIn, UserEntity, UserOut> {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

}
