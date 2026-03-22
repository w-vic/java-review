package com.javareview.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商品服务启动类
 * <p>
 * @SpringBootApplication 是一个组合注解，等价于：
 * - @Configuration：声明当前类为配置类
 * - @EnableAutoConfiguration：开启自动配置（根据 classpath 下的 starter 自动装配 Bean）
 * - @ComponentScan：扫描当前包及子包下的 @Component、@Service 等注解
 *
 * @author java-review
 */
@SpringBootApplication(scanBasePackages = "com.javareview")
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
