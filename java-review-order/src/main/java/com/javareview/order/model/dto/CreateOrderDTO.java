package com.javareview.order.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建订单入参
 * <p>
 * orderNo 由前端/调用方生成并传入，用于幂等性校验：
 * 同一个 orderNo 只能创建一次订单，防止网络重试导致重复下单。
 *
 * @author java-review
 */
public record CreateOrderDTO(

        @NotBlank(message = "订单号不能为空")
        String orderNo,

        @NotNull(message = "商品ID不能为空")
        Long productId,

        @NotNull(message = "购买数量不能为空")
        @Min(value = 1, message = "购买数量至少为 1")
        Integer quantity
) {
}
