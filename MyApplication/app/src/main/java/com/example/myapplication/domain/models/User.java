package com.example.myapplication.domain.models;

import com.example.myapplication.data.database.LevelingSystemHelper;
import com.example.myapplication.data.repository.ItemRepository;

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
        this.level = 0;
        this.title = "Beginner";
        this.powerPoints = 0;
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

     // Vraca osnovni Power Points bez bonusa od opreme.
    public int getBasePowerPoints() {
        return this.powerPoints;
    }

     // Izracunava ukupne Power Points sabiranjem osnovnih PP-a i bonusa od aktivne opreme.
    public int getTotalPowerPoints() {
        int totalPower = this.powerPoints;
        for (UserEquipment item : this.userEquipmentList) {
            if (item.isActive()) {
                Equipment equipment = ItemRepository.getEquipmentById(item.getEquipmentId());
                if (equipment != null) {
                    totalPower += (int) equipment.getBonusValue();
                }
            }
        }
        return totalPower;
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

    // METODE ZA RUKOVANJE XP-om I AŽURIRANJE STATISTIKE

     // Dodaje XP korisniku i provjerava da li je doslo do promjene nivoa.
    public void addXp(int amount) {
        // Dodavanje XP-a
        this.xp += amount;

        // Provjera da li je korisnik presao na sledeci nivo
        int requiredXp = LevelingSystemHelper.getRequiredXpForNextLevel(this.level);

        while (this.xp >= requiredXp) {
            // Povecanje nivoa
            this.level++;
            // Azuriranje titule
            this.title = LevelingSystemHelper.getTitleForLevel(this.level);
            // Dodavanje Power Points-a
            this.powerPoints += LevelingSystemHelper.getPowerPointsRewardForLevel(this.level);

            // Oduzimanje potrosenog XP-a za prelazak na novi nivo
            this.xp -= requiredXp;

            // Azuriranje requiredXp za sledecu iteraciju
            if (this.level > 0) {
                requiredXp = LevelingSystemHelper.getRequiredXpForNextLevel(this.level);
            } else {
                // Ako je korisnik i dalje na nivou 0, sprijeciti beskonacnu petlju
                break;
            }
        }
    }
}
