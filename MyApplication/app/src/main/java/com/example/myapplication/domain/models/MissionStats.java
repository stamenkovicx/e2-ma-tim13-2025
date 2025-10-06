package com.example.myapplication.domain.models;

public class MissionStats {
    private int successfulMissions;
    private int failedMissions;
    private int activeMissions;

    public MissionStats(int successfulMissions, int failedMissions, int activeMissions) {
        this.successfulMissions = successfulMissions;
        this.failedMissions = failedMissions;
        this.activeMissions = activeMissions;
    }

    // Getters
    public int getSuccessfulMissions() { return successfulMissions; }
    public int getFailedMissions() { return failedMissions; }
    public int getActiveMissions() { return activeMissions; }
}