package com.javareview.product.convert;

import com.javareview.product.es.ProductDocument;
import com.javareview.product.model.dto.ProductCreateDTO;
import com.javareview.product.model.dto.ProductUpdateDTO;
import com.javareview.product.model.entity.ProductDO;
import com.javareview.product.model.vo.ProductVO;

/**
 * 商品对象转换器
 * <p>
 * 集中管理 DO/DTO/VO/ES Document 之间的转换逻辑，
 * 避免在 Service 层散落大量 setter 调用。
 *
 * @author java-review
 */
public final class ProductConverter {

    private ProductConverter() {
    }

    public static ProductDO toEntity(ProductCreateDTO dto) {
        var entity = new ProductDO();
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setPrice(dto.price());
        entity.setStock(dto.stock());
        return entity;
    }

    public static ProductVO toVO(ProductDO entity) {
        return new ProductVO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStock(),
                entity.getCreateTime(),
                entity.getUpdateTime()
        );
    }

    public static ProductVO toVO(ProductDocument doc) {
        return new ProductVO(
                doc.getId(),
                doc.getName(),
                doc.getDescription(),
                doc.getPrice(),
                doc.getStock(),
                null,
                null
        );
    }

    public static ProductDocument toDocument(ProductDO entity) {
        var doc = new ProductDocument();
        doc.setId(entity.getId());
        doc.setName(entity.getName());
        doc.setDescription(entity.getDescription());
        doc.setPrice(entity.getPrice());
        doc.setStock(entity.getStock());
        return doc;
    }

    /**
     * 将更新 DTO 中非 null 的字段应用到实体上（部分更新）
     */
    public static void applyUpdate(ProductDO entity, ProductUpdateDTO dto) {
        if (dto.name() != null) {
            entity.setName(dto.name());
        }
        if (dto.description() != null) {
            entity.setDescription(dto.description());
        }
        if (dto.price() != null) {
            entity.setPrice(dto.price());
        }
        if (dto.stock() != null) {
            entity.setStock(dto.stock());
        }
    }
}
