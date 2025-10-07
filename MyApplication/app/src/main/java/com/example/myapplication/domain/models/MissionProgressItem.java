package com.example.myapplication.domain.models;

public class MissionProgressItem {

    private String username;
    private int damageDealt;

    public MissionProgressItem(String username, int damageDealt) {
        this.username = username;
        this.damageDealt = damageDealt;
    }

    public String getUsername() {
        return username;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

}