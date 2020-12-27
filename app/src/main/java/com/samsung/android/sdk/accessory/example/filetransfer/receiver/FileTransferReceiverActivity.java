/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd. All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that
 * the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or
 *       other materials provided with the distribution.
 *     * Neither the name of Samsung Electronics Co., Ltd. nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.FileModel;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ProfileModel;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.FileTransferReceiver.FileAction;

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.FileTransferReceiver.ReceiverBinder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.io.IOException;
import java.util.List;


import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.DEST_DIRECTORY;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.CSV_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MODEL_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.SCHEDULER_INTERVAL;

public class FileTransferReceiverActivity<ArrayList, listItems, ListElements> extends AppCompatActivity {
    private static final String TAG = "FileTransferReceiverActivity";
    private static final int STORAGE_PERMISSION_CODE = 1;
    private static boolean mIsUp = false;
    private int mTransId;
    private Context mCtxt;
    private String mDirPath;
    private String mFilePath;
    private AlertDialog mAlert;
    private ProgressBar mRecvProgressBar;
    private FileTransferReceiver mReceiverService;
    DatabaseHelper myDb;

    private ListView listview;
    private Button Addbutton;
    private Button reloadBtn;
    //button objects
    private Button buttonStart;
    private Button buttonStop;


    private EditText GetValue;

    java.util.ArrayList<String> listItems;

    ArrayAdapter<String> adapter;


    int cameraRequestCode = 001;
    Classifier classifier;


    Intent mServiceIntent;
    private FileTransferReceiver mYourService;
    public static String PACKAGE_NAME;



    DownloadManager downloadmanager;

    private NotificationManagerCompat notificationManager;


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "Service disconnected");
            mReceiverService = null;
        }

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            Log.d(TAG, "Service connected");
            mReceiverService = ((ReceiverBinder) binder).getService();
            mReceiverService.registerFileAction(getFileAction());
            mServiceIntent = new Intent(FileTransferReceiverActivity.this, mReceiverService.getClass());

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ft_receiver_activity);

        mIsUp = true;
        mCtxt = getApplicationContext();
        myDb = new DatabaseHelper(this);

//        if(hasStoragePermission())








//        requestStoragePermission();

//
//
//        mServiceIntent = new Intent(FileTransferReceiverActivity.this, FileTransferReceiver.class);
//        mServiceIntent.setAction(String.valueOf(Constants.ACTION.STARTFOREGROUND_ACTION));
//
//        startService(mServiceIntent);

//        Intent serviceIntent = new Intent(this, ExampleService.class);
//        serviceIntent.putExtra("inputExtra", "input text sample");
//        ContextCompat.startForegroundService(this, serviceIntent);

//        classifier = new Classifier(Utils.assetFilePath(this, "bayesbeat_cpu_codeless.pt"));


        PACKAGE_NAME = getApplicationContext().getPackageName();


        listview = (ListView) findViewById(R.id.list);

        List<FileModel> filesList = filesList = myDb.getFiles(5);




        listItems = new java.util.ArrayList<String>();
        listItems.add("List of received files");
        for(FileModel file: filesList){
            String name = file.getFileName(); // id is column name in db
            listItems.add("file :" + name);
        }


        Log.d("here in activity sleep", String.valueOf(Thread.currentThread().getId()));


        new StarterTask().execute("my string parameter");

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        listview.setAdapter(adapter);

        Addbutton = (Button) findViewById(R.id.btAddDb);
        reloadBtn = (Button) findViewById(R.id.btReload);
        //getting buttons from xml
        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);

//
//        mRecvProgressBar = (ProgressBar) findViewById(R.id.RecvProgress);
//        mRecvProgressBar.setMax(100);





        Addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<FileModel> filesList = filesList = myDb.getFiles(5);
                listItems.clear();
                listItems.add("List of received files");
                for(FileModel file: filesList){
                    String name = file.getFileName(); // id is column name in db
                    listItems.add("file :" + name);
                }
                ProfileModel profile = myDb.get_profile();

                Toast.makeText(mCtxt, profile.getUserName() + " " + profile.getDevice_id(), Toast.LENGTH_LONG).show();

                adapter.notifyDataSetChanged();

            }
        });

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServiceIntent.setAction(String.valueOf(Constants.ACTION.STARTFOREGROUND_ACTION));
                if (!isMyServiceRunning(mReceiverService.getClass())) {
                    startService(mServiceIntent);
                }

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServiceIntent.setAction(String.valueOf(Constants.ACTION.STOPFOREGROUND_ACTION));
                if (isMyServiceRunning(mReceiverService.getClass())) {

                    startService(mServiceIntent);

                }
            }
        });

        mServiceIntent = new Intent();
        mServiceIntent.setAction("restartservice");
        mServiceIntent.setClass(this, Restarter.class);
        this.sendBroadcast(mServiceIntent);
        Log.d("why ", "-------------------------------------------------------------man");

