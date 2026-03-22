package com.javareview.order.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单数据库实体
 *
 * @author java-review
 */
@Data
@TableName("orders")
public class OrderDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务订单号（用于幂等判断） */
    private String orderNo;

    private Long productId;

    private String productName;

    private BigDecimal price;

    private Integer quantity;

    /** 订单总金额 = price × quantity */
    private BigDecimal totalAmount;

    /**
     * 订单状态（使用 switch expression 演示）
     * 0-待支付  1-已支付  2-已取消
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
