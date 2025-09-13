package com.example.myapplication.domain.models;

public enum DifficultyType {
    VERY_EASY("Veoma lak", 1),
    EASY("Lak", 3),
    HARD("Tezak", 7),
    EXTREMELY_HARD("Ekstremno tezak", 20);

    private final String serbianName;
    private final int xpValue;

    DifficultyType(String serbianName, int xpValue) {
        this.serbianName = serbianName;
        this.xpValue = xpValue;
    }

    public String getSerbianName() {
        return serbianName;
    }

    public int getXpValue() {
        return xpValue;
    }
}