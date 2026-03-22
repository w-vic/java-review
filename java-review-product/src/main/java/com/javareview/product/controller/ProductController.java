package com.javareview.product.controller;

import com.javareview.common.dto.StockDeductDTO;
import com.javareview.common.result.Result;
import com.javareview.product.model.dto.ProductCreateDTO;
import com.javareview.product.model.dto.ProductUpdateDTO;
import com.javareview.product.model.vo.ProductVO;
import com.javareview.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品接口
 * <p>
 * RESTful 风格：
 * - POST   /product        新增
 * - GET    /product/{id}   查询（走 Redis 缓存）
 * - GET    /product/search 搜索（走 Elasticsearch）
 * - PUT    /product/{id}   更新
 * - DELETE /product/{id}   删除
 *
 * @author java-review
 */
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public Result<ProductVO> create(@Valid @RequestBody ProductCreateDTO dto) {
        return Result.ok(productService.create(dto));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> getById(@PathVariable Long id) {
        return Result.ok(productService.getById(id));
    }

    /**
     * 商品搜索——走 Elasticsearch，支持按名称/描述模糊匹配
     */
    @GetMapping("/search")
    public Result<List<ProductVO>> search(@RequestParam String keyword) {
        return Result.ok(productService.search(keyword));
    }

    /**
     * 查询商品库存——供订单服务 Feign 调用，下单前校验库存是否充足
     */
    @GetMapping("/{id}/stock")
    public Result<Integer> getStock(@PathVariable Long id) {
        var product = productService.getById(id);
        return Result.ok(product.stock());
    }

    /**
     * 扣减库存——供订单服务付款成功后 Feign 调用
     */
    @PostMapping("/{id}/stock/deduct")
    public Result<Void> deductStock(@PathVariable Long id, @Valid @RequestBody StockDeductDTO dto) {
        productService.deductStock(id, dto.quantity());
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<ProductVO> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateDTO dto) {
        return Result.ok(productService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok();
    }
}
