package com.javareview.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API 网关启动类
 * <p>
 * Spring Cloud Gateway 是微服务的统一入口，所有外部请求先经过 Gateway，
 * 由它根据路由规则转发到对应的下游服务。
 * <p>
 * 核心概念：
 * - Route（路由）：一个路由 = 一条转发规则（条件 + 目标地址）
 * - Predicate（断言）：匹配条件，如路径前缀、Header、参数等
 * - Filter（过滤器）：请求/响应的处理链，如添加 Header、限流、日志等
 *
 * @author java-review
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
