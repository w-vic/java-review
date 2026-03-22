package com.javareview.order.service;

import com.javareview.order.model.dto.CreateOrderDTO;
import com.javareview.order.model.vo.OrderVO;

import java.util.List;

/**
 * 订单服务接口
 *
 * @author java-review
 */
public interface OrderService {

    OrderVO create(CreateOrderDTO dto);

    OrderVO getById(Long id);

    List<OrderVO> list();
}
