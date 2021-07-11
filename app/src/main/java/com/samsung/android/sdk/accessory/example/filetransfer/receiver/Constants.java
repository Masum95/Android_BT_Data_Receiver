package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.os.Environment;

import java.io.File;

public interface  Constants {
    String PKG_FOLDER_NAME = "BayesBeat/";
    String CSV_FILE_DIR = Environment.getExternalStorageDirectory() + File.separator + PKG_FOLDER_NAME + "csvFiles/";
    String MODEL_FILE_DIR = Environment.getExternalStorageDirectory() + File.separator + PKG_FOLDER_NAME + "model/";
    String DEST_DIRECTORY = CSV_FILE_DIR;
    String MODEL_NAME = "bayesbeat_cpu.pt";
//    String BASE_URL = "https://bayesbeat.herokuapp.com/";
    String BASE_URL = "http://192.168.31.62:8000/";

    String FILE_UPLOAD_GET_URL = BASE_URL + "file/upload/";
    String FILE_RCV_ACK_URL = BASE_URL + "file/ack/";
    String WATCH_ID_URL = BASE_URL + "file/watch_id/";
    String MEDICAL_PROFILE_URL = BASE_URL + "file/medical_profile/";

    String SERVER_SRC_KEYWORD = "SERVER";
    String WATCH_SRC_KEYWORD = "WATCH";


    String SHARED_PREF_ID = "FileXferAppPreference";

    long  SCHEDULER_INTERVAL = 15 * 60 * 1000;


    public enum ACTION{
        STARTFOREGROUND_ACTION,
        STOPFOREGROUND_ACTION,
    }

    public enum ACTION2{
        STARTFOREGROUND_ACTION,
        STOPFOREGROUND_ACTION,
    }
}

