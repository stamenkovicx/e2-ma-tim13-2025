package com.example.myapplication.domain.models;

public class Equipment {
    private int id;
    private String name;
    private String description;
    private String iconResourceId;
    private EquipmentType type;
    private double bonusValue;
    private boolean isActive;
    private int duration;

    public Equipment(int id, String name, String description, String iconResourceId, EquipmentType type, double bonusValue, int duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconResourceId = iconResourceId;
        this.type = type;
        this.bonusValue = bonusValue;
        this.isActive = false;
        this.duration = duration;
    }

    // konstruktor za jednokratne napitke koji nemaju trajanje
    public Equipment(int id, String name, String description, String iconResourceId, EquipmentType type, double bonusValue) {
        this(id, name, description, iconResourceId, type, bonusValue, 0);
    }

    // Geteri
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconResourceId() {
        return iconResourceId;
    }

    public EquipmentType getType() {
        return type;
    }

    public double getBonusValue() {
        return bonusValue;
    }
    public boolean isActive() {
        return isActive;
    }
    public int getDuration() {
        return duration;
    }

    // Seteri
    public void setActive(boolean active) { this.isActive = active; }
    public void setDuration(int duration) { this.duration = duration; }
}