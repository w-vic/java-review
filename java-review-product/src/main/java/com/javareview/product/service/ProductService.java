package com.javareview.product.service;

import com.javareview.product.model.dto.ProductCreateDTO;
import com.javareview.product.model.dto.ProductUpdateDTO;
import com.javareview.product.model.vo.ProductVO;

import java.util.List;

/**
 * 商品服务接口
 *
 * @author java-review
 */
public interface ProductService {

    ProductVO create(ProductCreateDTO dto);

    ProductVO getById(Long id);

    List<ProductVO> search(String keyword);

    ProductVO update(Long id, ProductUpdateDTO dto);

    void delete(Long id);
}
