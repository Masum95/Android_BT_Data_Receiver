
package com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model;

public class ResultModel {
    public static final String TABLE_NAME = "result_table";

    public static final String COL_ID = "id";
    public static final String COL_FILE_NAME = "file_name";
    public static final String COL_RESULT = "result";
    public static final String COL_AVG_ACTIVITY = "avg_activity";
    public static final String COL_AVG_HEART_RATE = "avg_hr";
    public static final String COL_TIMESTAMP = "timestamp";



    private String fileName;
    private String result;
    private String timestamp;



    private double avg_hr;
    private String avg_activity;
    // Create table SQL query
    public static final String CREATE_TABLE =
            "create table " + TABLE_NAME + " ( " + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_FILE_NAME + " TEXT," +
                    COL_TIMESTAMP + " TIMESTAMP  ," +
                    COL_AVG_ACTIVITY + " TEXT ," +
                    COL_AVG_HEART_RATE + " REAL ," +
                    COL_RESULT + " TEXT" +
                    ")";

    public ResultModel() {
    }

    public double getAvg_hr() {
        return avg_hr;
    }

    public void setAvg_hr(double avg_hr) {
        this.avg_hr = avg_hr;
    }

    public String getAvg_activity() {
        return avg_activity;
    }

    public void setAvg_activity(String avg_activity) {
        this.avg_activity = avg_activity;
    }
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
