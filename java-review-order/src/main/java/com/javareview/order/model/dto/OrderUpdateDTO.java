package com.javareview.order.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 修改订单（仅待支付）：调整购买数量
 */
public record OrderUpdateDTO(

        @NotNull(message = "购买数量不能为空")
        @Min(value = 1, message = "购买数量至少为 1")
        Integer quantity
) {
}
