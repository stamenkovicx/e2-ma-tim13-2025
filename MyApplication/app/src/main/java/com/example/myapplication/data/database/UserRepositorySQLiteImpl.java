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

        int baseXpForDifficulty = task.getDifficulty().getXpValue();
        int baseXpForImportance = task.getImportance().getXpValue();

        int dynamicXpFromDifficulty = LevelingSystemHelper.getXpForDifficulty(baseXpForDifficulty, user.getLevel());
        int dynamicXpFromImportance = LevelingSystemHelper.getXpForImportance(baseXpForImportance, user.getLevel());

        int totalXpGained = dynamicXpFromDifficulty + dynamicXpFromImportance;

        user.setXp(user.getXp() + totalXpGained);

        while (true) {
            int requiredXp = LevelingSystemHelper.getRequiredXpForNextLevel(user.getLevel());

            if (user.getXp() < requiredXp) {
                break;
            }

            int newLevel = user.getLevel() + 1;
            user.setLevel(newLevel);

            int remainingXp = user.getXp() - requiredXp;
            user.setXp(remainingXp);

            String newTitle = LevelingSystemHelper.getTitleForLevel(newLevel);
            user.setTitle(newTitle);

            int ppGained = LevelingSystemHelper.getPowerPointsRewardForLevel(newLevel);
            user.setPowerPoints(user.getBasePowerPoints() + ppGained);
        }

        dbHelper.updateUser(user);

        task.setStatus(TaskStatus.URAĐEN);
        CategoryRepositorySQLiteImpl categoryRepo = new CategoryRepositorySQLiteImpl(dbHelper.getContext());

        TaskRepositorySQLiteImpl taskRepo = new TaskRepositorySQLiteImpl(dbHelper.getContext(), categoryRepo);
        taskRepo.updateTask(task);
    }

    // obrisati ovo skroz ??
}
