package com.example.myapplication.data.repository;

import com.example.myapplication.domain.models.Boss;

public interface BossRepository {
    void getBoss(String bossId, OnBossLoadedListener listener);
    void saveBoss(Boss boss, OnBossSavedListener listener);

    interface OnBossLoadedListener {
        void onSuccess(Boss boss);
        void onFailure(Exception e);
    }

    interface OnBossSavedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}