package com.javareview.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类
 * <p>
 * @EnableFeignClients 开启 OpenFeign 支持。
 * Feign 是声明式 HTTP 客户端：用接口 + 注解描述远程调用，
 * 框架自动生成 HTTP 请求代码，比手写 RestTemplate 简洁很多。
 *
 * @author java-review
 */
@SpringBootApplication(scanBasePackages = "com.javareview")
@EnableFeignClients
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
