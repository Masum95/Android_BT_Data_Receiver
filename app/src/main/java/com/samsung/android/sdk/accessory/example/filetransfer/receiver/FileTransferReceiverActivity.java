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
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
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
import android.support.v7.widget.Toolbar;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.jacksonandroidnetworking.JacksonParserFactory;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.FileTransferReceiver.FileAction;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.FileTransferReceiver.ReceiverBinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.opencsv.CSVReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.FileReader;


import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.NotificationHandler.CHANNEL_1_ID;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.NotificationHandler.CHANNEL_2_ID;

public class FileTransferReceiverActivity<ArrayList, listItems, ListElements> extends AppCompatActivity {
    private static final String TAG = "FileTransferReceiverActivity";
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

    final static String pkgFolderName = "BayesBeat/";
    final static String csvFileDir = Environment.getExternalStorageDirectory() + File.separator + pkgFolderName + "csvFiles/";
    final static String modelFileDir = Environment.getExternalStorageDirectory() + File.separator + pkgFolderName + "model/";
    DownloadManager downloadmanager;
    private static final String DEST_DIRECTORY = csvFileDir;

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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ft_receiver_activity);
        myDb = new DatabaseHelper(this);

        notificationManager = NotificationManagerCompat.from(this);  // for pushing notification

        AndroidNetworking.initialize(getApplicationContext());  // for api request
        AndroidNetworking.setParserFactory(new JacksonParserFactory());
        downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }


//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        String csvfileString = this.getApplicationInfo().dataDir + File.separatorChar +  "myfile.csv";
//        File csvfile = new File(csvfileString);
//        AssetManager assetManager = getApplicationContext().getAssets();
//
//        try {
//            InputStream csvStream = assetManager.open("myfile.csv");
//
//            String[] colsList = new String[]{"a","b","c","d","e","f","g","h","i","j","k","l","m","n"};
//
//            DataFrame df = new DataFrame();
//            df.setColumnNames(colsList);
//            df.readDataFrameFrom(csvStream);
//            List<List<String>> myEntries = df.getDataFrame();// new java.util.ArrayList<List<String>>();
//            String[] line;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.d("tag", "not found");
//
//            Toast.makeText(this, "The specified file was not found", Toast.LENGTH_SHORT).show();
//        }

        classifier = new Classifier(Utils.assetFilePath(this, "bayesbeat_cpu_codeless.pt"));


        PACKAGE_NAME = getApplicationContext().getPackageName();


        listview = (ListView) findViewById(R.id.list);

        Cursor res = myDb.getLastN_Data(5);

        listItems = new java.util.ArrayList<String>();
        ;
        listItems.add("List of received files");
        while (res.moveToNext()) {
            String id = res.getString(res.getColumnIndex("id")); // id is column name in db
            String name = res.getString(res.getColumnIndex("name"));

            listItems.add("file :" + name);

        }


        adapter = new ArrayAdapter<String>(this,
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


        File csvFolder = new File(csvFileDir);
        File modelFolder = new File(modelFileDir);

        boolean success = true;
        if (!csvFolder.exists()) {
            success = csvFolder.mkdirs();
        }
        if (!modelFolder.exists()) {
            success = modelFolder.mkdirs();
        }
        new StarterTask().execute("my string parameter");

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

                    DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);

                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);

                    Cursor cursor = manager.query(query);

                    if (cursor.moveToFirst()) {
                        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            // process download
                            String filePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            // get other required data by changing the constant passed to getColumnIndex
                            Log.d("file_rcvd", filePath);
                            boolean isInserted = myDb.insertData(filePath,
                                    "server",
                                    1,
                                    1);
                            if (isInserted)
                                Toast.makeText(mCtxt, "Server Data Inserted", Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(mCtxt, "Data not Inserted", Toast.LENGTH_LONG).show();
                            new ModelRunner().execute(filePath);

                        }
                        Log.d("file_rcvd_here", String.valueOf(status) + " " + String.valueOf(reason));


                    }

                    cursor.close();

                }
            }
        };

        registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));


        Addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAllUsers/{pageNumber}")
                AndroidNetworking.get("https://bayesbeat.herokuapp.com/file/upload/")
//                        .addPathParameter("pageNumber", "0")
                        .addQueryParameter("start_time", "2020-11-27T19:58:19")
                        .addQueryParameter("end_time", "2020-11-27T19:58:19")
//                        .addQueryParameter("device_id", "3")
//                        .addHeaders("token", "1234")
                        .setPriority(Priority.LOW)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {

                            @Override
                            public void onResponse(JSONObject response) {
                                // do anything with response
                                Log.d("json_response", String.valueOf(response));
                                try {
                                    JSONArray ara = response.getJSONArray("data");
                                    JSONObject obj = ara.getJSONObject(0);
                                    String down_url = String.valueOf(obj.getString("file"));
                                    String file_name = String.valueOf(obj.getString("file_name"));
                                    Log.d("json_response", down_url);
                                    Uri uri = Uri.parse(down_url);

                                    DownloadManager.Request request = new DownloadManager.Request(uri);
                                    request.setTitle("CSV File");
                                    request.setDescription("Downloading");
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setVisibleInDownloadsUi(false);
                                    request.setDestinationUri(Uri.parse("file://" + csvFileDir + "sample.csv"));

                                    downloadmanager.enqueue(request);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onError(ANError error) {
                                // handle error
                                Log.d("json_response_eror", String.valueOf(error));
                            }
                        });


            }
        });

        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = myDb.getLastN_Data(5);

                listItems.clear();
                listItems.add("List of received files");

                while (res.moveToNext()) {
                    String id = res.getString(res.getColumnIndex("id")); // id is column name in db
                    String name = res.getString(res.getColumnIndex("name"));

                    listItems.add("file :" + name);

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


    private class StarterTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            String modelName = "bayesbeat_cpu.pt";
            AssetManager am = getAssets();
            InputStream inputStream = null;
            try {
                inputStream = am.open(modelName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                File f = new File(modelFileDir + modelName);
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

            return "model copied";
        }
    }

    private class ModelRunner extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String csvFileName = params[0];
            String modelName = "bayesbeat_cpu.pt";
            AssetManager am = getAssets();
            InputStream inputStream = null;
            try {
                inputStream = am.open(modelName);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            File file = createFileFromInputStream(inputStream);
            try {
                File f = new File(modelFileDir + modelName);
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
            Python py = Python.getInstance();
            PyObject pyObject = py.getModule("model_runner");
//            PyObject obj = pyObject.callAttr("add", csvFileDir + "/myfile.csv");
            try {
                PyObject obj = pyObject.callAttr("input_preprocessing", modelFileDir + modelName, csvFileName);
                Log.d("tag", "Result from python " + obj.toString());
                return obj.toString();

            }catch (Exception e){
                return "";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.isEmpty()) return;
            super.onPostExecute(result);
            String title = "Your Heart Update";
            String message = result;
            Notification notification = new NotificationCompat.Builder(mCtxt, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_medicine)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build();
            notificationManager.notify(1, notification);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {

            Intent resultView = new Intent(this, Result.class);

            resultView.putExtra("imagedata", data.getExtras());

            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

            String pred = classifier.predict(imageBitmap);
            resultView.putExtra("pred", pred);

            startActivity(resultView);

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
