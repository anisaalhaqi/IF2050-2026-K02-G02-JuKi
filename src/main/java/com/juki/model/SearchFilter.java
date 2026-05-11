package com.juki.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class SearchFilter {
    private String category;
    private String keyword;
    private LocalDate date;

    public SearchFilter(String category, String keyword, LocalDate date) {
        this.category = category;
        this.keyword = keyword;
        this.date = date;
    }

    public boolean isValidFilter() {
        return (keyword != null && keyword.length() >= 3) || 
               (category != null && !category.isEmpty()) || 
               (date != null);
    }

    public void applyFilter() {
        if (this.keyword != null) {
            this.keyword = this.keyword.toLowerCase();
        }
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
