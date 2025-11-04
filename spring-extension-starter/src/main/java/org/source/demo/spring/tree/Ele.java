package org.source.demo.spring.tree;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.source.utility.tree.define.Element;

@AllArgsConstructor
@Data
public class Ele implements Element<String> {
    private final String id;
    private final String parentId;
}
