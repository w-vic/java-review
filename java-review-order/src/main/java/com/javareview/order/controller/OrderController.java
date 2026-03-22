package com.javareview.order.controller;

import com.javareview.common.result.Result;
import com.javareview.order.model.dto.CreateOrderDTO;
import com.javareview.order.model.dto.OrderUpdateDTO;
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

    @GetMapping("/{id}")
    public Result<OrderVO> getById(@PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }

    @GetMapping("/by-no/{orderNo}")
    public Result<OrderVO> getByOrderNo(@PathVariable String orderNo) {
        return Result.ok(orderService.getByOrderNo(orderNo));
    }

    /** 静态路径需写在 `/{id}` 之前，避免 `list` 被当成 id */
    @GetMapping("/list")
    public Result<List<OrderVO>> list() {
        return Result.ok(orderService.list());
    }

    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody CreateOrderDTO dto) {
        return Result.ok(orderService.create(dto));
    }

    @PostMapping("/{id}/pay")
    public Result<OrderVO> pay(@PathVariable Long id) {
        return Result.ok(orderService.pay(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<OrderVO> cancel(@PathVariable Long id) {
        return Result.ok(orderService.cancel(id));
    }

    @PutMapping("/{id}")
    public Result<OrderVO> update(@PathVariable Long id, @Valid @RequestBody OrderUpdateDTO dto) {
        return Result.ok(orderService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return Result.ok();
    }


}
