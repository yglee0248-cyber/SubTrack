package com.subtrack.domain.category.dao;

import com.subtrack.domain.category.vo.Category;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryDao {

    List<Category> findDefaultCategories();
}
