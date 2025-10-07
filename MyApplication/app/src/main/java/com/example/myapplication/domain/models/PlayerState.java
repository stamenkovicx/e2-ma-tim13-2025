package com.example.myapplication.domain.models;

import com.google.firebase.firestore.DocumentId;

public class PlayerState {

    @DocumentId
    private String id;
    private int powerPoints;
    private int successChance;
    private int lastReward; // Dodato polje za poslednju nagradu

    public PlayerState() {}

    public PlayerState(String id, int powerPoints, int successChance, int lastReward) {
        this.id = id;
        this.powerPoints = powerPoints;
        this.successChance = successChance;
        this.lastReward = lastReward;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getPowerPoints() { return powerPoints; }
    public void setPowerPoints(int powerPoints) { this.powerPoints = powerPoints; }
    public int getSuccessChance() { return successChance; }
    public void setSuccessChance(int successChance) { this.successChance = successChance; }
    public int getLastReward() { return lastReward; }
    public void setLastReward(int lastReward) { this.lastReward = lastReward; }
}