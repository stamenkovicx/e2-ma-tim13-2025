package com.example.myapplication.data.database;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.domain.models.Task;
import com.example.myapplication.domain.models.TaskStatus;
import com.example.myapplication.domain.models.User;

public class UserRepositorySQLiteImpl {

    private DatabaseHelper dbHelper;

    public UserRepositorySQLiteImpl(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public void completeTaskAndAddXp(String userEmail, Task task) {
        if (task == null) return;

        if (task.getStatus() == TaskStatus.PAUZIRAN || task.getStatus() == TaskStatus.OTKAZAN) {
            return;
        }

        User user = dbHelper.getUser(userEmail);
        if (user == null) return;

        int newXp = user.getXp() + task.getXpValue();
        user.setXp(newXp);
        dbHelper.updateUser(user);

        task.setStatus(TaskStatus.URAĐEN);
        TaskRepositorySQLiteImpl taskRepo = new TaskRepositorySQLiteImpl(dbHelper.getContext());
        taskRepo.updateTask(task);
    }

}
