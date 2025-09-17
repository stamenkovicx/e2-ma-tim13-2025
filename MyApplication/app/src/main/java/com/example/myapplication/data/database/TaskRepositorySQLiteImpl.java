/*** package com.example.myapplication.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Calendar;
import java.util.Set;


 // Konkretna implementacija TaskRepository interfejsa
 // koja koristi SQLite bazu podataka.

public class TaskRepositorySQLiteImpl implements TaskRepository {

    private DatabaseHelper dbHelper;
    private CategoryRepository categoryRepository;


    // Bolji pristup (Dependency Injection)
    public TaskRepositorySQLiteImpl(Context context, CategoryRepository categoryRepository) {
        this.dbHelper = new DatabaseHelper(context);
        this.categoryRepository = categoryRepository;
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

            cv.put(DatabaseHelper.COLUMN_TASK_CATEGORY_COLOR,
                    task.getCategory() != null ? task.getCategory().getColor() : 0);
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
            cv.put(DatabaseHelper.COLUMN_TASK_USER_EMAIL, task.getUserEmail());

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

    // Ažurirajte implementaciju u TaskRepositorySQLiteImpl.java
    @Override
    public List<Task> getAllTasks(String userEmail) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Task> tasks = new ArrayList<>();

        Cursor cursor = db.query(DatabaseHelper.TABLE_TASKS,
                null,
                DatabaseHelper.COLUMN_TASK_USER_EMAIL + " = ?",
                new String[]{userEmail},
                null, null, null);

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
        // Logika za postavljanje completion_date
        cv.put(DatabaseHelper.COLUMN_TASK_STATUS, task.getStatus().name());

        if (task.getStatus() == TaskStatus.URAĐEN) {
            // AKO JE STATUS URADJEN, ZAPISUJEMO DANASNJI DATUM
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());
            cv.put(DatabaseHelper.COLUMN_TASK_COMPLETION_DATE, currentDate);
        } else {
            // U svim ostalim slucajevima, completion_date je NULL
            cv.putNull(DatabaseHelper.COLUMN_TASK_COMPLETION_DATE);
        }

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

     // racuna ukupan broj dana u kojima je korisnik zavrsio barem jedan zadatak.
    public int getTotalActiveDays() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int activeDays = 0;

        // Upit za dohvaćanje jedinstvenih datuma završetka zadataka
        String query = "SELECT COUNT(DISTINCT " + DatabaseHelper.COLUMN_TASK_COMPLETION_DATE + ") "
                + "FROM " + DatabaseHelper.TABLE_TASKS
                + " WHERE " + DatabaseHelper.COLUMN_TASK_STATUS + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{TaskStatus.URAĐEN.name()});

        if (cursor.moveToFirst()) {
            activeDays = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return activeDays;
    }


     //Racuna najduzi niz uzastopnih dana sa uspjesno zavrsenim zadacima
     //Niz se ne prekida ako u danu nema zadataka

    public int getLongestConsecutiveDays() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> completionDates = new ArrayList<>();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_TASKS,
                new String[]{DatabaseHelper.COLUMN_TASK_COMPLETION_DATE},
                DatabaseHelper.COLUMN_TASK_STATUS + " = ?",
                new String[]{TaskStatus.URAĐEN.name()},
                null,
                null,
                DatabaseHelper.COLUMN_TASK_COMPLETION_DATE + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_COMPLETION_DATE));
                completionDates.add(date);
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (completionDates.isEmpty()) {
            return 0;
        }

        return calculateLongestStreak(completionDates);
    }

    // Pomocna metoda za izracunavanje niza
    private int calculateLongestStreak(List<String> completionDates) {
        if (completionDates == null || completionDates.isEmpty()) {
            return 0;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Set<Date> uniqueDatesSet = new HashSet<>();

        // Parsiranje datuma i uklanjanje duplikata
        for (String dateStr : completionDates) {
            if (dateStr != null && !dateStr.isEmpty()) { // Provjera za null i prazan string
                try {
                    Date date = dateFormat.parse(dateStr);
                    uniqueDatesSet.add(date);
                } catch (ParseException e) {
                    Log.e("TaskRepo", "Error parsing date: " + dateStr, e);
                }
            }
        }

        if (uniqueDatesSet.isEmpty()) {
            return 0;
        }

        // Konvertovanje seta u listu i sortiranje
        List<Date> sortedDates = new ArrayList<>(uniqueDatesSet);
        Collections.sort(sortedDates);

        int currentStreak = 1;
        int maxStreak = 1;

        for (int i = 1; i < sortedDates.size(); i++) {
            long diff = sortedDates.get(i).getTime() - sortedDates.get(i - 1).getTime();
            long daysDiff = diff / (1000 * 60 * 60 * 24);

            if (daysDiff == 1) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }

            if (currentStreak > maxStreak) {
                maxStreak = currentStreak;
            }
        }
        return maxStreak;
    }

    // Broj ukupno kreiranih, urađenih, neurađenih i otkazanih zadataka
    // metoda koja vraca ukupan broj zadataka za svaki status
    public int getTaskCountByStatus(TaskStatus status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_TASKS,
                new String[]{"COUNT(*)"},
                DatabaseHelper.COLUMN_TASK_STATUS + " = ?",
                new String[]{status.name()},
                null, null, null
        );
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // Broj zavrsenih zadataka po kategoriji
    public Map<String, Integer> getCompletedTasksCountByCategory() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Map<String, Integer> categoryCounts = new HashMap<>();

        String query = "SELECT " + DatabaseHelper.COLUMN_TASK_CATEGORY_ID + ", COUNT(*) "
                + "FROM " + DatabaseHelper.TABLE_TASKS
                + " WHERE " + DatabaseHelper.COLUMN_TASK_STATUS + " = ?"
                + " GROUP BY " + DatabaseHelper.COLUMN_TASK_CATEGORY_ID;

        Cursor cursor = db.rawQuery(query, new String[]{TaskStatus.URAĐEN.name()});

        if (cursor.moveToFirst()) {
            do {
                long categoryId = cursor.getLong(0);
                int count = cursor.getInt(1);
                Category category = categoryRepository.getCategoryById((int) categoryId);

                if (category != null) {
                    String categoryName = category.getName();
                    categoryCounts.put(categoryName, count);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categoryCounts;
    }

    // Prosjecna ocjena tezine zavrsenih zadataka
    public double getAverageDifficultyXp() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double averageXp = 0.0;

        String query = "SELECT AVG(" + DatabaseHelper.COLUMN_TASK_XP_VALUE + ") FROM " + DatabaseHelper.TABLE_TASKS +
                " WHERE " + DatabaseHelper.COLUMN_TASK_STATUS + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{TaskStatus.URAĐEN.name()});

        if (cursor.moveToFirst()) {
            averageXp = cursor.getDouble(0);
        }
        cursor.close();
        return averageXp;
    }



    public Map<String, Double> getAverageDifficultyXpLast7Days() {
        Map<String, Double> result = new LinkedHashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // SQL: grupisi po datumu zavrsetka i izracunaj prosjek XP težine
        String query = "SELECT strftime('%Y-%m-%d', completion_date/1000, 'unixepoch') AS day, " +
                "AVG(CASE difficulty " +
                "       WHEN 'VERY_EASY' THEN 1 " +
                "       WHEN 'EASY' THEN 3 " +
                "       WHEN 'HARD' THEN 7 " +
                "       WHEN 'EXTREMELY_HARD' THEN 20 " +
                "   END) AS avg_difficulty_xp " +
                "FROM tasks " +
                "WHERE status = 'URAĐEN' " +
                "AND completion_date >= date('now', '-7 days') " +
                "GROUP BY day " +
                "ORDER BY day ASC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String day = cursor.getString(cursor.getColumnIndexOrThrow("day"));
                double avgXp = cursor.getDouble(cursor.getColumnIndexOrThrow("avg_difficulty_xp"));
                result.put(day, avgXp);
            }
            cursor.close();
        }

        db.close();
        return result;
    }

    public Map<String, Double> getXpLast7Days(int userLevel) {
        Map<String, Double> xpPerDay = new LinkedHashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Inicijalizacija mape sa 0 XP za posljednjih 7 dana
        for (int i = 6; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -i);
            xpPerDay.put(dateFormat.format(cal.getTime()), 0.0);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Upit za dohvatanje zavrsenih zadataka u posljednjih 7 dana
        String query = "SELECT " + DatabaseHelper.COLUMN_TASK_COMPLETION_DATE + ", " +
                DatabaseHelper.COLUMN_TASK_IMPORTANCE + ", " +
                DatabaseHelper.COLUMN_TASK_DIFFICULTY + " FROM " + DatabaseHelper.TABLE_TASKS +
                " WHERE " + DatabaseHelper.COLUMN_TASK_STATUS + " = ? " +
                " AND " + DatabaseHelper.COLUMN_TASK_COMPLETION_DATE + " BETWEEN date('now','-6 days') AND date('now') " +
                " ORDER BY " + DatabaseHelper.COLUMN_TASK_COMPLETION_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{TaskStatus.URAĐEN.name()});

        if (cursor != null) {
            // Iteriranje kroz svaki zadatak i dodavanje XP-a u mapu
            while (cursor.moveToNext()) {
                String day = cursor.getString(0);
                String importanceStr = cursor.getString(1);
                String difficultyStr = cursor.getString(2);

                // Konvertovanje stringova u enum tipove
                ImportanceType importance = ImportanceType.valueOf(importanceStr);
                DifficultyType difficulty = DifficultyType.valueOf(difficultyStr);

                // Racunanje konacne XP vrijednosti za jedan zadatak
                int finalXp = LevelingSystemHelper.calculateFinalXp(importance, difficulty, userLevel);

                // Dodavanje izračunate vrijednosti u mapu
                double currentXp = xpPerDay.getOrDefault(day, 0.0);
                xpPerDay.put(day, currentXp + finalXp);
            }
            cursor.close();
        }
        db.close();
        return xpPerDay;
    }

    // TODO: metoda koja vraca broj zapocetih i zavrsenih specijalnih misija:
} ***/