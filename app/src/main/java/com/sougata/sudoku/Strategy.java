package com.sougata.sudoku;

public class Strategy {
    int id;
    private String title;
    private String[] description;
    private String[] images;

    public Strategy(int id, String title, String[] description, String[] images) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.images = images;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String[] getDescription() {
        return description;
    }

    public String[] getImages() {
        return images;
    }
}
