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
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.MedicalProfileModel;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ResultModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.CSV_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.DEST_DIRECTORY;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MEDICAL_PROFILE_URL;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MODEL_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.RECORD_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.SCHEDULER_INTERVAL;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.SHARED_PREF_ID;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.WATCH_SRC_KEYWORD;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Utils.getTimeStampFromFile;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Utils.startMultpleModelRunnerAsyncTaskInParallel;


public class FileTransferReceiverFragment extends Fragment {
    Context thisContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        thisContext = container.getContext();

        return inflater.inflate(R.layout.dashboard2, container, false);
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

    TextView phone_numTextView, nameTextView, upBpmTxtView, downBpmTxtView;
    ImageView downarrowView, uparrowView;
    TabLayout tabLayout;
    private EditText GetValue;

    java.util.ArrayList<String> listItems;

    ArrayAdapter<String> adapter;


    int cameraRequestCode = 001;
    Classifier classifier;


    Intent mServiceIntent;
    private FileTransferReceiver mYourService;
    public static String PACKAGE_NAME;

    LinearLayout warningLayout;

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
            Log.d(TAG, "Service connected+++++++++++++++++");
            mReceiverService = ((FileTransferReceiver.ReceiverBinder) binder).getService();
            mReceiverService.registerFileAction(getFileAction());

        }
    };
    SharedPreferences prefs;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsUp = true;
        mCtxt = thisContext;
        myDb = new DatabaseHelper(thisContext);
        prefs = getContext().getSharedPreferences(SHARED_PREF_ID, 0);

        boolean is_device_connected = prefs.getBoolean("IS_DEVICE_CONNECTED", false);

        prefs = mCtxt.getSharedPreferences(SHARED_PREF_ID, 0);
        int fileCount = myDb.getCountOfUploadedFilesLastNMin(30);
        if(fileCount == 0) {
            prefs.edit().putBoolean("IS_DEVICE_CONNECTED", false).apply();
        }



        String regi_id = myDb.get_profile().getRegi_id();

        PACKAGE_NAME = thisContext.getPackageName();

        warningLayout = (LinearLayout) getView().findViewById(R.id.warningLayout);
        phone_numTextView =  getView().findViewById(R.id.phoneNum);
        nameTextView =  getView().findViewById(R.id.profileName);
        upBpmTxtView =  getView().findViewById(R.id.upBpm);
        downBpmTxtView =  getView().findViewById(R.id.downBpm);
        downarrowView =  getView().findViewById(R.id.downarrow);
        uparrowView =  getView().findViewById(R.id.uparrow);
        tabLayout =  getView().findViewById(R.id.tabLayout);
        reloadBtn =  getView().findViewById(R.id.reloadBtn);
        warningLayout.setVisibility(View.VISIBLE);

        String mobile_num =  myDb.get_profile().getPhone_num();
        String name = myDb.getMedicalProfile(regi_id).getName();
        phone_numTextView.setText(mobile_num);
        nameTextView.setText(name);

        List<ResultModel> resultList = myDb.getResults(24);
        double maxHR = -1;
        double minHr = 1000;
        for(ResultModel resultModel: resultList){
            maxHR = Math.round(Math.max(resultModel.getAvg_hr(), maxHR));
            minHr = Math.round(Math.min(resultModel.getAvg_hr(), minHr));
        }
        if(maxHR != -1){
            upBpmTxtView.setText(String.valueOf(maxHR));
            uparrowView.setVisibility(View.VISIBLE);

        }

        if(minHr != 1000){
            downBpmTxtView.setText(String.valueOf(minHr));
            downarrowView.setVisibility(View.VISIBLE);

        }


        if(is_device_connected){
            tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#90EE90"));
            tabLayout.getTabAt(0).setText("Receiving File");
        }

        LineChartModule bar = new LineChartModule(thisContext, getActivity(), R.id.lineChart);
//        listview = (ListView) getView().findViewById(R.id.list);
//        listItems = new java.util.ArrayList<String>();
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

//        adapter = new ArrayAdapter<String>(thisContext,
//                android.R.layout.simple_list_item_1,
//                listItems);
//        listview.setAdapter(adapter);
        Log.d("here in activity sleep", String.valueOf(Thread.currentThread().getId()));


        new FileTransferReceiverFragment.StarterTask().execute("my string parameter");


