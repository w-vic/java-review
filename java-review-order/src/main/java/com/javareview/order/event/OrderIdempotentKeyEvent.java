package com.javareview.order.event;

/**
 * 已占用幂等 Redis 键；仅当事务回滚时由监听器删除，以便客户端重试。
 */
public record OrderIdempotentKeyEvent(String redisKey) {}
