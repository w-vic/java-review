package com.javareview.product.model.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品出参（VO = View Object）
 * <p>
 * VO 是返回给调用方的视图对象，只包含前端/调用方需要的字段，
 * 不暴露数据库内部字段（如 deleted、内部 ID 策略等）。
 * 同样使用 record，保持不可变。
 *
 * @author java-review
 */
public record ProductVO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
