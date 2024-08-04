package org.source.demo.spring.facade.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.source.demo.spring.facade.param.StudentParam;
import org.source.demo.spring.facade.view.StudentView;
import org.source.utility.mapstruct.TwoMapper;

@Mapper
public interface StudentMapper extends TwoMapper<StudentParam, StudentView> {

    StudentMapper INSTANCE = Mappers.getMapper(StudentMapper.class);
}
