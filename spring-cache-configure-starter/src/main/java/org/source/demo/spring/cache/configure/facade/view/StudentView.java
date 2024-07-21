package org.source.demo.spring.cache.configure.facade.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StudentView {
    private Integer id;
    private String name;
    private Integer age;

    private String className;
}
