package com.example.myapplication.domain.models;

public class Equipment {
    private String name;
    private String description;
    private String iconResourceId;
    private EquipmentType type;
    private double bonusValue;

    public Equipment(String name, String description, String iconResourceId, EquipmentType type, double bonusValue) {
        this.name = name;
        this.description = description;
        this.iconResourceId = iconResourceId;
        this.type = type;
        this.bonusValue = bonusValue;
    }

    // Getters
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
}