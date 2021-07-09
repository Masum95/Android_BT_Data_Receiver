package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ResultModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.CSV_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.DEST_DIRECTORY;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MODEL_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.SCHEDULER_INTERVAL;


public class FileTransferReceiverFragment extends Fragment {
    Context thisContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        thisContext = container.getContext();

        return inflater.inflate(R.layout.ft_receiver_activity, container, false);
    }


    private static final String TAG = "MessageFragment";
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
    private Button reloadBtn;
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
            mReceiverService = ((FileTransferReceiver.ReceiverBinder) binder).getService();
            mReceiverService.registerFileAction(getFileAction());
            mServiceIntent = new Intent(thisContext, mReceiverService.getClass());

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsUp = true;
        mCtxt = thisContext;
        myDb = new DatabaseHelper(thisContext);


        PACKAGE_NAME = thisContext.getPackageName();



//        BarChartActivity bar = new BarChartActivity(thisContext, getActivity(), R.id.idBarChart);
        LineChartModule bar = new LineChartModule(thisContext, getActivity(), R.id.lineChart);
//        listview = (ListView) getView().findViewById(R.id.list);
//        listItems = new java.util.ArrayList<String>();
//
//
//
//
//
//        List<ResultModel> resultList  = myDb.getResults(5);
//        listItems.clear();
//        listItems.add("Previous History");
//        for(ResultModel result: resultList){
//            String timestamp = result.getTimestamp(); // id is column name in db
//            String res = result.getResult();
//            String activity = result.getAvg_activity();
//            double hr = result.getAvg_hr();
//            listItems.add(timestamp + " <--> " + res +  " <--> " +  activity + "<-->" + hr);
//        }

        Log.d("here in activity sleep", String.valueOf(Thread.currentThread().getId()));


        new FileTransferReceiverFragment.StarterTask().execute("my string parameter");

//        adapter = new ArrayAdapter<String>(thisContext,
//                android.R.layout.simple_list_item_1,
//                listItems);
//        listview.setAdapter(adapter);

        reloadBtn = (Button) getView().findViewById(R.id.btReload);
        //getting buttons from xml
        buttonStop = (Button) getView().findViewById(R.id.buttonStop);



        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ResultModel> resultList  = myDb.getResults(5);
                listItems.clear();
                listItems.add("Previous History");
                for(ResultModel result: resultList){
                    String timestamp = result.getTimestamp(); // id is column name in db
                    String res = result.getResult();
                    listItems.add(timestamp + " <--> " + res);
                }

                adapter.notifyDataSetChanged();

            }
        });


        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServiceIntent.setAction(String.valueOf(Constants.ACTION.STOPFOREGROUND_ACTION));
                if (isMyServiceRunning(mReceiverService.getClass())) {

                    getActivity().startService(mServiceIntent);

                }
            }
        });

        mServiceIntent = new Intent();
        mServiceIntent.setAction("restartservice");
        mServiceIntent.setClass(thisContext, Restarter.class);
        thisContext.sendBroadcast(mServiceIntent);

//        mCtxt.bindService(new Intent(thisContext, FileTransferReceiver.class),
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
//                finish();
            } else {

            }
            Log.d("tag", "Files Created "+ CSV_FILE_DIR);





            String modelName = "bayesbeat_cpu.pt";
            AssetManager am = getActivity().getAssets();
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
            JobScheduler scheduler = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
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
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
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
    public void onStart() {
        Log.d("activity", "in on start ");

        mIsUp = true;
        super.onStart();
    }

    @Override
    public void onStop() {
//        mServiceIntent = new Intent();
//        mServiceIntent.setAction(String.valueOf(Constants.ACTION.STARTFOREGROUND_ACTION));
//        mServiceIntent.setClass(this, Restarter.class);
//        this.sendBroadcast(mServiceIntent);
        Log.d("activity", "in on stop ");
        mIsUp = false;
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.d("activity", "in on pause ");

        mIsUp = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d("activity", "in on resume ");

        mIsUp = true;
        super.onResume();
    }

    public void onDestroy() {

        Log.d("activity", "in on destroy ");

        mIsUp = false;
        super.onDestroy();

    }

//    @Override
    public void onBackPressed() {
        Log.d("activity", "in on stop ");

        mIsUp = false;
        getActivity().moveTaskToBack(true);
    }

    // for Android before 2.0, just in case
//    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mIsUp = false;
            getActivity().moveTaskToBack(true);
            return true;
        }
        return super.getActivity().onKeyDown(keyCode, event);
    }

    public static boolean isUp() {
        return mIsUp;
    }


    private FileTransferReceiver.FileAction getFileAction() {
        return new FileTransferReceiver.FileAction() {
            @Override
            public void onFileActionError() {
                getActivity().runOnUiThread(new Runnable() {
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                    }
                });
            }

            @Override
            public void onFileActionTransferRequested(int id, String path) {
                mFilePath = path;
                mTransId = id;

                getActivity().runOnUiThread(new Runnable() {
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