package com.example.myapplication.domain.models;

public class Badge {
    private String name;
    private String description;
    private String iconResourceId;

    public Badge(String name, String description, String iconResourceId) {
        this.name = name;
        this.description = description;
        this.iconResourceId = iconResourceId;
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
}