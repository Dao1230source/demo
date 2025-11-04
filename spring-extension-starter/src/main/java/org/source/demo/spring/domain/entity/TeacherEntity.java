package org.source.demo.spring.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TeacherEntity {
    private String id;
    private String name;

    private String classId;
}
