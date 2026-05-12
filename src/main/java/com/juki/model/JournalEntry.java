package com.juki.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class JournalEntry {
    private Integer id;
    private String category;
    private String title;
    private String description;
    private String trigger;
    private String target;
    private LocalDate date;
    private LocalTime time;
    private List<Photo> photos = new ArrayList<>();
    private Integer userId;

    public JournalEntry() {}

    public JournalEntry(Integer id, String category, String title, String description, 
                        String trigger, String target, LocalDate date, LocalTime time, List<Photo> photos) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.description = description;
        this.trigger = trigger;
        this.target = target;
        this.date = date;
        this.time = time;
        this.photos = photos != null ? photos : new ArrayList<>();
    }

    public void createEntry(String category, String title, String description, String trigger, String target, LocalDate date, LocalTime time, List<Photo> photos) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.trigger = trigger;
        this.target = target;
        this.date = date;
        this.time = time;
        this.photos = photos != null ? photos : new ArrayList<>();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTrigger() { return trigger; }
    public void setTrigger(String trigger) { this.trigger = trigger; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public List<Photo> getPhotos() { return photos; }
    public void setPhotos(List<Photo> photos) { this.photos = photos != null ? photos : new ArrayList<>(); }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}
