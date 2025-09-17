package com.example.myapplication.data.database;

import android.util.Log;
import android.util.Pair;

import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.domain.models.Category;
import com.example.myapplication.domain.models.DifficultyType;
import com.example.myapplication.domain.models.ImportanceType;
import com.example.myapplication.domain.models.Task;
import com.example.myapplication.domain.models.TaskStatus;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TaskRepositoryFirebaseImpl implements TaskRepository {

    private static final String TAG = "TaskRepoFirebaseImpl";
    private static final String USERS_COLLECTION = "users";
    private static final String COLLECTION_TASKS = "tasks";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_COMPLETION_DATE = "completionDate";
    private static final String FIELD_XP_VALUE = "xpValue";
    private static final String FIELD_CATEGORY_ID = "category.id";

    private final FirebaseFirestore db;

    public TaskRepositoryFirebaseImpl() {
        this.db = FirebaseFirestore.getInstance();
    }

    private CollectionReference getTasksCollection(String userId) {
        return db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(COLLECTION_TASKS);
    }

    // --- CRUD OPERACIJE ---

    @Override
    public void insertTask(Task task, String userId, OnTaskAddedListener listener) {
        String documentId = getTasksCollection(userId).document().getId();
        task.setId(documentId);

        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("id", task.getId());
        taskMap.put("name", task.getName());
        taskMap.put("description", task.getDescription());
        taskMap.put("category", task.getCategory());
        taskMap.put("frequency", task.getFrequency());
        taskMap.put("interval", task.getInterval());
        taskMap.put("intervalUnit", task.getIntervalUnit());
        taskMap.put("startDate", task.getStartDate() != null ? new Timestamp(task.getStartDate()) : null);
        taskMap.put("endDate", task.getEndDate() != null ? new Timestamp(task.getEndDate()) : null);
        taskMap.put("executionTime", task.getExecutionTime() != null ? new Timestamp(task.getExecutionTime()) : null);
        taskMap.put("completionDate", task.getCompletionDate() != null ? new Timestamp(task.getCompletionDate()) : null);
        taskMap.put("difficulty", task.getDifficulty());
        taskMap.put("importance", task.getImportance());
        taskMap.put("xpValue", task.getXpValue());
        taskMap.put("userId", task.getUserId());
        taskMap.put("status", task.getStatus().name());

        getTasksCollection(userId)
                .document(documentId)
                .set(taskMap)
                .addOnSuccessListener(aVoid -> listener.onSuccess(documentId))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Greška pri insertTask", e);
                    listener.onFailure(e);
                });
    }

    @Override
    public void getTaskById(String taskId, String userId, OnTaskLoadedListener listener) {
        getTasksCollection(userId).document(taskId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Task task = documentSnapshot.toObject(Task.class);
                        if (task != null) {
                            task.setId(documentSnapshot.getId());
                            task.setCategory(mapCategory(documentSnapshot.get("category")));
                        }
                        listener.onSuccess(task);
                    } else {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Greška pri getTaskById", e);
                    listener.onFailure(e);
                });
    }

    @Override
    public void getAllTasks(String userId, OnTasksLoadedListener listener) {
        getTasksCollection(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Task> tasks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task currentTask = document.toObject(Task.class);
                            currentTask.setId(document.getId());
                            currentTask.setCategory(mapCategory(document.get("category")));
                            tasks.add(currentTask);
                        }
                        listener.onSuccess(tasks);
                    } else {
                        Log.e(TAG, "Greška pri getAllTasks", task.getException());
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Unknown error fetching tasks."));
                    }
                });
    }

    @Override
    public void updateTask(Task task, String userId, OnTaskUpdatedListener listener) {
        if (task.getId() == null) {
            listener.onFailure(new IllegalArgumentException("Task ID cannot be null for update."));
            return;
        }

        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("name", task.getName());
        taskMap.put("description", task.getDescription());
        taskMap.put("category", task.getCategory());
        taskMap.put("frequency", task.getFrequency());
        taskMap.put("interval", task.getInterval());
        taskMap.put("intervalUnit", task.getIntervalUnit());
        taskMap.put("startDate", task.getStartDate() != null ? new Timestamp(task.getStartDate()) : null);
        taskMap.put("endDate", task.getEndDate() != null ? new Timestamp(task.getEndDate()) : null);
        taskMap.put("executionTime", task.getExecutionTime() != null ? new Timestamp(task.getExecutionTime()) : null);
        taskMap.put("completionDate", task.getCompletionDate() != null ? new Timestamp(task.getCompletionDate()) : null);
        taskMap.put("difficulty", task.getDifficulty());
        taskMap.put("importance", task.getImportance());
        taskMap.put("xpValue", task.getXpValue());
        taskMap.put("userId", task.getUserId());
        taskMap.put("status", task.getStatus().name());

        getTasksCollection(userId).document(task.getId())
                .set(taskMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Greška pri updateTask", e);
                    listener.onFailure(e);
                });
    }

    @Override
    public void deleteTask(String taskId, String userId, OnTaskDeletedListener listener) {
        getTasksCollection(userId).document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Greška pri deleteTask", e);
                    listener.onFailure(e);
                });
    }

    // --- METODE ZA STATISTIKU ---

    @Override
    public void getTotalActiveDays(String userId, OnStatisticsLoadedListener<Integer> listener) {
        getTasksCollection(userId)
                .whereEqualTo(FIELD_STATUS, TaskStatus.URAĐEN.name())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> completionDates = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task t = document.toObject(Task.class);
                            if (t.getCompletionDate() != null) {
                                completionDates.add(formatDate(t.getCompletionDate()));
                            }
                        }
                        int uniqueDays = (int) completionDates.stream().distinct().count();
                        listener.onSuccess(uniqueDays);
                    } else {
                        Log.e(TAG, "Greška pri getTotalActiveDays", task.getException());
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Unknown error fetching tasks."));
                    }
                });
    }

    @Override
    public void getLongestConsecutiveDays(String userId, OnStatisticsLoadedListener<Integer> listener) {
        getTasksCollection(userId)
                .whereEqualTo(FIELD_STATUS, TaskStatus.URAĐEN.name())
                .orderBy(FIELD_COMPLETION_DATE, Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Date> completionDates = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task t = document.toObject(Task.class);
                            if (t.getCompletionDate() != null) {
                                completionDates.add(t.getCompletionDate());
                            }
                        }
                        listener.onSuccess(calculateLongestStreak(completionDates));
                    } else {
                        Log.e(TAG, "Greška pri getLongestConsecutiveDays", task.getException());
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Unknown error fetching tasks."));
                    }
                });
    }

    @Override
    public void getTaskCountByStatus(String userId, TaskStatus status, OnStatisticsLoadedListener<Integer> listener) {
        getTasksCollection(userId)
                .whereEqualTo(FIELD_STATUS, status.name())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        listener.onSuccess(task.getResult().size());
                    } else {
                        Log.e(TAG, "Greška pri getTaskCountByStatus", task.getException());
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Unknown error fetching tasks."));
                    }
                });
    }

    @Override
    public void getCompletedTasksCountByCategory(String userId, OnStatisticsLoadedListener<Map<String, Pair<Integer, Integer>>> listener) {
        getTasksCollection(userId)
                .whereEqualTo("status", "URAĐEN")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Pair<Integer, Integer>> categoryCountsAndColors = new HashMap<>();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            Task completedTask = doc.toObject(Task.class);
                            if (completedTask != null && completedTask.getCategory() != null) {
                                String categoryName = completedTask.getCategory().getName();
                                int categoryColor = completedTask.getCategory().getColor();

                                Pair<Integer, Integer> currentData = categoryCountsAndColors.getOrDefault(categoryName, new Pair<>(0, categoryColor));
                                int newCount = currentData.first + 1;
                                categoryCountsAndColors.put(categoryName, new Pair<>(newCount, categoryColor));
                            }
                        }

                        listener.onSuccess(categoryCountsAndColors);
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    @Override
    public void getAverageDifficultyXp(String userId, OnStatisticsLoadedListener<Double> listener) {
        getTasksCollection(userId)
                .whereEqualTo(FIELD_STATUS, TaskStatus.URAĐEN.name())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Double> xpValues = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task t = document.toObject(Task.class);
                            xpValues.add((double) t.getXpValue());
                        }
                        double average = xpValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                        listener.onSuccess(average);
                    } else {
                        Log.e(TAG, "Greška pri getAverageDifficultyXp", task.getException());
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Unknown error fetching tasks."));
                    }
                });
    }

    @Override
    public void getAverageDifficultyXpLast7Days(String userId, OnStatisticsLoadedListener<Map<String, Double>> listener) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        Timestamp sevenDaysAgo = new Timestamp(cal.getTime());

        getTasksCollection(userId)
                .whereEqualTo(FIELD_STATUS, TaskStatus.URAĐEN.name())
                .whereGreaterThanOrEqualTo(FIELD_COMPLETION_DATE, sevenDaysAgo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, List<Double>> dailyXp = new HashMap<>();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task t = document.toObject(Task.class);
                            String day = dateFormat.format(t.getCompletionDate());
                            dailyXp.computeIfAbsent(day, k -> new ArrayList<>()).add((double) t.getXpValue());
                        }

                        Map<String, Double> result = new LinkedHashMap<>();
                        for (int i = 6; i >= 0; i--) {
                            Calendar tempCal = Calendar.getInstance();
                            tempCal.add(Calendar.DATE, -i);
                            String dayKey = dateFormat.format(tempCal.getTime());
                            List<Double> xps = dailyXp.get(dayKey);
                            double avg = (xps != null) ? xps.stream().mapToDouble(Double::doubleValue).average().orElse(0.0) : 0.0;
                            result.put(dayKey, avg);
                        }
                        listener.onSuccess(result);
                    } else {
                        Log.e(TAG, "Greška pri getAverageDifficultyXpLast7Days", task.getException());
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Unknown error fetching tasks."));
                    }
                });
    }

    @Override
    public void getXpLast7Days(String userId, int userLevel, OnStatisticsLoadedListener<Map<String, Double>> listener) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        Timestamp sevenDaysAgo = new Timestamp(cal.getTime());

        getTasksCollection(userId)
                .whereEqualTo(FIELD_STATUS, TaskStatus.URAĐEN.name())
                .whereGreaterThanOrEqualTo(FIELD_COMPLETION_DATE, sevenDaysAgo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Double> xpPerDay = new LinkedHashMap<>();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                        for (int i = 6; i >= 0; i--) {
                            Calendar tempCal = Calendar.getInstance();
                            tempCal.add(Calendar.DATE, -i);
                            xpPerDay.put(dateFormat.format(tempCal.getTime()), 0.0);
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task t = document.toObject(Task.class);
                            String day = dateFormat.format(t.getCompletionDate());
                            double currentXp = xpPerDay.getOrDefault(day, 0.0);
                            xpPerDay.put(day, currentXp + t.getXpValue());
                        }
                        listener.onSuccess(xpPerDay);
                    } else {
                        Log.e(TAG, "Greška pri getXpLast7Days", task.getException());
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Unknown error fetching tasks."));
                    }
                });
    }

    // --- POMOĆNE METODE ---

    private Category mapCategory(Object categoryObject) {
        if (categoryObject instanceof Map) {
            Map<String, Object> categoryMap = (Map<String, Object>) categoryObject;
            Category category = new Category();
            category.setId((String) categoryMap.get("id"));
            category.setName((String) categoryMap.get("name"));
            Object color = categoryMap.get("color");
            if (color instanceof Number) {
                category.setColor(((Number) color).intValue());
            }
            return category;
        }
        return null;
    }

    private int calculateLongestStreak(List<Date> completionDates) {
        if (completionDates == null || completionDates.isEmpty()) {
            return 0;
        }

        List<String> uniqueDates = completionDates.stream()
                .map(this::formatDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (uniqueDates.isEmpty()) {
            return 0;
        }

        int currentStreak = 1;
        int maxStreak = 1;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        try {
            for (int i = 1; i < uniqueDates.size(); i++) {
                cal.setTime(Objects.requireNonNull(dateFormat.parse(uniqueDates.get(i - 1))));
                cal.add(Calendar.DATE, 1);
                String nextDay = dateFormat.format(cal.getTime());

                if (nextDay.equals(uniqueDates.get(i))) {
                    currentStreak++;
                } else {
                    currentStreak = 1;
                }
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Greška pri parsiranju datuma u calculateLongestStreak", e);
        }
        return maxStreak;
    }

    private String formatDate(Date date) {
        if (date == null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(date);
    }

    @Override
    public void updateTasksColor(String categoryId, int newColor, String userId, OnTaskUpdatedListener listener) {
        getTasksCollection(userId)
                .whereEqualTo(FIELD_CATEGORY_ID, categoryId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        WriteBatch batch = db.batch();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task currentTask = document.toObject(Task.class);
                            currentTask.setCategory(mapCategory(document.get("category")));
                            currentTask.getCategory().setColor(newColor);
                            batch.set(document.getReference(), currentTask);
                        }
                        batch.commit()
                                .addOnSuccessListener(aVoid -> listener.onSuccess())
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Greška pri updateTasksColor (batch commit)", e);
                                    listener.onFailure(e);
                                });
                    } else {
                        Log.e(TAG, "Greška pri updateTasksColor (query)", task.getException());
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Unknown error fetching tasks."));
                    }
                });
    }
}