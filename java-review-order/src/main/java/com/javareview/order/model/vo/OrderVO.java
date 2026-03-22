package com.javareview.order.model.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单出参
 *
 * @author java-review
 */
public record OrderVO(
        Long id,
        String orderNo,
        Long productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        BigDecimal totalAmount,
        String statusDesc,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
