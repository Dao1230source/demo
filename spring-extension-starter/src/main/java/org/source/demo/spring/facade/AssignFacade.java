package org.source.demo.spring.facade;

import org.source.demo.spring.domain.entity.ClassEntity;
import org.source.demo.spring.domain.entity.StudentEntity;
import org.source.demo.spring.facade.data.StudentData;
import org.source.demo.spring.domain.entity.TeacherEntity;
import org.source.utility.utils.Streams;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class AssignFacade {

    public List<StudentData> getStudentDataList() {
        return List.of(new StudentData(1L, "1", "Shenzhen First School"),
                new StudentData(2L, "2"),
                new StudentData(3L, "3", "Shenzhen First School"),
                new StudentData(4L, "4"));
    }

    public static final Map<Long, StudentEntity> STUDENT_MAP = Map.ofEntries(
            Map.entry(1L, new StudentEntity(1L, "Jim", 18)),
            Map.entry(2L, new StudentEntity(2L, "Tom", 20)),
            Map.entry(3L, new StudentEntity(3L, "Jack", 19)),
            Map.entry(6L, new StudentEntity(6L, "Jim2", 18)),
            Map.entry(7L, new StudentEntity(7L, "Tom2", 20)),
            Map.entry(8L, new StudentEntity(8L, "Jack2", 19))
    );

    public List<StudentEntity> getStudentList(Collection<Long> studentIdList) {
        return Streams.map(studentIdList, STUDENT_MAP::get).filter(Objects::nonNull).toList();
    }

    public List<StudentEntity> getStudentListThrow(Collection<Long> studentIdList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static final Map<String, TeacherEntity> TEACHER_MAP = Map.ofEntries(
            Map.entry("2", new TeacherEntity("2", "Wolf", "c2")),
            Map.entry("3", new TeacherEntity("3", "Trump", "c3")),
            Map.entry("4", new TeacherEntity("4", "Biden", "c4")),
            Map.entry("8", new TeacherEntity("8", "Wolf", "c2")),
            Map.entry("9", new TeacherEntity("9", "Trump", "c3")),
            Map.entry("10", new TeacherEntity("10", "Biden", "c4"))
    );

    public Map<String, TeacherEntity> getTeacherMap(Collection<String> teacherIdList) {
        return Streams.toMap(Streams.map(teacherIdList, TEACHER_MAP::get).filter(Objects::nonNull).toList(),
                TeacherEntity::getId);
    }

    public static final Map<String, ClassEntity> CLASS_MAP = Map.ofEntries(
            Map.entry("c1", new ClassEntity("c1", "class1")),
            Map.entry("c2", new ClassEntity("c2", "class2")),
            Map.entry("c3", new ClassEntity("c3", "class3")),
            Map.entry("c4", new ClassEntity("c4", "class4"))
    );

    public Map<String, ClassEntity> getClassMap(Collection<String> classIdList) {
        return Streams.toMap(Streams.map(classIdList, CLASS_MAP::get).filter(Objects::nonNull).toList(),
                ClassEntity::getId);
    }

}
