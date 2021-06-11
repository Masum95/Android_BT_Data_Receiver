
package com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model;

public class ProfileModel {
    public static final String TABLE_NAME = "profile_table";

    public static final String COL_ID = "id";
    public static final String COL_USER_NAME = "user_name";
    public static final String COL_DEVICE_ID = "device_id";
    public static final String COL_PHONE_NUM = "phone_num";
    public static final String COL_REGI_ID = "registration_id";


    private String userName;

    private String device_id;
    private String phone_num;
    private String regi_id;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "create table " + TABLE_NAME + " ( " + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_USER_NAME + " TEXT," +
                    COL_PHONE_NUM + " TEXT," +
                    COL_DEVICE_ID + " TEXT," +
                    COL_REGI_ID + " TEXT" +
                    ")";

    public ProfileModel() {
    }

    public String getRegi_id() {
        return regi_id;
    }

    public void setRegi_id(String regi_id) {
        this.regi_id = regi_id;
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

    public String getPhone_num() {
        return phone_num;
    }

    public void setPhone_num(String phone_num) {
        this.phone_num = phone_num;
    }


    @Override
    public String toString() {
        return "ProfileModel{" +
                "userName='" + userName + '\'' +
                ", device_id='" + device_id + '\'' +
                ", phone_num='" + phone_num + '\'' +
                ", regi_id='" + regi_id + '\'' +
                '}';
    }
}
