package com.javareview.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javareview.order.model.entity.OrderDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper
 *
 * @author java-review
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {
}
