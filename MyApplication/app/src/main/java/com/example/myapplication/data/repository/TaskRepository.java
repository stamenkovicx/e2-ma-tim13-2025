package com.example.myapplication.data.repository;

import android.app.Application;

import com.example.myapplication.data.local.dao.TaskDao;
// ISPRAVLJENA LINIJA ISPOD
import com.example.myapplication.data.local.datebase.AppDatabase;
import com.example.myapplication.data.model.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private TaskDao taskDao;
    private ExecutorService executorService;

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.taskDao = db.taskDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insertTask(Task task) {
        executorService.execute(() -> {
            taskDao.insertTask(task);
        });
    }
}