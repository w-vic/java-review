package com.javareview.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 扣减库存请求体（订单服务 Feign 与商品服务接口共用）
 */
public record StockDeductDTO(

        @NotNull(message = "扣减数量不能为空")
        @Min(value = 1, message = "扣减数量至少为 1")
        Integer quantity
) {
}
