/*package com.example.myapplication.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.domain.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepositorySQLiteImpl implements CategoryRepository {

    private DatabaseHelper dbHelper;

    public CategoryRepositorySQLiteImpl(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    @Override
    public long insertCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_CATEGORY_NAME, category.getName());
        cv.put(DatabaseHelper.COLUMN_CATEGORY_COLOR, category.getColor());
        long insertId = db.insert(DatabaseHelper.TABLE_CATEGORIES, null, cv);
        db.close();
        return insertId;
    }

    @Override
    public Category getCategoryById(int categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                null, DatabaseHelper.COLUMN_CATEGORY_ID + " =?",
                new String[]{String.valueOf(categoryId)}, null, null, null);

        Category category = null;
        if (cursor != null && cursor.moveToFirst()) {
            category = cursorToCategory(cursor);
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return category;
    }

    @Override
    public List<Category> getAllCategories() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Category> categories = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                categories.add(cursorToCategory(cursor));
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return categories;
    }

    @Override
    public int updateCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_CATEGORY_NAME, category.getName());
        cv.put(DatabaseHelper.COLUMN_CATEGORY_COLOR, category.getColor());
        int update = db.update(DatabaseHelper.TABLE_CATEGORIES, cv,
                DatabaseHelper.COLUMN_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(category.getId())});
        db.close();
        return update;
    }

    @Override
    public int deleteCategory(int categoryId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int delete = db.delete(DatabaseHelper.TABLE_CATEGORIES,
                DatabaseHelper.COLUMN_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(categoryId)});
        db.close();
        return delete;
    }

    private Category cursorToCategory(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
        int color = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_COLOR));
        return new Category(id, name, color);
    }
}*/