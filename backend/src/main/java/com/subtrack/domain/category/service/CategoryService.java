package com.subtrack.domain.category.service;

import com.subtrack.domain.category.dao.CategoryDao;
import com.subtrack.domain.category.dto.CategoryResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryDao categoryDao;

    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getDefaultCategories() {
        return categoryDao.findDefaultCategories()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
