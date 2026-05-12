package com.juki.model;

import java.time.LocalDate;

public class DailyMood {
    private int id;
    private String moodName;
    private LocalDate date;
    private int userId;

    public DailyMood(int id, String moodName, LocalDate date, int userId) {
        this.id = id;
        this.moodName = moodName;
        this.date = date;
        this.userId = userId;
    }

    public int getId() { return id; }
    public String getMoodName() { return moodName; }
    public LocalDate getDate() { return date; }
    public int getUserId() { return userId; }
}
