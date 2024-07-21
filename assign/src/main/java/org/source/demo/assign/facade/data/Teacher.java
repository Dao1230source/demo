package org.source.demo.assign.facade.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Teacher {
    private String id;
    private String name;

    private String classId;
}
