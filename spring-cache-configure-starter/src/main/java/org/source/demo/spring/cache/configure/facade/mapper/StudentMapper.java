package org.source.demo.spring.cache.configure.facade.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.source.demo.spring.cache.configure.facade.param.StudentParam;
import org.source.demo.spring.cache.configure.facade.view.StudentView;
import org.source.mapstruct.TwoMapper;

@Mapper
public interface StudentMapper extends TwoMapper<StudentParam, StudentView> {

    StudentMapper INSTANCE = Mappers.getMapper(StudentMapper.class);
}
