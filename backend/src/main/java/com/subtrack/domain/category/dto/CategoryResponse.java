package com.subtrack.domain.category.dto;

import com.subtrack.domain.category.vo.Category;

public class CategoryResponse {

    private final Long categoryId;
    private final String name;
    private final String colorCode;
    private final String icon;
    private final Integer sortOrder;

    public CategoryResponse(Long categoryId, String name, String colorCode, String icon, Integer sortOrder) {
        this.categoryId = categoryId;
        this.name = name;
        this.colorCode = colorCode;
        this.icon = icon;
        this.sortOrder = sortOrder;
    }

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getCategoryId(),
                category.getName(),
                category.getColorCode(),
                category.getIcon(),
                category.getSortOrder()
        );
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getIcon() {
        return icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}
