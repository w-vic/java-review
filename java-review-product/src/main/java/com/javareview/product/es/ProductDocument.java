package com.javareview.product.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;

/**
 * 商品 ES 文档
 * <p>
 * @Document 指定 ES 索引名。ES 中的"索引"类似 MySQL 的"表"。
 * @Field 定义字段映射：
 * - FieldType.Text：会被分词，适合全文搜索（如商品名称）
 * - FieldType.Keyword：不分词，适合精确匹配（如状态、ID）
 * - analyzer = "ik_max_word"：IK 中文分词器，按最细粒度切分，适合搜索场景
 *   需要在 ES 中安装 elasticsearch-analysis-ik 插件
 *
 * @author java-review
 */
@Data
@Document(indexName = "product")
public class ProductDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String name;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String description;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Integer)
    private Integer stock;
}
