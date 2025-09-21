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
import com.google.firebase.firestore.DocumentReference;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TaskRepositoryFirebaseImpl implements TaskRepository {

    private static final String TAG = "TaskRepoFirebaseImpl";
    private static final String USERS_COLLECTION = "users";
    private static final String COLLECTION_TASKS = "tasks";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_COMPLETION_DATE = "completionDate";
    private static final String FIELD_XP_VALUE = "xpValue";
    private static final String FIELD_CATEGORY_ID = "category.id";
    private static final String QUOTAS_COLLECTION = "xp_quotas";
    private static final String FIELD_LAST_UPDATED = "lastUpdated";
    private static final int TASK_EXPIRATION_GRACE_PERIOD_DAYS = 3;

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
        taskMap.put("creationDate", new Timestamp(task.getCreationDate()));

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

    /**
     * Ažurira status zadatka na URAĐEN i VRAĆA tačan broj osvojenih poena.
     */
    @Override
    public void updateTaskStatusToDone(String taskId, String userId, OnTaskCompletedListener listener) {
        final AtomicInteger awardedXp = new AtomicInteger(0);

        db.runTransaction(transaction -> {
            // 1. DOHVATANJE ZADATKA
            DocumentReference taskRef = getTasksCollection(userId).document(taskId);
            DocumentSnapshot taskSnapshot = transaction.get(taskRef);

            if (!taskSnapshot.exists()) {
                throw new RuntimeException("Zadatak nije pronađen.");
            }

            Task task = taskSnapshot.toObject(Task.class);
            if (task == null) {
                throw new RuntimeException("Greška pri mapiranju zadatka.");
            }
            if (task.getStatus() == TaskStatus.URAĐEN) {
                throw new RuntimeException("Zadatak je već završen.");
            }
            task.setCategory(mapCategory(taskSnapshot.get("category")));

            // 2. DEFINISANJE KVOTA I XP VREDNOSTI
            DifficultyType difficulty = task.getDifficultyType();
            ImportanceType importance = task.getImportanceType();

            int difficultyXp = getDifficultyXp(difficulty);
            String difficultyQuotaKey = getDifficultyQuotaKey(difficulty);
            String difficultyQuotaPeriod = getDifficultyQuotaPeriod(difficulty);
            int difficultyQuotaLimit = getDifficultyQuotaLimit(difficulty);

            int importanceXp = getImportanceXp(importance);
            String importanceQuotaKey = getImportanceQuotaKey(importance);
            String importanceQuotaPeriod = getImportanceQuotaPeriod(importance);
            int importanceQuotaLimit = getImportanceQuotaLimit(importance);

            // 3. PROVERA KVOTE ZA TEŽINU
            boolean awardDifficultyXp = false;
            long newDifficultyCount = 0;
            DocumentSnapshot difficultyQuotaSnapshot = null;
            if (difficultyQuotaPeriod != null) {
                DocumentReference difficultyQuotaRef = db.collection(USERS_COLLECTION).document(userId)
                        .collection(QUOTAS_COLLECTION).document(difficultyQuotaPeriod);
                difficultyQuotaSnapshot = transaction.get(difficultyQuotaRef);

                long currentCount = 0;
                if (difficultyQuotaSnapshot.exists()) {
                    Timestamp lastUpdated = difficultyQuotaSnapshot.getTimestamp(FIELD_LAST_UPDATED);
                    if (!isQuotaPeriodExpired(lastUpdated, difficultyQuotaPeriod)) {
                        currentCount = difficultyQuotaSnapshot.getLong(difficultyQuotaKey) != null ? difficultyQuotaSnapshot.getLong(difficultyQuotaKey) : 0;
                    }
                }

                if (currentCount < difficultyQuotaLimit) {
                    awardDifficultyXp = true;
                    newDifficultyCount = currentCount + 1;
                }
            } else {
                awardDifficultyXp = true; // Nema kvote za ovu težinu
            }

            // 4. PROVERA KVOTE ZA BITNOST
            boolean awardImportanceXp = false;
            long newImportanceCount = 0;
            if (importanceQuotaPeriod != null) {
                DocumentSnapshot importanceQuotaSnapshot;
                if (difficultyQuotaPeriod != null && difficultyQuotaPeriod.equals(importanceQuotaPeriod)) {
                    importanceQuotaSnapshot = difficultyQuotaSnapshot;
                } else {
                    DocumentReference importanceQuotaRef = db.collection(USERS_COLLECTION).document(userId)
                            .collection(QUOTAS_COLLECTION).document(importanceQuotaPeriod);
                    importanceQuotaSnapshot = transaction.get(importanceQuotaRef);
                }

                long currentCount = 0;
                if (importanceQuotaSnapshot != null && importanceQuotaSnapshot.exists()) {
                    Timestamp lastUpdated = importanceQuotaSnapshot.getTimestamp(FIELD_LAST_UPDATED);
                    if (!isQuotaPeriodExpired(lastUpdated, importanceQuotaPeriod)) {
                        currentCount = importanceQuotaSnapshot.getLong(importanceQuotaKey) != null ? importanceQuotaSnapshot.getLong(importanceQuotaKey) : 0;
                    }
                }

                if (currentCount < importanceQuotaLimit) {
                    awardImportanceXp = true;
                    newImportanceCount = currentCount + 1;
                }
            } else {
                awardImportanceXp = true; // Nema kvote za ovu bitnost
            }

            // 5. OBRAČUN FINALNOG XP-a I AŽURIRANJE ZADATKA
            int finalXp = (awardDifficultyXp ? difficultyXp : 0) + (awardImportanceXp ? importanceXp : 0);
            awardedXp.set(finalXp); // Postavi vrednost koja će biti vraćena

            Map<String, Object> taskUpdates = new HashMap<>();
            taskUpdates.put(FIELD_STATUS, TaskStatus.URAĐEN.name());
            taskUpdates.put(FIELD_COMPLETION_DATE, new Timestamp(new Date()));
            taskUpdates.put(FIELD_XP_VALUE, finalXp);
            transaction.update(taskRef, taskUpdates);

            // 6. AŽURIRANJE KVOTA
            Timestamp now = new Timestamp(new Date());
            if (awardDifficultyXp && difficultyQuotaPeriod != null) {
                DocumentReference difficultyQuotaRef = db.collection(USERS_COLLECTION).document(userId)
                        .collection(QUOTAS_COLLECTION).document(difficultyQuotaPeriod);
                Map<String, Object> quotaUpdates = new HashMap<>();
                quotaUpdates.put(difficultyQuotaKey, newDifficultyCount);
                quotaUpdates.put(FIELD_LAST_UPDATED, now);
                transaction.set(difficultyQuotaRef, quotaUpdates, SetOptions.merge());
            }

            if (awardImportanceXp && importanceQuotaPeriod != null) {
                DocumentReference importanceQuotaRef = db.collection(USERS_COLLECTION).document(userId)
                        .collection(QUOTAS_COLLECTION).document(importanceQuotaPeriod);
                Map<String, Object> quotaUpdates = new HashMap<>();
                quotaUpdates.put(importanceQuotaKey, newImportanceCount);
                quotaUpdates.put(FIELD_LAST_UPDATED, now);
                transaction.set(importanceQuotaRef, quotaUpdates, SetOptions.merge());
            }

            return null; // Uspeh transakcije
        }).addOnSuccessListener(aVoid -> {
            listener.onSuccess(awardedXp.get());
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Transakcija neuspešna: " + e.getMessage(), e);
            listener.onFailure(e);
        });
    }

    // --- NOVE POMOĆNE METODE ZA KVOTE I XP ---

    private int getDifficultyXp(DifficultyType type) {
        if (type == null) return 0;
        switch (type) {
            case VERY_EASY: return 1;
            case EASY: return 3;
            case HARD: return 7;
            case EXTREMELY_HARD: return 15;
            default: return 0;
        }
    }

    private int getImportanceXp(ImportanceType type) {
        if (type == null) return 0;
        switch (type) {
            case NORMAL: return 1;
            case IMPORTANT: return 3;
            case EXTREMELY_IMPORTANT: return 7;
            case SPECIAL: return 20;
            default: return 0;
        }
    }

    // --- METODE ZA TEŽINU ---
    private String getDifficultyQuotaKey(DifficultyType type) {
        return "DIFFICULTY_" + type.name();
    }

    private String getDifficultyQuotaPeriod(DifficultyType type) {
        if (type == null) return null;
        switch (type) {
            case VERY_EASY:
            case EASY:
            case HARD:
                return "daily";
            case EXTREMELY_HARD:
                return "weekly";
            default:
                return null; // Nema kvote
        }
    }

    private int getDifficultyQuotaLimit(DifficultyType type) {
        if (type == null) return Integer.MAX_VALUE;
        switch (type) {
            case VERY_EASY:
            case EASY:
                return 5;
            case HARD:
                return 2;
            case EXTREMELY_HARD:
                return 1;
            default:
                return Integer.MAX_VALUE; // Nema ograničenja
        }
    }

    // --- METODE ZA BITNOST ---
    private String getImportanceQuotaKey(ImportanceType type) {
        return "IMPORTANCE_" + type.name();
    }

    private String getImportanceQuotaPeriod(ImportanceType type) {
        if (type == null) return null;
        switch (type) {
            case NORMAL:
            case IMPORTANT:
            case EXTREMELY_IMPORTANT:
                return "daily";
            case SPECIAL:
                return "monthly";
            default:
                return null; // Nema kvote
        }
    }

    private int getImportanceQuotaLimit(ImportanceType type) {
        if (type == null) return Integer.MAX_VALUE;
        switch (type) {
            case NORMAL:
            case IMPORTANT:
                return 5;
            case EXTREMELY_IMPORTANT:
                return 2;
            case SPECIAL:
                return 1;
            default:
                return Integer.MAX_VALUE; // Nema ograničenja
        }
    }

    // --- UNIVERZALNA METODA ZA PROVERU PERIODA ---
    private boolean isQuotaPeriodExpired(Timestamp lastUpdated, String period) {
        if (lastUpdated == null) return true;

        Calendar now = Calendar.getInstance();
        Calendar last = Calendar.getInstance();
        last.setTime(lastUpdated.toDate());

        switch (period) {
            case "daily":
                return now.get(Calendar.DAY_OF_YEAR) != last.get(Calendar.DAY_OF_YEAR)
                        || now.get(Calendar.YEAR) != last.get(Calendar.YEAR);
            case "weekly":
                now.setFirstDayOfWeek(Calendar.MONDAY);
                last.setFirstDayOfWeek(Calendar.MONDAY);
                return now.get(Calendar.WEEK_OF_YEAR) != last.get(Calendar.WEEK_OF_YEAR)
                        || now.get(Calendar.YEAR) != last.get(Calendar.YEAR);
            case "monthly":
                return now.get(Calendar.MONTH) != last.get(Calendar.MONTH)
                        || now.get(Calendar.YEAR) != last.get(Calendar.YEAR);
            default:
                return true;
        }
    }


    public void checkAndDeactivateExpiredTasks(String userId, OnTaskUpdatedListener listener) {
        getTasksCollection(userId)
                .whereEqualTo("status", TaskStatus.AKTIVAN.name())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        WriteBatch batch = db.batch();
                        AtomicInteger tasksToUpdate = new AtomicInteger(0);
                        Calendar now = Calendar.getInstance();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task currentTask = document.toObject(Task.class);

                            if (currentTask.getEndDate() != null) {
                                Calendar expiryCalendar = Calendar.getInstance();
                                expiryCalendar.setTime(currentTask.getEndDate());
                                expiryCalendar.add(Calendar.DAY_OF_YEAR, TASK_EXPIRATION_GRACE_PERIOD_DAYS);

                                if (now.after(expiryCalendar)) {
                                    DocumentReference taskRef = document.getReference();
                                    batch.update(taskRef, "status", TaskStatus.NEURAĐEN.name());
                                    tasksToUpdate.incrementAndGet();
                                }
                            }
                        }

                        if (tasksToUpdate.get() > 0) {
                            batch.commit()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Successfully deactivated " + tasksToUpdate.get() + " expired tasks.");
                                        listener.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Greška pri masovnom ažuriranju neaktivnih zadataka.", e);
                                        listener.onFailure(e);
                                    });
                        } else {
                            Log.d(TAG, "Nema isteklih zadataka za deaktivaciju.");
                            listener.onSuccess();
                        }

                    } else {
                        Log.e(TAG, "Greška pri dohvatanju isteklih zadataka", task.getException());
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Unknown error fetching tasks."));
                    }
                });
    }

    /**
     * Proverava da li je zadatak preko kvote za BILO KOJU od svoje dve komponente (težina ili bitnost).
     * Vraća 'true' ako je bar jedna kvota prekoračena.
     */
    @Override
    public void isTaskOverQuota(Task task, String userId, OnQuotaCheckedListener listener) {
        DifficultyType difficulty = task.getDifficultyType();
        String difficultyQuotaKey = getDifficultyQuotaKey(difficulty);
        String difficultyQuotaPeriod = getDifficultyQuotaPeriod(difficulty);
        int difficultyQuotaLimit = getDifficultyQuotaLimit(difficulty);

        AtomicBoolean isOverQuota = new AtomicBoolean(false);

        // 1. Proveri kvotu za težinu
        if (difficultyQuotaPeriod == null) {
            checkImportanceQuota(task, userId, listener, false);
            return;
        }

        DocumentReference difficultyQuotaRef = db.collection(USERS_COLLECTION).document(userId)
                .collection(QUOTAS_COLLECTION).document(difficultyQuotaPeriod);

        difficultyQuotaRef.get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                DocumentSnapshot snapshot = task1.getResult();
                long currentCount = 0;
                if (snapshot != null && snapshot.exists()) {
                    Timestamp lastUpdated = snapshot.getTimestamp(FIELD_LAST_UPDATED);
                    if (!isQuotaPeriodExpired(lastUpdated, difficultyQuotaPeriod)) {
                        currentCount = snapshot.getLong(difficultyQuotaKey) != null ? snapshot.getLong(difficultyQuotaKey) : 0;
                    }
                }
                if (currentCount >= difficultyQuotaLimit) {
                    isOverQuota.set(true);
                }
                // 2. Bez obzira na rezultat, proveri i kvotu za bitnost
                checkImportanceQuota(task, userId, listener, isOverQuota.get());
            } else {
                listener.onFailure(task1.getException());
            }
        });
    }

    private void checkImportanceQuota(Task task, String userId, OnQuotaCheckedListener listener, boolean isAlreadyOverQuota) {
        // Ako je kvota za težinu već prekoračena, odmah vrati true.
        if (isAlreadyOverQuota) {
            listener.onResult(true);
            return;
        }

        ImportanceType importance = task.getImportanceType();
        String importanceQuotaKey = getImportanceQuotaKey(importance);
        String importanceQuotaPeriod = getImportanceQuotaPeriod(importance);
        int importanceQuotaLimit = getImportanceQuotaLimit(importance);

        if (importanceQuotaPeriod == null) {
            listener.onResult(false); // Ni težina ni bitnost nemaju kvotu
            return;
        }

        DocumentReference importanceQuotaRef = db.collection(USERS_COLLECTION).document(userId)
                .collection(QUOTAS_COLLECTION).document(importanceQuotaPeriod);

        importanceQuotaRef.get().addOnCompleteListener(task2 -> {
            if (task2.isSuccessful()) {
                DocumentSnapshot snapshot = task2.getResult();
                long currentCount = 0;
                if (snapshot != null && snapshot.exists()) {
                    Timestamp lastUpdated = snapshot.getTimestamp(FIELD_LAST_UPDATED);
                    if (!isQuotaPeriodExpired(lastUpdated, importanceQuotaPeriod)) {
                        currentCount = snapshot.getLong(importanceQuotaKey) != null ? snapshot.getLong(importanceQuotaKey) : 0;
                    }
                }
                // Finalni rezultat je true ako je BILO KOJA kvota prekoračena
                listener.onResult(currentCount >= importanceQuotaLimit);
            } else {
                listener.onFailure(task2.getException());
            }
        });
    }


    @Override
    public void getTasksCreatedAfter(String userId, Date afterDate, OnTasksLoadedListener listener) {
        if (afterDate == null) {
            getAllTasks(userId, listener);
            return;
        }

        getTasksCollection(userId)
                .whereGreaterThanOrEqualTo("creationDate", new Timestamp(afterDate))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Task> tasks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task currentTask = document.toObject(Task.class);
                            currentTask.setId(document.getId());
                            tasks.add(currentTask);
                        }
                        listener.onSuccess(tasks);
                    } else {
                        Log.e("TaskRepo", "Greška pri getTasksCreatedAfter", task.getException());
                        listener.onFailure(task.getException());
                    }
                });
    }
}