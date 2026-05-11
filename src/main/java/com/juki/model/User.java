package com.juki.model;

public class User {
    private int id;
    private String fullName;
    private String username;

    public User(int id, String fullName, String username) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }
}