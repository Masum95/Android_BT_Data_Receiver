package com.samsung.android.sdk.accessory.example.filetransfer.receiver.FileRecvRecord;

class CustomListItem {
    String title;
    String details;
    int backgroundColor;
    int record_strength;
    public CustomListItem(String title, String details, int backgroundColor, int record_strength) {
        this.title = title;
        this.details = details;
        this.backgroundColor = backgroundColor;
        this.record_strength = record_strength;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getRecord_strength() {
        return record_strength;
    }

    public void setRecord_strength(int record_strength) {
        this.record_strength = record_strength;
    }
}