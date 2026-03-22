package com.javareview.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javareview.common.dto.StockDeductDTO;
import com.javareview.common.exception.BizException;
import com.javareview.common.result.ErrorCode;
import com.javareview.common.result.Result;
import com.javareview.order.convert.OrderConverter;
import com.javareview.order.event.OrderIdempotentKeyEvent;
import com.javareview.order.feign.ProductFeignClient;
import com.javareview.order.mapper.OrderMapper;
import com.javareview.order.model.dto.CreateOrderDTO;
import com.javareview.order.model.dto.OrderUpdateDTO;
import com.javareview.order.model.entity.OrderDO;
import com.javareview.order.model.vo.OrderVO;
import com.javareview.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务实现
 * <p>
 * 演示要点：
 * 1. Redis SETNX 实现幂等性（防止重复下单）
 * 2. OpenFeign 调用商品服务（校验库存、付款时扣减库存）
 * 3. 创建订单仅待支付、不占库存；付款成功后 Feign 同步扣库存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final StringRedisTemplate redisTemplate;
    private final ProductFeignClient productFeignClient;
    private final ApplicationEventPublisher eventPublisher;

    private static final String IDEMPOTENT_KEY_PREFIX = "order:idempotent:";
    private static final long IDEMPOTENT_TTL_HOURS = 24;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO create(CreateOrderDTO dto) {
        var idempotentKey = IDEMPOTENT_KEY_PREFIX + dto.orderNo();
        var isFirstRequest = redisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", IDEMPOTENT_TTL_HOURS, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(isFirstRequest)) {
            throw new BizException(ErrorCode.ORDER_DUPLICATE);
        }

        eventPublisher.publishEvent(new OrderIdempotentKeyEvent(idempotentKey));

        var productResult = productFeignClient.getById(dto.productId());
        assertFeignOk(productResult);
        if (productResult.getData() == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        var product = productResult.getData();

        var stockResult = productFeignClient.getStock(dto.productId());
        assertFeignOk(stockResult);
        if (stockResult.getData() == null || stockResult.getData() < dto.quantity()) {
            throw new BizException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        var entity = OrderConverter.toEntity(dto, product);
        orderMapper.insert(entity);

        log.info("订单创建成功（待支付）, orderNo={}, productId={}", dto.orderNo(), dto.productId());
        return OrderConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO pay(Long id) {
        var order = orderMapper.selectByIdForUpdate(id);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() == 1) {
            return OrderConverter.toVO(order);
        }
        if (order.getStatus() != 0) {
            throw new BizException(ErrorCode.ORDER_CANNOT_PAY);
        }

        var deductResult = productFeignClient.deductStock(order.getProductId(), new StockDeductDTO(order.getQuantity()));
        assertFeignOk(deductResult);

        order.setStatus(1);
        orderMapper.updateById(order);
        log.info("订单付款成功, id={}, orderNo={}", id, order.getOrderNo());
        return OrderConverter.toVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO cancel(Long id) {
        var order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() == 2) {
            throw new BizException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }
        if (order.getStatus() != 0) {
            throw new BizException(ErrorCode.ORDER_CANNOT_UPDATE);
        }
        order.setStatus(2);
        orderMapper.updateById(order);
        log.info("订单已取消, id={}, orderNo={}", id, order.getOrderNo());
        return OrderConverter.toVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO update(Long id, OrderUpdateDTO dto) {
        var order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != 0) {
            throw new BizException(ErrorCode.ORDER_CANNOT_UPDATE);
        }

        var productResult = productFeignClient.getById(order.getProductId());
        assertFeignOk(productResult);
        if (productResult.getData() == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        var product = productResult.getData();

        var stockResult = productFeignClient.getStock(order.getProductId());
        assertFeignOk(stockResult);
        if (stockResult.getData() == null || stockResult.getData() < dto.quantity()) {
            throw new BizException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        order.setQuantity(dto.quantity());
        order.setPrice(product.price());
        order.setTotalAmount(product.price().multiply(BigDecimal.valueOf(dto.quantity())));
        orderMapper.updateById(order);
        log.info("订单数量已更新, id={}, quantity={}", id, dto.quantity());
        return OrderConverter.toVO(order);
    }

    @Override
    public void delete(Long id) {
        var order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() == 1) {
            throw new BizException(ErrorCode.ORDER_CANNOT_DELETE);
        }
        orderMapper.deleteById(id);
        log.info("订单已删除, id={}", id);
    }

    @Override
    public OrderVO getById(Long id) {
        var entity = orderMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        return OrderConverter.toVO(entity);
    }

    @Override
    public OrderVO getByOrderNo(String orderNo) {
        var entity = orderMapper.selectOne(new LambdaQueryWrapper<OrderDO>().eq(OrderDO::getOrderNo, orderNo));
        if (entity == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        return OrderConverter.toVO(entity);
    }

    @Override
    public List<OrderVO> list() {
        return orderMapper.selectList(null).stream()
                .map(OrderConverter::toVO)
                .toList();
    }

    private static void assertFeignOk(Result<?> result) {
        if (result == null || result.getCode() != 200) {
            int code = result != null ? result.getCode() : 0;
            String message = result != null ? result.getMessage() : "调用商品服务失败";
            throw switch (code) {
                case 20001 -> new BizException(ErrorCode.PRODUCT_NOT_FOUND, message);
                case 20002 -> new BizException(ErrorCode.STOCK_NOT_ENOUGH, message);
                default -> new BizException(ErrorCode.SYSTEM_ERROR, message);
            };
        }
    }
}
