package org.source.demo.tree.model;

import lombok.Data;
import org.source.utility.tree.define.Element;

/**
* 行政区划数据元素
*/
@Data
public class AreaElement implements Element<String> {
    private String id;
    private String name;
    private Integer level;
    private String parentId;
    private Integer reserved;

    public AreaElement() {
    }

    public AreaElement(String id, String name, Integer level, String parentId, Integer reserved) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.parentId = parentId;
        this.reserved = reserved;
    }

    @Override
    public String toString() {
        return "AreaElement{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", parentId='" + parentId + '\'' +
                '}';
    }
}