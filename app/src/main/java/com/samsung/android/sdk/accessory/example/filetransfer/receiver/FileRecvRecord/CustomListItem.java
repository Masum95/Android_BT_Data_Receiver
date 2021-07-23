package com.samsung.android.sdk.accessory.example.filetransfer.receiver.FileRecvRecord;

class CustomListItem {
    String title;
    String details;
    int backgroundColor;
    public CustomListItem(String title, String details, int backgroundColor) {
        this.title = title;
        this.details = details;
        this.backgroundColor = backgroundColor;
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

}