
package com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model;

public class ProfileModel {
    public static final String TABLE_NAME = "profile_table";

    public static final String COL_ID = "id";
    public static final String COL_USER_NAME = "user_name";
    public static final String COL_DEVICE_ID = "device_id";


    private String userName;

    private String device_id;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "create table " + TABLE_NAME + " ( " + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_USER_NAME + " TEXT," +
                    COL_DEVICE_ID + " TEXT" +
                    ")";

    public ProfileModel() {
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }
}
