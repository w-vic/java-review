package com.javareview.product.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品数据库实体（DO = Data Object）
 * <p>
 * DO 直接映射数据库表结构，仅在 Mapper/Service 层使用，
 * 禁止直接暴露给 Controller 返回——应转为 VO。
 *
 * @author java-review
 */
@Data
@TableName("product")
public class ProductDO {

    /**
     * 主键，数据库自增。
     * IdType.AUTO 表示依赖数据库的 AUTO_INCREMENT。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    /** 使用 BigDecimal 存储金额，避免浮点精度问题 */
    private BigDecimal price;

    private Integer stock;

    /** MyBatis-Plus 自动填充：插入时自动设置 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** MyBatis-Plus 自动填充：插入和更新时自动设置 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
