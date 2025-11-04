package org.source.spring.log.facade.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.source.spring.log.facade.param.UserParam;
import org.source.spring.log.facade.view.UserView;
import org.source.utility.mapstruct.TwoMapper;

@Mapper
public interface UserMapper extends TwoMapper<UserParam, UserView> {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
}
