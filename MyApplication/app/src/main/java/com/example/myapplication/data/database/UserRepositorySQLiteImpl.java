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

    // obrisati ovo skroz ??
}
