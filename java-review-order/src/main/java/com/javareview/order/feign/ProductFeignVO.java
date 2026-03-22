package com.javareview.order.feign;

import java.math.BigDecimal;

/**
 * 商品服务 Feign 返回值
 * <p>
 * 这里单独定义一个 record 而非复用 product 模块的 VO，
 * 是因为微服务之间不应直接依赖对方的模块 JAR——
 * 否则就失去了服务独立部署的意义。
 * 实际项目中也可以抽出一个 API 模块来共享接口定义。
 *
 * @author java-review
 */
public record ProductFeignVO(
        Long id,
        String name,
        BigDecimal price,
        Integer stock
) {
}
