package com.example.myapplication.data.repository;

import com.example.myapplication.domain.models.Category;
import java.util.List;

public interface CategoryRepository {

    // Callback interfejsi za asinhroni rad
    interface OnCategoryAddedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    interface OnCategoriesLoadedListener {
        void onSuccess(List<Category> categories);
        void onFailure(Exception e);
    }

    interface OnCategoryDeletedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    interface OnCategoryUpdatedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    void insertCategory(Category category, String userId, OnCategoryAddedListener listener);
    void getCategoryById(String categoryId, String userId, OnCategoriesLoadedListener listener);
    void getAllCategories(String userId, OnCategoriesLoadedListener listener);
    void updateCategory(Category category, String userId, OnCategoryUpdatedListener listener);
    void deleteCategory(String categoryId, String userId, OnCategoryDeletedListener listener);
}