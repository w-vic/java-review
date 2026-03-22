package com.javareview.order.convert;

import com.javareview.order.feign.ProductFeignVO;
import com.javareview.order.model.dto.CreateOrderDTO;
import com.javareview.order.model.entity.OrderDO;
import com.javareview.order.model.vo.OrderVO;

/**
 * 订单对象转换器
 *
 * @author java-review
 */
public final class OrderConverter {

    private OrderConverter() {
    }

    public static OrderDO toEntity(CreateOrderDTO dto, ProductFeignVO product) {
        var entity = new OrderDO();
        entity.setOrderNo(dto.orderNo());
        entity.setProductId(dto.productId());
        entity.setProductName(product.name());
        entity.setPrice(product.price());
        entity.setQuantity(dto.quantity());
        entity.setTotalAmount(product.price().multiply(java.math.BigDecimal.valueOf(dto.quantity())));
        entity.setStatus(0);
        return entity;
    }

    /**
     * 将订单实体转为 VO
     * <p>
     * 演示 JDK 17 switch expression（箭头语法 + 直接返回值），
     * 比传统 switch-case-break 更简洁、不会遗漏 break。
     */
    public static OrderVO toVO(OrderDO entity) {
        var statusDesc = switch (entity.getStatus()) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已取消";
            default -> "未知状态";
        };

        return new OrderVO(
                entity.getId(),
                entity.getOrderNo(),
                entity.getProductId(),
                entity.getProductName(),
                entity.getPrice(),
                entity.getQuantity(),
                entity.getTotalAmount(),
                statusDesc,
                entity.getCreateTime(),
                entity.getUpdateTime()
        );
    }
}
