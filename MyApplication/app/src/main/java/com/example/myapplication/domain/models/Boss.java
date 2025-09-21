package com.example.myapplication.domain.models;

import com.google.firebase.firestore.DocumentId;

public class Boss {

    @DocumentId
    private String id;
    private int level;
    private int hp;
    private int maxHp;
    private boolean isDefeated;
    private int attemptsLeft;
    private int baseReward;
    private int userLevelOnEncounter;

    public Boss() {}

    public Boss(int level, int previousHp, int userLevel) {
        this.level = level;
        this.userLevelOnEncounter = userLevel; // nivo korisnika
        this.attemptsLeft = 5;
        this.baseReward = 200;
        this.isDefeated = false;

        if (level == 1) {
            this.hp = 200;
        } else {
            this.hp = previousHp * 2 + previousHp / 2;
        }
        this.maxHp = this.hp;
    }

    // Getteri i setteri za sva polja
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getAttemptsLeft() { return attemptsLeft; }
    public void setAttemptsLeft(int attemptsLeft) { this.attemptsLeft = attemptsLeft; }
    public int getBaseReward() { return baseReward; }
    public void setBaseReward(int baseReward) { this.baseReward = baseReward; }
    public boolean getIsDefeated() { return isDefeated; }
    public void setIsDefeated(boolean isDefeated) { this.isDefeated = isDefeated; }
    public int getUserLevelOnEncounter() { return userLevelOnEncounter; }
    public void setUserLevelOnEncounter(int userLevelOnEncounter) { this.userLevelOnEncounter = userLevelOnEncounter; }


    public void takeDamage(int damage) {
        hp -= damage;
        if (hp < 0) {
            hp = 0;
        }
    }
}