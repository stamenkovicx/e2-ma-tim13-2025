package com.example.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    private String uid;
    private String email;
    private String username;
    private String avatar;
    private int level;
    private String titula;
    private int pp;
    private int xp;
    private int novcici;

    public User() {
        // Prazan konstruktor
    }

    // --- GETTERI I SETTERI ZA SVA POLJA ---

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getTitula() { return titula; }
    public void setTitula(String titula) { this.titula = titula; }

    public int getPp() { return pp; }
    public void setPp(int pp) { this.pp = pp; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getNovcici() { return novcici; }
    public void setNovcici(int novcici) { this.novcici = novcici; }
}