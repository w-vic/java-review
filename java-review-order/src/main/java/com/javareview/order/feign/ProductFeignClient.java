package com.javareview.order.feign;

import com.javareview.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务 Feign 客户端
 * <p>
 * @FeignClient 声明要调用的远程服务：
 * - name：目标服务在 Nacos 中的注册名（spring.application.name）
 * - path：该服务的 Controller 基础路径
 * <p>
 * Feign 会根据 name 从 Nacos 获取服务实例列表，
 * 再通过 LoadBalancer 做负载均衡，自动选择一个实例发起 HTTP 请求。
 * 开发者只需要写接口，无需关心 HTTP 连接管理。
 *
 * @author java-review
 */
@FeignClient(name = "java-review-product", path = "/product")
public interface ProductFeignClient {

    /**
     * 查询商品详情
     * <p>
     * 这里的返回类型直接使用 Result 包装，与 ProductController 的返回结构一致。
     * Feign 会自动反序列化 JSON 响应。
     */
    @GetMapping("/{id}")
    Result<ProductFeignVO> getById(@PathVariable("id") Long id);
}
