package com.javareview.order.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 订单事务事件：仅处理「下单事务回滚时释放幂等 Redis 键」。
 */
@Component
@RequiredArgsConstructor
public class OrderTransactionEventListener {

    private final StringRedisTemplate redisTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void releaseIdempotentKey(OrderIdempotentKeyEvent event) {
        redisTemplate.delete(event.redisKey());
    }
}
