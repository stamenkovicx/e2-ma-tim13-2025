package com.example.myapplication.domain.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String email;
    private String password;
    private String avatar;
    private int xp;
    private int level;
    private String title;
    private int powerPoints;
    private int coins;
    private List<String> badges;
    private List<UserEquipment> userEquipmentList;

    public User(String username, String email, String password, String avatar) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.xp = 0;
        this.level = 1;
        this.title = "Beginner";
        this.powerPoints = 100;
        this.coins = 0;
        this.badges = new ArrayList<>();
        this.userEquipmentList = new ArrayList<>();
    }

    public User(String username, String email, String password, String avatar, int level, String title, int powerPoints, int xp, int coins) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.level = level;
        this.title = title;
        this.powerPoints = powerPoints;
        this.xp = xp;
        this.coins = coins;
        this.badges = new ArrayList<>();
        this.userEquipmentList = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPowerPoints() {
        return powerPoints;
    }

    public void setPowerPoints(int powerPoints) {
        this.powerPoints = powerPoints;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    public List<UserEquipment> getUserEquipmentList() {
        return userEquipmentList;
    }

    public void setUserEquipmentList(List<UserEquipment> userEquipmentList) {
        this.userEquipmentList = userEquipmentList;
    }

    public void addEquipment(UserEquipment equipment) {
        if (this.userEquipmentList == null) {
            this.userEquipmentList = new ArrayList<>();
        }
        this.userEquipmentList.add(equipment);
    }
}