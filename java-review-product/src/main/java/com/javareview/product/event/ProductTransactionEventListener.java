package com.javareview.product.event;

import com.javareview.product.es.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 事务边界外的副作用：仅用 {@link TransactionalEventListener} 声明阶段，无手动 registerSynchronization。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTransactionEventListener {

    private final StringRedisTemplate redisTemplate;
    private final ProductSearchService searchService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(ProductCreatedEvent event) {
        try {
            searchService.save(event.document());
        } catch (Exception e) {
            log.error("商品创建后同步 ES 失败, id={}", event.document().getId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUpdated(ProductUpdatedEvent event) {
        redisTemplate.delete(event.cacheKey());
        try {
            searchService.save(event.document());
        } catch (Exception e) {
            log.error("商品更新后同步 ES 失败, id={}", event.productId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeleted(ProductDeletedEvent event) {
        redisTemplate.delete(event.cacheKey());
        try {
            searchService.deleteById(event.productId());
        } catch (Exception e) {
            log.error("商品删除后同步 ES 失败, id={}", event.productId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockChanged(ProductStockCacheInvalidateEvent event) {
        redisTemplate.delete(event.cacheKey());
    }
}
