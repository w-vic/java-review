package com.javareview.product.event;

import com.javareview.product.es.ProductDocument;

/**
 * 商品已持久化，事务提交后同步 ES
 */
public record ProductCreatedEvent(ProductDocument document) {}
