package org.source.demo.utility.assign.facade;

import org.source.demo.utility.assign.facade.data.Class1;
import org.source.demo.utility.assign.facade.data.Student;
import org.source.demo.utility.assign.facade.data.StudentInfo;
import org.source.demo.utility.assign.facade.data.Teacher;
import org.source.utility.utils.Streams;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class AssignFacade {

    public List<StudentInfo> getStudentInfoList() {
        return List.of(new StudentInfo(1L, "1", "New York First School"),
                new StudentInfo(2L, "2"),
                new StudentInfo(3L, "3", "New York First School"),
                new StudentInfo(4L, "4"));
    }

    public static final Map<Long, Student> STUDENT_MAP = Map.ofEntries(
            Map.entry(1L, new Student(1L, "Jim", 18)),
            Map.entry(2L, new Student(2L, "Tom", 20)),
            Map.entry(3L, new Student(3L, "Jack", 19)),
            Map.entry(6L, new Student(6L, "Jim2", 18)),
            Map.entry(7L, new Student(7L, "Tom2", 20)),
            Map.entry(8L, new Student(8L, "Jack2", 19))
    );

    public List<Student> getStudentList(Collection<Long> studentIdList) {
        return Streams.map(studentIdList, STUDENT_MAP::get).filter(Objects::nonNull).toList();
    }

    public List<Student> getStudentListThrow(Collection<Long> studentIdList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static final Map<String, Teacher> TEACHER_MAP = Map.ofEntries(
            Map.entry("2", new Teacher("2", "Wolf", "c2")),
            Map.entry("3", new Teacher("3", "Trump", "c3")),
            Map.entry("4", new Teacher("4", "Biden", "c4")),
            Map.entry("8", new Teacher("8", "Wolf", "c2")),
            Map.entry("9", new Teacher("9", "Trump", "c3")),
            Map.entry("10", new Teacher("10", "Biden", "c4"))
    );

    public Map<String, Teacher> getTeacherMap(Collection<String> teacherIdList) {
        return Streams.toMap(Streams.map(teacherIdList, TEACHER_MAP::get).filter(Objects::nonNull).toList(),
                Teacher::getId);
    }

    public static final Map<String, Class1> CLASS_MAP = Map.ofEntries(
            Map.entry("c1", new Class1("c1", "class1")),
            Map.entry("c2", new Class1("c2", "class2")),
            Map.entry("c3", new Class1("c3", "class3")),
            Map.entry("c4", new Class1("c4", "class4"))
    );

    public Map<String, Class1> getClassMap(Collection<String> classIdList) {
        return Streams.toMap(Streams.map(classIdList, CLASS_MAP::get).filter(Objects::nonNull).toList(),
                Class1::getId);
    }

}
