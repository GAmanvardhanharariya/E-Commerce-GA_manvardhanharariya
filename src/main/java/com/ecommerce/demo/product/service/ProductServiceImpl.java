package com.ecommerce.demo.product.service;

import com.ecommerce.demo.category.entity.Category;
import com.ecommerce.demo.category.repository.CategoryRepository;

import com.ecommerce.demo.product.dto.CategoryMiniResponse;
import com.ecommerce.demo.product.dto.ProductCreateRequest;
import com.ecommerce.demo.product.dto.ProductResponse;
import com.ecommerce.demo.product.dto.ProductUpdateRequest;

import com.ecommerce.demo.product.entity.Product;
import com.ecommerce.demo.product.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    public ProductServiceImpl(ProductRepository productRepo, CategoryRepository categoryRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public ProductResponse create(ProductCreateRequest req) {

        Product p = Product.builder()
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .stock(req.stock())
                .active(true)
                .build();

        // Attach category if categoryId provided
        if (req.categoryId() != null) {
            Category cat = categoryRepo.findById(req.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found: " + req.categoryId()));
            p.setCategory(cat);
        }

        Product saved = productRepo.save(p);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        return toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> listAll() {
        return productRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public ProductResponse update(Long id, ProductUpdateRequest req) {

        Product p = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        p.setName(req.name());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setStock(req.stock());
        p.setActive(req.active());

        // Category update rules (simple v1):
        // - if categoryId is null => remove category
        // - else set to that category
        if (req.categoryId() == null) {
            p.setCategory(null);
        } else {
            Category cat = categoryRepo.findById(req.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found: " + req.categoryId()));
            p.setCategory(cat);
        }

        return toResponse(p);
    }

    @Override
    public void delete(Long id) {
        if (!productRepo.existsById(id)) {
            throw new EntityNotFoundException("Product not found: " + id);
        }
        productRepo.deleteById(id);
    }

    private ProductResponse toResponse(Product p) {

        CategoryMiniResponse category = null;
        if (p.getCategory() != null) {
            category = new CategoryMiniResponse(
                    p.getCategory().getId(),
                    p.getCategory().getName(),
                    p.getCategory().getSlug()
            );
        }

        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStock(),
                p.getActive(),
                category,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
