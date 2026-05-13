package com.juki.model;

public class User {
    private int id;
    private String fullName;
    private String username;
    private String profilePhotoPath;

    public User(int id, String fullName, String username) {
        this(id, fullName, username, null);
    }

    public User(int id, String fullName, String username, String profilePhotoPath) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.profilePhotoPath = profilePhotoPath;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }

    public void setProfilePhotoPath(String profilePhotoPath) {
        this.profilePhotoPath = profilePhotoPath;
    }
}
