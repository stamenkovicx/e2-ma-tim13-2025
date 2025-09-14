package com.example.myapplication.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.domain.models.Category;
import com.example.myapplication.domain.models.DifficultyType;
import com.example.myapplication.domain.models.ImportanceType;
import com.example.myapplication.domain.models.Task;
import com.example.myapplication.domain.models.TaskStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Konkretna implementacija TaskRepository interfejsa
 * koja koristi SQLite bazu podataka.
 */
public class TaskRepositorySQLiteImpl implements TaskRepository {

    private DatabaseHelper dbHelper;
    private CategoryRepository categoryRepository;


    public TaskRepositorySQLiteImpl(Context context) {
        // Koristimo postojeći DatabaseHelper da dobijemo pristup bazi
        this.dbHelper = new DatabaseHelper(context);
        this.categoryRepository = new CategoryRepositorySQLiteImpl(context);

    }

    @Override
    public long insertTask(Task task) {
        long insert = -1;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();

            cv.put(DatabaseHelper.COLUMN_TASK_NAME, task.getName());
            cv.put(DatabaseHelper.COLUMN_TASK_DESCRIPTION, task.getDescription());
            cv.put(DatabaseHelper.COLUMN_TASK_CATEGORY_ID,
                    task.getCategory() != null ? task.getCategory().getId() : -1);
            cv.put(DatabaseHelper.COLUMN_TASK_FREQUENCY, task.getFrequency());
            cv.put(DatabaseHelper.COLUMN_TASK_INTERVAL, task.getInterval());
            cv.put(DatabaseHelper.COLUMN_TASK_INTERVAL_UNIT, task.getIntervalUnit());
            cv.put(DatabaseHelper.COLUMN_TASK_START_DATE, formatDate(task.getStartDate()));
            cv.put(DatabaseHelper.COLUMN_TASK_END_DATE, formatDate(task.getEndDate()));
            cv.put(DatabaseHelper.COLUMN_TASK_EXECUTION_TIME, formatTime(task.getExecutionTime()));
            cv.put(DatabaseHelper.COLUMN_TASK_DIFFICULTY,
                    task.getDifficulty() != null ? task.getDifficulty().name() : "UNKNOWN");
            cv.put(DatabaseHelper.COLUMN_TASK_IMPORTANCE,
                    task.getImportance() != null ? task.getImportance().name() : "UNKNOWN");
            cv.put(DatabaseHelper.COLUMN_TASK_XP_VALUE, task.getXpValue());
            cv.put(DatabaseHelper.COLUMN_TASK_STATUS, task.getStatus().name());
            cv.put(DatabaseHelper.COLUMN_TASK_COMPLETION_DATE, formatDate(task.getCompletionDate()));

            insert = db.insert(DatabaseHelper.TABLE_TASKS, null, cv);
            if (insert == -1) {
                android.util.Log.e("TaskRepo", "Insert task failed!");
            } else {
                android.util.Log.d("TaskRepo", "Task insertovan sa ID=" + insert);
            }
        } catch (Exception e) {
            android.util.Log.e("TaskRepo", "Greška pri insertTask", e);
        } finally {
            if (db != null) db.close();
        }
        return insert;
    }


    @Override
    public Task getTaskById(long taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Task task = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_TASKS,
                null, DatabaseHelper.COLUMN_TASK_ID + " =?",
                new String[]{String.valueOf(taskId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            task = cursorToTask(cursor);
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return task;
    }

    @Override
    public List<Task> getAllTasks() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Task> tasks = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TASKS,
                null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                tasks.add(cursorToTask(cursor));
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return tasks;
    }

    @Override
    public int updateTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DatabaseHelper.COLUMN_TASK_NAME, task.getName());
        cv.put(DatabaseHelper.COLUMN_TASK_DESCRIPTION, task.getDescription());
        cv.put(DatabaseHelper.COLUMN_TASK_CATEGORY_ID, task.getCategory().getId());
        cv.put(DatabaseHelper.COLUMN_TASK_FREQUENCY, task.getFrequency());
        cv.put(DatabaseHelper.COLUMN_TASK_INTERVAL, task.getInterval());
        cv.put(DatabaseHelper.COLUMN_TASK_INTERVAL_UNIT, task.getIntervalUnit());
        cv.put(DatabaseHelper.COLUMN_TASK_START_DATE, formatDate(task.getStartDate()));
        cv.put(DatabaseHelper.COLUMN_TASK_END_DATE, formatDate(task.getEndDate()));
        cv.put(DatabaseHelper.COLUMN_TASK_EXECUTION_TIME, formatTime(task.getExecutionTime()));
        cv.put(DatabaseHelper.COLUMN_TASK_DIFFICULTY, task.getDifficulty().name());
        cv.put(DatabaseHelper.COLUMN_TASK_IMPORTANCE, task.getImportance().name());
        cv.put(DatabaseHelper.COLUMN_TASK_XP_VALUE, task.getXpValue());
        cv.put(DatabaseHelper.COLUMN_TASK_STATUS, task.getStatus().name());
        cv.put(DatabaseHelper.COLUMN_TASK_COMPLETION_DATE, formatDate(task.getCompletionDate()));

        int update = db.update(DatabaseHelper.TABLE_TASKS, cv,
                DatabaseHelper.COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
        db.close();
        return update;
    }

    @Override
    public int deleteTask(long taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int delete = db.delete(DatabaseHelper.TABLE_TASKS,
                DatabaseHelper.COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)});
        db.close();
        return delete;
    }

    private Task cursorToTask(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_NAME));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_DESCRIPTION));
        int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_CATEGORY_ID));

        Category category = getCategoryById(categoryId);

        String frequency = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_FREQUENCY));
        int interval = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_INTERVAL));
        String intervalUnit = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_INTERVAL_UNIT));

        Date startDate = parseDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_START_DATE)));
        Date endDate = parseDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_END_DATE)));
        Date executionTime = parseTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_EXECUTION_TIME)));

        String difficultyStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_DIFFICULTY));
        String importanceStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_IMPORTANCE));
        String statusStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_STATUS));
        String completionDateStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_COMPLETION_DATE));

        DifficultyType difficulty = DifficultyType.valueOf(difficultyStr);
        ImportanceType importance = ImportanceType.valueOf(importanceStr);

        TaskStatus status = TaskStatus.valueOf(statusStr != null ? statusStr : "AKTIVAN");
        Date completionDate = parseDate(completionDateStr);

        return new Task(
                id, name, description, category,
                frequency, interval, intervalUnit,
                startDate, endDate, executionTime,
                difficulty, importance,
                status, completionDate
        );
    }


    private Category getCategoryById(int categoryId) {
        return categoryRepository.getCategoryById(categoryId);
    }

    private String formatDate(Date date) {
        if (date == null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(date);
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String formatTime(Date date) {
        if (date == null) return null;
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(date);
    }

    private Date parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            return timeFormat.parse(timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int updateTasksColor(int categoryId, int newColor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_TASK_CATEGORY_COLOR, newColor);

        int updatedRows = db.update(DatabaseHelper.TABLE_TASKS, cv,
                DatabaseHelper.COLUMN_TASK_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(categoryId)});
        db.close();
        return updatedRows;
    }
}