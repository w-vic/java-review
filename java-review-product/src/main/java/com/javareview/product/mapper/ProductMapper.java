package com.javareview.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javareview.product.model.entity.ProductDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品 Mapper
 * <p>
 * 继承 BaseMapper 即可获得常用的 CRUD 方法（insert、selectById、updateById 等），
 * 无需手写 XML。如有复杂 SQL，可在 resources/mapper/ 下新建 XML 文件。
 *
 * @author java-review
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {
}
