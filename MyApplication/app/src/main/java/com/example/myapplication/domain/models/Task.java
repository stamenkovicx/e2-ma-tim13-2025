package com.example.myapplication.domain.models;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {

    private String id;
    private String name;
    private String description;
    private Category category;
    private String frequency;
    private Integer interval;
    private String intervalUnit;
    private Date startDate;
    private Date endDate;
    private Date executionTime;
    private Date completionDate;

    private String difficulty;
    private String importance;

    private int xpValue;
    private String userId;
    private TaskStatus status;
    private Date creationDate;

    public Task() {
        this.status = TaskStatus.AKTIVAN;
    }



    public Task(String name, String description, Category category, String frequency, Integer interval, String intervalUnit, Date startDate, Date endDate, Date executionTime, String difficulty, String importance, int xpValue, String userId) {
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
        this.xpValue = xpValue;
        this.userId = userId;
        this.status = TaskStatus.AKTIVAN;
        this.creationDate = new Date(); // Odmah postavi datum kreiranja
    }

    @Exclude
    public DifficultyType getDifficultyType() {
        if (this.difficulty == null) return null;
        try {
            return DifficultyType.valueOf(this.difficulty);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setDifficultyType(DifficultyType difficultyType) {
        if (difficultyType != null) {
            this.difficulty = difficultyType.name();
        }
    }

    @Exclude
    public ImportanceType getImportanceType() {
        if (this.importance == null) return null;
        try {
            return ImportanceType.valueOf(this.importance);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setImportanceType(ImportanceType importanceType) {
        if (importanceType != null) {
            this.importance = importanceType.name();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public Integer getInterval() { return interval; }
    public void setInterval(Integer interval) { this.interval = interval; }
    public String getIntervalUnit() { return intervalUnit; }
    public void setIntervalUnit(String intervalUnit) { this.intervalUnit = intervalUnit; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Date getExecutionTime() { return executionTime; }
    public void setExecutionTime(Date executionTime) { this.executionTime = executionTime; }
    public Date getCompletionDate() { return completionDate; }
    public void setCompletionDate(Date completionDate) { this.completionDate = completionDate; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getImportance() { return importance; }
    public void setImportance(String importance) { this.importance = importance; }
    public int getXpValue() { return xpValue; }
    public void setXpValue(int xpValue) { this.xpValue = xpValue; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public Date getCreationDate() { return creationDate; }
    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }
}