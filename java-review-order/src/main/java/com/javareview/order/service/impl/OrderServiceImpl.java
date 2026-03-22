package com.javareview.order.service.impl;

import com.javareview.common.exception.BizException;
import com.javareview.common.result.ErrorCode;
import com.javareview.order.convert.OrderConverter;
import com.javareview.order.feign.ProductFeignClient;
import com.javareview.order.mapper.OrderMapper;
import com.javareview.order.model.dto.CreateOrderDTO;
import com.javareview.order.model.entity.OrderDO;
import com.javareview.order.model.vo.OrderVO;
import com.javareview.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务实现
 * <p>
 * 演示要点：
 * 1. Redis SETNX 实现幂等性（防止重复下单）
 * 2. OpenFeign 远程调用商品服务
 * 3. JDK 17 switch expression
 *
 * @author java-review
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final StringRedisTemplate redisTemplate;
    private final ProductFeignClient productFeignClient;

    /** 幂等键前缀，过期时间设为 24 小时 */
    private static final String IDEMPOTENT_KEY_PREFIX = "order:idempotent:";
    private static final long IDEMPOTENT_TTL_HOURS = 24;

    /**
     * 创建订单（幂等性设计）
     * <p>
     * 幂等原理：用 Redis SETNX（SET if Not eXists）实现。
     * - 第一次请求：SETNX 成功 → 执行业务逻辑
     * - 重复请求：SETNX 失败（key 已存在）→ 直接拒绝
     * 这样即使客户端因网络超时而重试，也不会产生重复订单。
     */
    @Override
    public OrderVO create(CreateOrderDTO dto) {
        // 1. 幂等性校验：尝试用 orderNo 作为 Redis key 做 SETNX
        var idempotentKey = IDEMPOTENT_KEY_PREFIX + dto.orderNo();
        var isFirstRequest = redisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", IDEMPOTENT_TTL_HOURS, TimeUnit.HOURS);

        if (Boolean.FALSE.equals(isFirstRequest)) {
            throw new BizException(ErrorCode.ORDER_DUPLICATE);
        }

        // 2. 通过 OpenFeign 调用商品服务，获取商品信息
        var productResult = productFeignClient.getById(dto.productId());
        if (productResult.getCode() != 200 || productResult.getData() == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        var product = productResult.getData();

        // 3. 构建订单实体并入库
        var entity = OrderConverter.toEntity(dto, product);
        orderMapper.insert(entity);

        log.info("订单创建成功, orderNo={}, productId={}", dto.orderNo(), dto.productId());
        return OrderConverter.toVO(entity);
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
    public List<OrderVO> list() {
        return orderMapper.selectList(null).stream()
                .map(OrderConverter::toVO)
                .toList();
    }
}
