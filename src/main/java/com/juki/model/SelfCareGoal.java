package com.juki.model;

import java.time.LocalDate;

public class SelfCareGoal {
    private Integer id;
    private String title;
    private boolean isCompleted;
    private LocalDate date;

    public SelfCareGoal() {}

    public SelfCareGoal(Integer id, String title, boolean isCompleted, LocalDate date) {
        this.id = id;
        this.title = title;
        this.isCompleted = isCompleted;
        this.date = date;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}