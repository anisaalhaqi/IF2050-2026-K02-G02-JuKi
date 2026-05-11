package com.juki.model;

public class Photo {
    private Integer id;
    private String filePath;

    public Photo() {}

    public Photo(Integer id, String filePath) {
        this.id = id;
        this.filePath = filePath;
    }

    public void uploadPhoto() {
        System.out.println("Proses unggah foto...");
    }

    public void deletePhoto() {
        System.out.println("Menghapus foto...");
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}