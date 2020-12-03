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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
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
import android.support.v7.widget.Toolbar;

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.FileTransferReceiver.FileAction;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.FileTransferReceiver.ReceiverBinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.opencsv.CSVReader;
import java.io.IOException;
import java.io.FileReader;

public class FileTransferReceiverActivity<ArrayList, listItems, ListElements>  extends AppCompatActivity {
    private static final String TAG = "FileTransferReceiverActivity";
    private static boolean mIsUp = false;
    private static final String DEST_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath(); // better is Environment.getExternalStorageDirectory()
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ft_receiver_activity);
        myDb = new DatabaseHelper(this);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String csvfileString = this.getApplicationInfo().dataDir + File.separatorChar +  "myfile.csv";
        File csvfile = new File(csvfileString);
        AssetManager assetManager = getApplicationContext().getAssets();

        Log.d("tag", "here");
        try {
            InputStream csvStream = assetManager.open("myfile.csv");

            String[] colsList = new String[]{"a","b","c","d","e","f","g","h","i","j","k","l","m","n"};

            DataFrame df = new DataFrame();
            df.setColumnNames(colsList);
            df.readDataFrameFrom(csvStream);
            List<List<String>> myEntries = df.getDataFrame();// new java.util.ArrayList<List<String>>();
            String[] line;


            Log.d("tag", String.valueOf(myEntries.get(0).get(1)));
            Log.d("tag", String.valueOf(df.getNumRow()));
            Log.d("tag", String.valueOf(df.getNumCol()));
            Log.d("tag", String.valueOf(df.getCol("n")));

            Log.d("tag", String.valueOf(df.getCol("c")));



        } catch (Exception e) {
            e.printStackTrace();
            Log.d("tag", "not found");

            Toast.makeText(this, "The specified file was not found", Toast.LENGTH_SHORT).show();
        }



//        classifier = new Classifier(Utils.assetFilePath(this,"mobilenet-v2.pt"));
        classifier = new Classifier(Utils.assetFilePath(this,"bayesbeat_cpu_codeless.pt"));

//        Button capture = findViewById(R.id.capture);
//
//        capture.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View view){
//
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//                startActivityForResult(cameraIntent,cameraRequestCode);
//
//            }
//
//
//        });





        PACKAGE_NAME = getApplicationContext().getPackageName();


        listview = (ListView) findViewById(R.id.list);

        Cursor res = myDb.getLastN_Data(5);

        listItems = new java.util.ArrayList<String>();;
        listItems.add("List of received files");
        while (res.moveToNext()) {
            String id = res.getString( res.getColumnIndex("id") ); // id is column name in db
            String name = res.getString(res.getColumnIndex("name"));

            listItems.add("file :" +  name );

        }



        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        listview.setAdapter(adapter);

        Addbutton = (Button) findViewById(R.id.btAddDb);
        reloadBtn = (Button) findViewById(R.id.btReload);
        //getting buttons from xml
        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);



        mIsUp = true;
        mCtxt = getApplicationContext();
        mRecvProgressBar = (ProgressBar) findViewById(R.id.RecvProgress);
        mRecvProgressBar.setMax(100);


        Addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isInserted = myDb.insertData("filename",
                        "smartwatch",
                        1,
                        0);
                if(isInserted == true)
                    Toast.makeText(mCtxt,"Data Inserted",Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(mCtxt,"Data not Inserted",Toast.LENGTH_LONG).show();

            }
        });

        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = myDb.getLastN_Data(5);

                listItems.clear();
                listItems.add("List of received files");

                while (res.moveToNext()) {
                    String id = res.getString( res.getColumnIndex("id") ); // id is column name in db
                    String name = res.getString(res.getColumnIndex("name"));

                    listItems.add("file :" +  name );

                }

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
                Log.d("tag", "hello from  ");

                mServiceIntent.setAction(String.valueOf(Constants.ACTION.STOPFOREGROUND_ACTION));
                if (isMyServiceRunning(mReceiverService.getClass())) {

                    startService(mServiceIntent);

                }


            }
        });

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(mCtxt, " No SDCARD Present", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            mDirPath = Environment.getExternalStorageDirectory() + File.separator + "FileTransferReceiver";
            File file = new File(mDirPath);
            if (file.mkdirs()) {
                Toast.makeText(mCtxt, " Stored in " + mDirPath, Toast.LENGTH_LONG).show();
            }
        }
        mCtxt.bindService(new Intent(getApplicationContext(), FileTransferReceiver.class),
                    this.mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(requestCode == cameraRequestCode && resultCode == RESULT_OK){

            Intent resultView = new Intent(this,Result.class);

            resultView.putExtra("imagedata",data.getExtras());

            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

            String pred = classifier.predict(imageBitmap);
            resultView.putExtra("pred",pred);

            startActivity(resultView);

        }

    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.d("list----", String.valueOf(service.service));
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }



    @Override
    protected void onStart() {
        mIsUp = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        mIsUp = false;
        super.onStop();
    }

    @Override
    protected void onPause() {
        mIsUp = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        mIsUp = true;
        super.onResume();
    }

    public void onDestroy() {
        mServiceIntent = new Intent();
        mServiceIntent.setAction("restartservice");
        mServiceIntent.setClass(this, Restarter.class);
        this.sendBroadcast(mServiceIntent);
        Log.d("why ", "man");

        mIsUp = false;
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
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

    public static boolean isUp()
    {
        return mIsUp;
    }

    private FileAction getFileAction() {
        return new FileAction() {
            @Override
            public void onFileActionError() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        if (mAlert != null && mAlert.isShowing()) {
//                            mAlert.dismiss();
//                        }
                        Toast.makeText(mCtxt, "Transfer cancelled " + "Error", Toast.LENGTH_SHORT).show();

                        mRecvProgressBar.setProgress(0);


                    }
                });
            }

            @Override
            public void onFileActionProgress(final long progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecvProgressBar.setProgress((int) progress);
                    }
                });
            }

            @Override
            public void onFileActionTransferComplete(final String fileName) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecvProgressBar.setProgress(0);
//                        if (mAlert != null) {
//                            mAlert.dismiss();
//                        }
                        Toast.makeText(mCtxt, "Receive Completed!", Toast.LENGTH_SHORT).show();

                        boolean isInserted = myDb.insertData(fileName,
                                "sw",
                                0,
                                0);
                        if (isInserted == true)
                            Toast.makeText(mCtxt, "Data Inserted", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(mCtxt, "Data not Inserted", Toast.LENGTH_LONG).show();

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
}
