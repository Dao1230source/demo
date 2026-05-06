package org.source.spring.doc.domain.value;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.source.spring.object.ObjectBodyData;

@EqualsAndHashCode(callSuper = true)
@Data
public class DocData extends ObjectBodyData {
    private String name;
    private String parentName;
}
