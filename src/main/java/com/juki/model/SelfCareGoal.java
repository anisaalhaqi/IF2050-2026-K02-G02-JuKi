package com.juki.model;

import java.time.LocalDate;

public class SelfCareGoal {
    private Integer id;
    private String title;
    private boolean isCompleted;
    private LocalDate date;
    private int userId;

    public SelfCareGoal() {}

    public SelfCareGoal(Integer id, String title, boolean isCompleted, LocalDate date, int userId) {
        this.id = id;
        this.title = title;
        this.isCompleted = isCompleted;
        this.date = date;
        this.userId = userId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}