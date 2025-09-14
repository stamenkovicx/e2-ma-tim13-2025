package com.example.myapplication.data.repository;

import com.example.myapplication.domain.models.Task;

import java.util.List;


public interface TaskRepository {
    long insertTask(Task task);
    Task getTaskById(long taskId);
    List<Task> getAllTasks();
    int updateTask(Task task);
    int deleteTask(long taskId);

    int updateTasksColor(int categoryId, int newColor);

}