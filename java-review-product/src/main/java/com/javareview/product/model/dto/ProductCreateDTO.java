package com.javareview.product.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 商品创建入参（DTO = Data Transfer Object）
 * <p>
 * 使用 JDK 17 的 record 特性：
 * - record 自动生成 constructor、getter、equals、hashCode、toString
 * - 天然不可变（immutable），非常适合做入参载体
 * - 字段名即 getter 方法名（name() 而非 getName()）
 *
 * @author java-review
 */
public record ProductCreateDTO(

        @NotBlank(message = "商品名称不能为空")
        String name,

        String description,

        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于 0")
        BigDecimal price,

        @NotNull(message = "库存不能为空")
        @Min(value = 0, message = "库存不能为负数")
        Integer stock
) {
}
