package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.os.Environment;

import java.io.File;

public interface  Constants {
    String PKG_FOLDER_NAME = "BayesBeat/";
    String CSV_FILE_DIR = Environment.getExternalStorageDirectory() + File.separator + PKG_FOLDER_NAME + "csvFiles/";
    String MODEL_FILE_DIR = Environment.getExternalStorageDirectory() + File.separator + PKG_FOLDER_NAME + "model/";
    String DEST_DIRECTORY = CSV_FILE_DIR;
    String MODEL_NAME = "bayesbeat_cpu.pt";
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