//        reloadBtn = (Button) getView().findViewById(R.id.btReload);
//        //getting buttons from xml
//        buttonStop = (Button) getView().findViewById(R.id.buttonStop);


        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                List<ResultModel> resultList  = myDb.getResults(5);
//                listItems.clear();
//                listItems.add("Previous History");
//                for(ResultModel result: resultList){
//                    String timestamp = result.getTimestamp(); // id is column name in db
//                    String res = result.getResult();
//                    listItems.add(timestamp + " <--> " + res);
//                }
//
//                adapter.notifyDataSetChanged();
//                getActivity().finish();
//                startActivity(getActivity().getIntent());
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);
                startActivity(getActivity().getIntent());
                getActivity().overridePendingTransition(0, 0);
            }
        });
//
//
//        buttonStop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mServiceIntent.setAction(String.valueOf(Constants.ACTION.STOPFOREGROUND_ACTION));
//                if (isMyServiceRunning(mReceiverService.getClass())) {
//
//                    getActivity().startService(mServiceIntent);
//
//                }
//            }
//        });

        mServiceIntent = new Intent();
        mServiceIntent.setAction("restartservice");
        mServiceIntent.setClass(thisContext, Restarter.class);
        thisContext.sendBroadcast(mServiceIntent);
        new syncMedicalProfile().execute();
//        mCtxt.bindService(new Intent(thisContext, FileTransferReceiver.class),
//                this.mServiceConnection, Context.BIND_AUTO_CREATE);
    }



    private class syncMedicalProfile extends AsyncTask<String, Integer, String> {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected String doInBackground(String... params) {

            OkHttpClient client = new OkHttpClient();

            JSONObject jsonObject = Utils.getMedicalProfileJson(getContext());




            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody body = RequestBody.create(jsonObject.toString(), JSON); // new
            Log.d("tag=======", String.valueOf(body));

            Request request = new Request.Builder().url(MEDICAL_PROFILE_URL) // The URL to send the data to
                    .post(body)
                    .build();
            Log.d("tag=======", String.valueOf(request));


            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(final Call call, final IOException e) {
                    // Handle the error
                    Log.d("sending", String.valueOf(e));
                }

                @Override
                public void onResponse(final Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        // Handle the error
                        Log.d("sending", "un successful");
                    } else {
                        Log.d("sending", " successful");
                    }

                    response.close();
                    // Upload successful
                }
            });
            return "hello";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }

    private class  StarterTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {

            Log.d("tag", " here before  " + CSV_FILE_DIR);

            File csvFolder = new File(CSV_FILE_DIR);
            File modelFolder = new File(MODEL_FILE_DIR);
            File recordFolder = new File(RECORD_FILE_DIR);


            boolean success = true;
            if (!csvFolder.exists()) {
                success = csvFolder.mkdirs();
                Log.d("tag", "Files Created here " + CSV_FILE_DIR + success);

            }
            if (!recordFolder.exists()) {
                success = recordFolder.mkdirs();
                Log.d("tag", "Files Created here " + recordFolder + success);

            }
            if (!modelFolder.exists()) {
                success = modelFolder.mkdirs();
            }
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(mCtxt, " No SDCARD Present", Toast.LENGTH_SHORT).show();
//                finish();
            } else {

            }
            Log.d("tag", "Files Created " + CSV_FILE_DIR);


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
                    .setPeriodic(SCHEDULER_INTERVAL)
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
        myDb.close();
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
        myDb.close();
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
                Log.d(TAG, "INSIDE XFER PROGRESS");

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mRecvProgressBar.setProgress((int) progress);
//                    }
//                });
            }

            @Override
            public void onFileActionTransferComplete(final String fileName) {
                Log.d(TAG, "INSIDE XFER COMPLETE");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isInserted = myDb.insertFileInfo(fileName,
                                WATCH_SRC_KEYWORD,
                                getTimeStampFromFile(fileName),
                                0,
                                0);
                        startMultpleModelRunnerAsyncTaskInParallel(new ModelRunner(mCtxt), fileName);
                        if(isInserted == true)
                            Toast.makeText(mCtxt,"Data Inserted",Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(mCtxt,"Data not Inserted",Toast.LENGTH_LONG).show();


                    }
                });
            }

            @Override
            public void onFileActionTransferRequested(int id, String path) {
                Log.d(TAG, "INSIDE XFER REQUESTED");

                mFilePath = path;
                mTransId = id;
                tabLayout.setBackgroundColor(Color.parseColor("#90EE90"));
                tabLayout.getTabAt(0).setText("Connected To Watch");

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