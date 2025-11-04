package org.source.demo.spring.facade.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class StudentData {
    private Long id;
    private String teacherId;

    private String name;
    private Integer age;
    private String teacherName;

    private String remark;
    private String school;

    private String classId;
    private String className;


    public StudentData(Long id, String teacherId) {
        this.id = id;
        this.teacherId = teacherId;
    }

    public StudentData(Long id, String teacherId, String school) {
        this.id = id;
        this.teacherId = teacherId;
        this.school = school;
    }
}
