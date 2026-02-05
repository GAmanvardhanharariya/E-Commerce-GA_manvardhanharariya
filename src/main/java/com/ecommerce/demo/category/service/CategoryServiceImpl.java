package com.ecommerce.demo.category.service;

import com.ecommerce.demo.category.dto.*;
import com.ecommerce.demo.category.entity.Category;
import com.ecommerce.demo.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;

    public CategoryServiceImpl(CategoryRepository repo) {
        this.repo = repo;
    }

    @Override
    public CategoryResponse create(CategoryCreateRequest req) {
        if (repo.existsBySlug(req.slug())) {
            throw new IllegalArgumentException("Category slug already exists: " + req.slug());
        }

        Category c = Category.builder()
                .name(req.name())
                .slug(req.slug())
                .description(req.description())
                .active(true)
                .build();

        return toResponse(repo.save(c));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category c = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        return toResponse(c);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<CategoryResponse> listAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public CategoryResponse update(Long id, CategoryUpdateRequest req) {
        Category c = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));

        // slug uniqueness check (only if changed)
        if (!c.getSlug().equals(req.slug()) && repo.existsBySlug(req.slug())) {
            throw new IllegalArgumentException("Category slug already exists: " + req.slug());
        }

        c.setName(req.name());
        c.setSlug(req.slug());
        c.setDescription(req.description());
        c.setActive(req.active());

        return toResponse(c);
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Category not found: " + id);
        }
        repo.deleteById(id);
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getSlug(),
                c.getDescription(),
                c.getActive(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
