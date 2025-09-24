package com.example.myapplication.domain.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Alliance {
    private String allianceId;
    private String name;
    private String leaderId;
    private List<String> memberIds;
    private List<String> pendingInvitations;

    private boolean specialMissionActive = false;
    private Date specialMissionStartTime;
    private int specialMissionBossHp;
    private int specialMissionBossMaxHp;

    public Alliance() {
        // Obavezni prazan konstruktor za Firebase
    }

    public Alliance(String allianceId, String name, String leaderId, List<String> memberIds, List<String> pendingInvitations) {
        this.allianceId = allianceId;
        this.name = name;
        this.leaderId = leaderId;
        this.memberIds = memberIds != null ? memberIds : new ArrayList<>();
        this.pendingInvitations = pendingInvitations != null ? pendingInvitations : new ArrayList<>();
    }

    // Geteri
    public String getAllianceId() {
        return allianceId;
    }

    public String getName() {
        return name;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public List<String> getPendingInvitations() {
        return pendingInvitations;
    }

    // Seteri
    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public void setPendingInvitations(List<String> pendingInvitations) {
        this.pendingInvitations = pendingInvitations;
    }
    public boolean isSpecialMissionActive() {
        return specialMissionActive;
    }

    public void setSpecialMissionActive(boolean specialMissionActive) {
        this.specialMissionActive = specialMissionActive;
    }

    public Date getSpecialMissionStartTime() {
        return specialMissionStartTime;
    }

    public void setSpecialMissionStartTime(Date specialMissionStartTime) {
        this.specialMissionStartTime = specialMissionStartTime;
    }

    public int getSpecialMissionBossHp() {
        return specialMissionBossHp;
    }

    public void setSpecialMissionBossHp(int specialMissionBossHp) {
        this.specialMissionBossHp = specialMissionBossHp;
    }

    public int getSpecialMissionBossMaxHp() {
        return specialMissionBossMaxHp;
    }

    public void setSpecialMissionBossMaxHp(int specialMissionBossMaxHp) {
        this.specialMissionBossMaxHp = specialMissionBossMaxHp;
    }
}