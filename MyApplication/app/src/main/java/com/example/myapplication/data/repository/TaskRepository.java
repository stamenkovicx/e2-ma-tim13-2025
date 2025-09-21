package com.example.myapplication.data.repository;

import android.util.Pair;

import com.example.myapplication.domain.models.Task;
import com.example.myapplication.domain.models.TaskStatus;

import java.util.Date;
import java.util.List;
import java.util.Map;


public interface TaskRepository {

    // Callback interfejsi za asinhroni rad
    interface OnTaskAddedListener {
        void onSuccess(String taskId);

        void onFailure(Exception e);
    }
    interface OnTaskCompletedListener {
        void onSuccess(int awardedXp); // Vraća tačan broj osvojenih poena
        void onFailure(Exception e);
    }

    interface OnTaskLoadedListener {
        void onSuccess(Task task);

        void onFailure(Exception e);
    }

    interface OnTasksLoadedListener {
        void onSuccess(List<Task> tasks);

        void onFailure(Exception e);
    }

    interface OnTaskUpdatedListener {
        void onSuccess();

        void onFailure(Exception e);
    }

    interface OnTaskDeletedListener {
        void onSuccess();

        void onFailure(Exception e);
    }

    // Callback za statističke metode
    interface OnStatisticsLoadedListener<T> {
        void onSuccess(T result);

        void onFailure(Exception e);
    }

    // Metode za CRUD operacije sa Firebase-om
    void insertTask(Task task, String userId, OnTaskAddedListener listener);

    void getTaskById(String taskId, String userId, OnTaskLoadedListener listener);

    void getAllTasks(String userId, OnTasksLoadedListener listener);

    void updateTask(Task task, String userId, OnTaskUpdatedListener listener);

    void deleteTask(String taskId, String userId, OnTaskDeletedListener listener);

    // Metode za statistiku
    void getTotalActiveDays(String userId, OnStatisticsLoadedListener<Integer> listener);

    void getLongestConsecutiveDays(String userId, OnStatisticsLoadedListener<Integer> listener);

    void getTaskCountByStatus(String userId, TaskStatus status, OnStatisticsLoadedListener<Integer> listener);

    void getCompletedTasksCountByCategory(String userId, OnStatisticsLoadedListener<Map<String, Pair<Integer, Integer>>> listener);

    void getAverageDifficultyXp(String userId, OnStatisticsLoadedListener<Double> listener);

    void getAverageDifficultyXpLast7Days(String userId, OnStatisticsLoadedListener<Map<String, Double>> listener);

    void getXpLast7Days(String userId, int userLevel, OnStatisticsLoadedListener<Map<String, Double>> listener);

    void updateTasksColor(String categoryId, int newColor, String userId, OnTaskUpdatedListener listener);

    void updateTaskStatusToDone(String taskId, String userId, OnTaskCompletedListener listener);

    void checkAndDeactivateExpiredTasks(String userId, OnTaskUpdatedListener listener);

    interface OnQuotaCheckedListener {
        void onResult(boolean overQuota);

        void onFailure(Exception e);
    }

    void isTaskOverQuota(Task task, String userId, OnQuotaCheckedListener listener);

    void getTasksCreatedAfter(String userId, Date afterDate, OnTasksLoadedListener listener);

}