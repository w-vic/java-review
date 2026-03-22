package com.javareview.product.event;

/**
 * 库存已扣减提交，事务提交后淘汰商品缓存
 */
public record ProductStockCacheInvalidateEvent(String cacheKey) {}
