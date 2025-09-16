package com.example.myapplication.domain.models;

public class UserEquipment {
    private int equipmentId;
    private boolean isActive;
    private int duration;

    public UserEquipment(){}
    public UserEquipment(int equipmentId, boolean isActive, int duration) {
        this.equipmentId = equipmentId;
        this.isActive = isActive;
        this.duration = duration;
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
}