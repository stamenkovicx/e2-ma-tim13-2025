package com.example.myapplication.domain.models;

import java.util.ArrayList;
import java.util.List;

public class SpecialMissionProgress {

    private String userId;
    private String allianceId;
    private int damageDealt = 0;

    // Brojaci za svaku akciju
    private int shopPurchases = 0;
    private int regularBossHits = 0;
    private int simpleTasksSolved = 0;
    private int otherTasksSolved = 0;
    private boolean noUnsolvedTasksBonusApplied = false;
    private boolean rewardsClaimed = false;

    private List<String> daysMessaged;

    public SpecialMissionProgress() {
        // Prazan konstruktor za Firebase
        this.daysMessaged = new ArrayList<>();
    }

    public SpecialMissionProgress(String userId, String allianceId) {
        this.userId = userId;
        this.allianceId = allianceId;
        this.daysMessaged = new ArrayList<>();
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(int damageDealt) {
        this.damageDealt = damageDealt;
    }

    public int getShopPurchases() {
        return shopPurchases;
    }

    public void setShopPurchases(int shopPurchases) {
        this.shopPurchases = shopPurchases;
    }

    public int getRegularBossHits() {
        return regularBossHits;
    }

    public void setRegularBossHits(int regularBossHits) {
        this.regularBossHits = regularBossHits;
    }

    public int getSimpleTasksSolved() {
        return simpleTasksSolved;
    }

    public void setSimpleTasksSolved(int simpleTasksSolved) {
        this.simpleTasksSolved = simpleTasksSolved;
    }

    public int getOtherTasksSolved() {
        return otherTasksSolved;
    }

    public void setOtherTasksSolved(int otherTasksSolved) {
        this.otherTasksSolved = otherTasksSolved;
    }

    public boolean isNoUnsolvedTasksBonusApplied() {
        return noUnsolvedTasksBonusApplied;
    }

    public void setNoUnsolvedTasksBonusApplied(boolean noUnsolvedTasksBonusApplied) {
        this.noUnsolvedTasksBonusApplied = noUnsolvedTasksBonusApplied;
    }

    public List<String> getDaysMessaged() {
        return daysMessaged;
    }

    public void setDaysMessaged(List<String> daysMessaged) {
        this.daysMessaged = daysMessaged;
    }
    public boolean areRewardsClaimed() {
        return rewardsClaimed;
    }

    public void setRewardsClaimed(boolean rewardsClaimed) {
        this.rewardsClaimed = rewardsClaimed;
    }
}