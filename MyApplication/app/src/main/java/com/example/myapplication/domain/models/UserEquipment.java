package com.example.myapplication.domain.models;

public class UserEquipment {
    private int equipmentId;
    private boolean isActive;
    private int duration;
    private int remainingBattles;
    public UserEquipment(){}
    public UserEquipment(int equipmentId, boolean isActive, int duration) {
        this.equipmentId = equipmentId;
        this.isActive = isActive;
        this.duration = duration;
        this.remainingBattles = duration;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getDuration() {
        return duration;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getRemainingBattles() {
        return remainingBattles;
    }

    public void setRemainingBattles(int remainingBattles) {
        this.remainingBattles = remainingBattles;
    }

    public void decrementRemainingBattles() {
        if (remainingBattles > 0) {
            remainingBattles--;
        }
    }

    public boolean shouldBeRemoved() {
        return remainingBattles <= 0;
    }
}