package com.javareview.product.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

/**
 * 商品更新入参
 * <p>
 * 更新时所有字段都是可选的（允许 null），只更新传入的字段。
 *
 * @author java-review
 */
public record ProductUpdateDTO(
        String name,
        String description,

        @DecimalMin(value = "0.01", message = "价格必须大于 0")
        BigDecimal price,

        @Min(value = 0, message = "库存不能为负数")
        Integer stock
) {
}
