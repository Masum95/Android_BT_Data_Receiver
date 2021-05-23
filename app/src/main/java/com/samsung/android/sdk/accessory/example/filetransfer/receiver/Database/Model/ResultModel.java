
package com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model;

public class ResultModel {
    public static final String TABLE_NAME = "result_table";

    public static final String COL_ID = "id";
    public static final String COL_FILE_NAME = "file_name";
    public static final String COL_RESULT = "result";
    public static final String COL_TIMESTAMP = "timestamp";



    private String fileName;
    private String result;
    private String timestamp;
    // Create table SQL query
    public static final String CREATE_TABLE =
            "create table " + TABLE_NAME + " ( " + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_FILE_NAME + " TEXT," +
                    COL_TIMESTAMP + " DATETIME ," +

                    COL_RESULT + " TEXT" +
                    ")";

    public ResultModel() {
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
