package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.os.Environment;

import java.io.File;

public interface  Constants {
    String PKG_FOLDER_NAME = "BayesBeat/";
    String CSV_FILE_DIR = Environment.getExternalStorageDirectory() + File.separator + PKG_FOLDER_NAME + "csvFiles/";
    String RECORD_FILE_DIR = Environment.getExternalStorageDirectory() + File.separator + PKG_FOLDER_NAME + "records/";
    String MODEL_FILE_DIR = Environment.getExternalStorageDirectory() + File.separator + PKG_FOLDER_NAME + "model/";
    String DEST_DIRECTORY = CSV_FILE_DIR;
    String MODEL_NAME = "bayesbeat_cpu.pt";
//    String BASE_URL = "https://bayesbeat.herokuapp.com/";
    String BASE_URL = "http://192.168.31.62:8000/";

    String FILE_UPLOAD_GET_URL = BASE_URL + "file/upload/";
    String FILE_RCV_ACK_URL = BASE_URL + "file/ack/";
    String WATCH_ID_URL = BASE_URL + "file/watch_id/";
    String MEDICAL_PROFILE_URL = BASE_URL + "file/medical_profile/";
    String PDF_GENERATE_URL = BASE_URL + "file/pdf/";

    String SERVER_SRC_KEYWORD = "SERVER";
    String WATCH_SRC_KEYWORD = "WATCH";
    String DATE_FORMAT = "WATCH";
    double ACCEPTED_SIG_RATIO_FOR_RECORD_LIST = 0.1;
    double ACCEPTED_SIG_RATIO_FOR_PDF_PRINT = 0.2;


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

