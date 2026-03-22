package com.javareview.product.service.impl;

import com.javareview.common.exception.BizException;
import com.javareview.common.result.ErrorCode;
import com.javareview.product.convert.ProductConverter;
import com.javareview.product.es.ProductSearchService;
import com.javareview.product.mapper.ProductMapper;
import com.javareview.product.model.dto.ProductCreateDTO;
import com.javareview.product.model.dto.ProductUpdateDTO;
import com.javareview.product.model.vo.ProductVO;
import com.javareview.product.event.ProductCreatedEvent;
import com.javareview.product.event.ProductDeletedEvent;
import com.javareview.product.event.ProductStockCacheInvalidateEvent;
import com.javareview.product.event.ProductUpdatedEvent;
import com.javareview.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务实现
 * <p>
 * 演示要点：
 * 1. MyBatis-Plus CRUD
 * 2. Redis 缓存（Cache-Aside 模式：先查缓存，未命中再查 DB，写入缓存）
 * 3. Elasticsearch 全文搜索
 *
 * @author java-review
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final StringRedisTemplate redisTemplate;
    private final ProductSearchService searchService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    /** Redis 缓存 key 前缀 */
    private static final String CACHE_KEY_PREFIX = "product:";
    private static final long CACHE_TTL_SECONDS = 3600;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO create(ProductCreateDTO dto) {
        var entity = ProductConverter.toEntity(dto);
        productMapper.insert(entity);

        eventPublisher.publishEvent(new ProductCreatedEvent(ProductConverter.toDocument(entity)));

        log.info("商品创建成功, id={}", entity.getId());
        return ProductConverter.toVO(entity);
    }

    /**
     * 查询商品（Cache-Aside 模式）
     * <p>
     * 流程：Redis 缓存 → 命中则直接返回 → 未命中则查 MySQL → 写回缓存
     * 这是最常见的缓存策略，适合读多写少的场景。
     */
    @Override
    public ProductVO getById(Long id) {
        var cacheKey = CACHE_KEY_PREFIX + id;

        // 1. 先查 Redis 缓存
        var cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("缓存命中, key={}", cacheKey);
            return deserialize(cached);
        }

        // 2. 缓存未命中，查数据库
        var entity = productMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 3. 写回缓存，设置过期时间避免内存无限膨胀
        var vo = ProductConverter.toVO(entity);
        redisTemplate.opsForValue().set(cacheKey, serialize(vo), CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        log.debug("缓存回填, key={}", cacheKey);

        return vo;
    }

    /**
     * 商品搜索（Elasticsearch）
     * <p>
     * ES 擅长全文检索和复杂查询，MySQL 的 LIKE '%keyword%' 无法利用索引，
     * 数据量大时性能极差。生产环境中搜索类需求应走 ES。
     */
    @Override
    public List<ProductVO> search(String keyword) {
        var docs = searchService.search(keyword);
        return docs.stream()
                .map(ProductConverter::toVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO update(Long id, ProductUpdateDTO dto) {
        var entity = productMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        ProductConverter.applyUpdate(entity, dto);
        productMapper.updateById(entity);

        eventPublisher.publishEvent(new ProductUpdatedEvent(id, CACHE_KEY_PREFIX + id, ProductConverter.toDocument(entity)));

        log.info("商品更新成功, id={}", id);
        return ProductConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        productMapper.deleteById(id);
        eventPublisher.publishEvent(new ProductDeletedEvent(id, CACHE_KEY_PREFIX + id));
        log.info("商品删除成功, id={}", id);
    }

    /**
     * 扣减库存（订单付款时由 Feign 调用；失败抛 {@link BizException} 便于调用方识别）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStock(Long productId, Integer quantity) {
        var entity = productMapper.selectById(productId);
        if (entity == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (entity.getStock() < quantity) {
            throw new BizException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        entity.setStock(entity.getStock() - quantity);
        entity.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(entity);

        eventPublisher.publishEvent(new ProductStockCacheInvalidateEvent(CACHE_KEY_PREFIX + productId));
        log.info("库存扣减成功, productId={}, 扣减数量={}, 剩余库存={}", productId, quantity, entity.getStock());
    }

    // ==================== 序列化工具方法 ====================

    private String serialize(ProductVO vo) {
        try {
            return objectMapper.writeValueAsString(vo);
        } catch (Exception e) {
            throw new RuntimeException("序列化失败", e);
        }
    }

    private ProductVO deserialize(String json) {
        try {
            return objectMapper.readValue(json, ProductVO.class);
        } catch (Exception e) {
            throw new RuntimeException("反序列化失败", e);
        }
    }
}
