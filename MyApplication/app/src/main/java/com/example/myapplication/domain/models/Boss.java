package com.example.myapplication.domain.models;

public class Boss {
    private int level;
    private int hp;
    private int maxHp;

    public Boss(int level, int previousHp) {
        this.level = level;

        if (level == 1) {
            this.hp = 200;
        } else {
            this.hp = previousHp * 2 + previousHp / 2;
        }
        this.maxHp = this.hp;
    }

    public int getLevel() {
        return level;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public boolean isDefeated() {
        return hp <= 0;
    }

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp < 0) {
            hp = 0;
        }
    }
}
