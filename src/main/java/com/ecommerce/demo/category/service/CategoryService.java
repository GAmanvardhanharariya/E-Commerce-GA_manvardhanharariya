package com.ecommerce.demo.category.service;

import com.ecommerce.demo.category.dto.*;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryCreateRequest req);
    CategoryResponse getById(Long id);
    List<CategoryResponse> listAll();
    CategoryResponse update(Long id, CategoryUpdateRequest req);
    void delete(Long id);
}
