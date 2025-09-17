package com.example.myapplication.data.database;

import android.util.Log;

import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.domain.models.Category;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepositoryFirebaseImpl implements CategoryRepository {

    private static final String TAG = "CategoryRepoFirebase";
    private static final String USERS_COLLECTION = "users";
    private static final String CATEGORIES_COLLECTION = "categories";

    private final FirebaseFirestore db;

    public CategoryRepositoryFirebaseImpl() {
        this.db = FirebaseFirestore.getInstance();
    }

    private CollectionReference getCategoriesCollection(String userId) {
        return db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(CATEGORIES_COLLECTION);
    }

    @Override
    public void insertCategory(Category category, String userId, OnCategoryAddedListener listener) {
        getCategoriesCollection(userId)
                .add(category)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Category added with ID: " + documentReference.getId());
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding category", e);
                    listener.onFailure(e);
                });
    }

    @Override
    public void getAllCategories(String userId, OnCategoriesLoadedListener listener) {
        Log.d(TAG, "Fetching categories for userId: " + userId);

        getCategoriesCollection(userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Documents fetched: " + queryDocumentSnapshots.size());
                    List<Category> categories = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(TAG, "Doc ID: " + document.getId() + " => " + document.getData());
                        Category category = document.toObject(Category.class);
                        category.setId(document.getId());
                        categories.add(category);
                    }
                    listener.onSuccess(categories);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting categories", e);
                    listener.onFailure(e);
                });
    }


    @Override
    public void deleteCategory(String categoryId, String userId, OnCategoryDeletedListener listener) {
        getCategoriesCollection(userId)
                .document(categoryId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Category successfully deleted!");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting category", e);
                    listener.onFailure(e);
                });
    }

    @Override
    public void updateCategory(Category category, String userId, OnCategoryUpdatedListener listener) {
        if (category.getId() == null) {
            listener.onFailure(new IllegalArgumentException("Category ID cannot be null for update."));
            return;
        }

        getCategoriesCollection(userId)
                .document(category.getId())
                .set(category)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Category successfully updated!");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating category", e);
                    listener.onFailure(e);
                });
    }

    @Override
    public void getCategoryById(String categoryId, String userId, OnCategoriesLoadedListener listener) {
    }
}