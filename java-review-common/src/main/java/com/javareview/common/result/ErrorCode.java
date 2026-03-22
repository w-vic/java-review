package com.javareview.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局错误码枚举
 * <p>
 * 按模块划分区间，避免不同服务的错误码冲突：
 * - 10xxx：通用错误
 * - 20xxx：商品模块
 * - 30xxx：订单模块
 *
 * @author java-review
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ==================== 通用 ====================
    SYSTEM_ERROR(10000, "系统异常，请稍后重试"),
    PARAM_ERROR(10001, "参数校验失败"),
    NOT_FOUND(10002, "资源不存在"),

    // ==================== 商品模块 ====================
    PRODUCT_NOT_FOUND(20001, "商品不存在"),
    STOCK_NOT_ENOUGH(20002, "库存不足"),

    // ==================== 订单模块 ====================
    ORDER_NOT_FOUND(30001, "订单不存在"),
    ORDER_DUPLICATE(30002, "重复提交，请勿重复下单"),
    ORDER_CANNOT_PAY(30003, "当前订单状态不允许付款"),
    ORDER_CANNOT_UPDATE(30004, "当前订单不允许修改"),
    ORDER_CANNOT_DELETE(30005, "已支付订单不可删除"),
    ORDER_ALREADY_CANCELLED(30006, "订单已取消"),
    ;

    private final int code;
    private final String message;
}
