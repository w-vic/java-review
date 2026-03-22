package com.javareview.product.event;

import com.javareview.product.es.ProductDocument;

/**
 * 商品已更新，事务提交后淘汰缓存并同步 ES
 */
public record ProductUpdatedEvent(long productId, String cacheKey, ProductDocument document) {}
