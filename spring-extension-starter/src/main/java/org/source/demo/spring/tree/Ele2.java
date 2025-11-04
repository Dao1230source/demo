package org.source.demo.spring.tree;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.source.utility.tree.define.Element;
import org.source.utility.tree.define.EnhanceElement;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class Ele2 extends EnhanceElement<String> {
    private final String id;
    private final String parentId;
    private final String sorted;

    @Override
    public int compareTo(@NotNull EnhanceElement<String> o) {
        return Element.comparator(this, (Ele2) o, Ele2::getSorted);
    }
}
