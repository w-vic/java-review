package com.javareview.product.es;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品 ES 搜索服务
 * <p>
 * 使用 ElasticsearchOperations（Spring Data ES 提供）进行索引读写。
 * NativeQuery 支持构建复杂的 ES 查询（bool、match、range 等）。
 *
 * @author java-review
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 保存/更新商品索引文档
     * <p>
     * ES 的 save 是 upsert 语义：文档存在则更新，不存在则新增。
     */
    public void save(ProductDocument document) {
        elasticsearchOperations.save(document);
        log.info("ES 索引写入成功, id={}", document.getId());
    }

    /**
     * 多字段全文搜索
     * <p>
     * multi_match 查询会同时在 name 和 description 两个字段上做分词匹配，
     * 返回相关度最高的结果。
     */
    public List<ProductDocument> search(String keyword) {
        var query = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .fields("name", "description")
                                .query(keyword)
                        )
                )
                .build();

        var hits = elasticsearchOperations.search(query, ProductDocument.class);
        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
    }

    public void deleteById(Long id) {
        elasticsearchOperations.delete(String.valueOf(id), ProductDocument.class);
        log.info("ES 索引删除成功, id={}", id);
    }
}
