package com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model;

public class FileModel {
    public static final String TABLE_NAME = "files_table";

    public static final String COL_ID = "id";
    public static final String COL_FILE_NAME = "file_name";
    public static final String COL_IS_UPLOADED = "is_uploaded";
    public static final String COL_SRC = "source";
    public static final String COL_UPLOAD_TIME = "uploaded_at";
    public static final String COL_FILE_GEN_TIME = "gen_at";
    public static final String COL_RESULT_GEN = "result_generated";

    private String fileName;

    private int isUploaded;
    private String src;
    private String uploadedAt;
    private String fileGenTime;
    private int resultGen;



    // Create table SQL query
    public static final String CREATE_TABLE =
            "create table " + TABLE_NAME + " ( " + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_FILE_NAME + " TEXT," +
                    COL_IS_UPLOADED + " INTEGER," +
                    COL_SRC + " TEXT," +
                    COL_UPLOAD_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    COL_FILE_GEN_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    COL_RESULT_GEN + " INTEGER" +
                    ")";

    public FileModel() {
    }


    public String getFileName() {
        return fileName;
    }

    public FileModel(String fileName, int isUploaded, String src, String uploadedAt, String fileGenTime, int resultGen) {
        this.fileName = fileName;
        this.isUploaded = isUploaded;
        this.src = src;
        this.uploadedAt = uploadedAt;
        this.resultGen = resultGen;
        this.fileGenTime = fileGenTime;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getIsUploaded() {
        return isUploaded;
    }

    public void setIsUploaded(int isUploaded) {
        this.isUploaded = isUploaded;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public int getResultGen() {
        return resultGen;
    }

    public void setResultGen(int resultGen) {
        this.resultGen = resultGen;
    }

    public String getFileGenTime() {
        return fileGenTime;
    }

    public void setFileGenTime(String fileGenTime) {
        this.fileGenTime = fileGenTime;
    }
}