//        mCtxt.bindService(new Intent(getApplicationContext(), FileTransferReceiver.class),
//                this.mServiceConnection, Context.BIND_AUTO_CREATE);
    }


    private class StarterTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {

            Log.d("tag", " here before  "+ CSV_FILE_DIR);

            File csvFolder = new File(CSV_FILE_DIR);
            File modelFolder = new File(MODEL_FILE_DIR);


            boolean success = true;
            if (!csvFolder.exists()) {
                success = csvFolder.mkdirs();
                Log.d("tag", "Files Created here "+ CSV_FILE_DIR + success);

            }
            if (!modelFolder.exists()) {
                success = modelFolder.mkdirs();
            }
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(mCtxt, " No SDCARD Present", Toast.LENGTH_SHORT).show();
                finish();
            } else {

            }
            Log.d("tag", "Files Created "+ CSV_FILE_DIR);





            String modelName = "bayesbeat_cpu.pt";
            AssetManager am = getAssets();
            InputStream inputStream = null;
            try {
                inputStream = am.open(modelName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                File f = new File(MODEL_FILE_DIR + modelName);
                OutputStream outputStream = new FileOutputStream(f);
                byte buffer[] = new byte[1024];
                int length = 0;

                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                //Logging exception
            }



            ComponentName componentName = new ComponentName(mCtxt, FileJobService.class);
            JobInfo info = new JobInfo.Builder(123, componentName)
//                .setRequiresCharging(true)
//                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPersisted(true)
                    .setPeriodic( SCHEDULER_INTERVAL)
                    .build();
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = scheduler.schedule(info);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d("tag", "Job scheduled");
            } else {
                Log.d("tag", "Job scheduling failed");
            }

            return "model copied";
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.d("list----", String.valueOf(service.service));
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }


    @Override
    protected void onStart() {
        Log.d("activity", "in on start ");

        mIsUp = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
//        mServiceIntent = new Intent();
//        mServiceIntent.setAction(String.valueOf(Constants.ACTION.STARTFOREGROUND_ACTION));
//        mServiceIntent.setClass(this, Restarter.class);
//        this.sendBroadcast(mServiceIntent);
        Log.d("activity", "in on stop ");
        mIsUp = false;
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d("activity", "in on pause ");

        mIsUp = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d("activity", "in on resume ");

        mIsUp = true;
        super.onResume();
    }

    public void onDestroy() {

        Log.d("activity", "in on destroy ");

        mIsUp = false;
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        Log.d("activity", "in on stop ");

        mIsUp = false;
        moveTaskToBack(true);
    }

    // for Android before 2.0, just in case
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mIsUp = false;
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static boolean isUp() {
        return mIsUp;
    }


    private FileAction getFileAction() {
        return new FileAction() {
            @Override
            public void onFileActionError() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(mCtxt, "Transfer cancelled " + "Error", Toast.LENGTH_SHORT).show();

//                        mRecvProgressBar.setProgress(0);


                    }
                });
            }

            @Override
            public void onFileActionProgress(final long progress) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mRecvProgressBar.setProgress((int) progress);
//                    }
//                });
            }

            @Override
            public void onFileActionTransferComplete(final String fileName) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        mRecvProgressBar.setProgress(0);

//                        Toast.makeText(mCtxt, "Receive Completed!", Toast.LENGTH_SHORT).show();

//                        boolean isInserted = myDb.insertData(fileName,
//                                "sw",
//                                0,
//                                0);
//                        if(isInserted == true)
//                            Toast.makeText(mCtxt,"Data Inserted",Toast.LENGTH_LONG).show();
//                        else
//                            Toast.makeText(mCtxt,"Data not Inserted",Toast.LENGTH_LONG).show();

                    }
                });
            }

            @Override
            public void onFileActionTransferRequested(int id, String path) {
                mFilePath = path;
                mTransId = id;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            String receiveFileName = mFilePath.substring(mFilePath.lastIndexOf("/"), mFilePath.length());
                            mReceiverService.receiveFile(mTransId, DEST_DIRECTORY
                                    + receiveFileName, true);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(mCtxt, "IllegalArgumentException", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
            }
        };
    }

//    private FileAction getFileAction() {
//        return new FileAction() {
//            @Override
//            public void onFileActionError() {
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
//////                        if (mAlert != null && mAlert.isShowing()) {
//////                            mAlert.dismiss();
//////                        }
////                        Toast.makeText(mCtxt, "Transfer cancelled " + "Error", Toast.LENGTH_SHORT).show();
////
//////                        mRecvProgressBar.setProgress(0);
////
////
////                    }
////                });
//            }
//
//            @Override
//            public void onFileActionProgress(final long progress) {
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        mRecvProgressBar.setProgress((int) progress);
////                    }
////                });
//            }
//
//            @Override
//            public void onFileActionTransferComplete(final String fileName) {
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        mRecvProgressBar.setProgress(0);
//////                        if (mAlert != null) {
//////                            mAlert.dismiss();
//////                        }
////                        Toast.makeText(mCtxt, "Receive Completed!", Toast.LENGTH_SHORT).show();
////
////                        boolean isInserted = myDb.insertData(fileName,
////                                "sw",
////                                0,
////                                0);
////                        if (isInserted == true)
////                            Toast.makeText(mCtxt, "Data Inserted", Toast.LENGTH_LONG).show();
////                        else
////                            Toast.makeText(mCtxt, "Data not Inserted", Toast.LENGTH_LONG).show();
////
////                    }
////                });
//            }
//
//            @Override
//            public void onFileActionTransferRequested(int id, String path) {
//                mFilePath = path;
//                mTransId = id;
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        try {
//                            String receiveFileName = mFilePath.substring(mFilePath.lastIndexOf("/"), mFilePath.length());
//                            mReceiverService.receiveFile(mTransId, DEST_DIRECTORY
//                                    + receiveFileName, true);
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            Toast.makeText(mCtxt, "IllegalArgumentException", Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                });
//            }
//        };
//
//    }
}
