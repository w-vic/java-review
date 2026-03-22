package com.javareview.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javareview.order.model.entity.OrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单 Mapper
 *
 * @author java-review
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {

    /**
     * 行锁，防止并发重复付款导致重复扣库存
     */
    @Select("SELECT * FROM orders WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    OrderDO selectByIdForUpdate(@Param("id") Long id);
}
