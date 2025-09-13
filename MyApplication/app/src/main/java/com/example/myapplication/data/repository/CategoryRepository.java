package com.example.myapplication.data.repository;

import com.example.myapplication.domain.models.Category;
import java.util.List;

public interface CategoryRepository {
    long insertCategory(Category category);
    Category getCategoryById(int categoryId);
    List<Category> getAllCategories();
    int updateCategory(Category category);
    int deleteCategory(int categoryId);
}