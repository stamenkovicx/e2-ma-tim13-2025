package com.example.myapplication.domain.models;

import java.io.Serializable;
import java.util.Date;

/*
  Predstavlja jedan zadatak u aplikaciji.
  Zadatak može biti jednokratan ili ponavljajući i ima svojstva kao što su naziv,
  opis, težina, bitnost i povezani datumi.
*/
public class Task implements Serializable {
    private int id;
    private String name;
    private String description;
    private Category category;
    private String frequency; // "one-time" ili "recurring"
    private int interval;
    private String intervalUnit; // "dan", "nedelja"
    private Date startDate;
    private Date endDate;
    private Date executionTime;
    private DifficultyType difficulty;
    private ImportanceType importance;
    private int xpValue;

    public Task(int id, String name, String description, Category category, String frequency, int interval, String intervalUnit, Date startDate, Date endDate, Date executionTime, DifficultyType difficulty, ImportanceType importance) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.frequency = frequency;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
        this.startDate = startDate;
        this.endDate = endDate;
        this.executionTime = executionTime;
        this.difficulty = difficulty;
        this.importance = importance;
        this.xpValue = calculateTaskXP(difficulty, importance);
    }

    // Pomoćna privatna funkcija za izračunavanje XP vrednosti na osnovu težine i bitnosti.
    private int calculateTaskXP(DifficultyType difficulty, ImportanceType importance) {
        return difficulty.getXpValue() + importance.getXpValue();
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(String intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }

    public DifficultyType getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyType difficulty) {
        this.difficulty = difficulty;
    }

    public ImportanceType getImportance() {
        return importance;
    }

    public void setImportance(ImportanceType importance) {
        this.importance = importance;
    }

    public int getXpValue() {
        return xpValue;
    }

    public void setXpValue(int xpValue) {
        this.xpValue = xpValue;
    }
}