package com.ecommerce.demo.product.service;

import com.ecommerce.demo.product.dto.*;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductCreateRequest req);
    ProductResponse getById(Long id);
    List<ProductResponse> listAll();
    ProductResponse update(Long id, ProductUpdateRequest req);
    void delete(Long id);
}
