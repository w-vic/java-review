package com.javareview.order.controller;

import com.javareview.common.result.Result;
import com.javareview.order.model.dto.CreateOrderDTO;
import com.javareview.order.model.vo.OrderVO;
import com.javareview.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单接口
 *
 * @author java-review
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody CreateOrderDTO dto) {
        return Result.ok(orderService.create(dto));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> getById(@PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }

    @GetMapping("/list")
    public Result<List<OrderVO>> list() {
        return Result.ok(orderService.list());
    }
}
