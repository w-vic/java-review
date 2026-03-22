package com.javareview.product.event;

/**
 * 商品已从库中删除，事务提交后淘汰缓存并删 ES 文档
 */
public record ProductDeletedEvent(long productId, String cacheKey) {}
