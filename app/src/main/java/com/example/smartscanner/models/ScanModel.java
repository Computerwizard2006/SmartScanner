package com.example.smartscanner.models;

public class ScanModel {
    private String text;
    private String date;

    public ScanModel(String text, String date) {
        this.text = text;
        this.date = date;
    }

    public String getText() { return text; }
    public String getDate() { return date; }
}