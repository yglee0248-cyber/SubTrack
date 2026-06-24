package com.subtrack.domain.category.dao;

import com.subtrack.domain.category.vo.Category;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CategoryDao {

    List<Category> findDefaultCategories();

    int existsDefaultCategoryById(@Param("categoryId") Long categoryId);
}
