package com.javareview.order.service;

import com.javareview.order.model.dto.CreateOrderDTO;
import com.javareview.order.model.dto.OrderUpdateDTO;
import com.javareview.order.model.vo.OrderVO;

import java.util.List;

/**
 * 订单服务接口
 *
 * @author java-review
 */
public interface OrderService {

    OrderVO create(CreateOrderDTO dto);

    OrderVO pay(Long id);

    OrderVO cancel(Long id);

    OrderVO update(Long id, OrderUpdateDTO dto);

    void delete(Long id);

    OrderVO getById(Long id);

    OrderVO getByOrderNo(String orderNo);

    List<OrderVO> list();
}
