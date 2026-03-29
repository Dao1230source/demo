package org.source.spring.cache.facade.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 产品输出对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOut implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private Double price;
}