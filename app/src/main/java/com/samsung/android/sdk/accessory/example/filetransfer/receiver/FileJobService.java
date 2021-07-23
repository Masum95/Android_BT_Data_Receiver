package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.jacksonandroidnetworking.JacksonParserFactory;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.FileModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.CSV_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.FILE_RCV_ACK_URL;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.FILE_UPLOAD_GET_URL;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MODEL_FILE_DIR;


import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MODEL_NAME;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.SERVER_SRC_KEYWORD;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.NotificationHandler.CHANNEL_1_ID;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Utils.getTimeStampFromFile;

public class FileJobService extends JobService {
    private static final String TAG = "ExampleJobService";
    private boolean jobCancelled = false;

    public static String PACKAGE_NAME;

    DownloadManager downloadmanager;
    BroadcastReceiver receiver;
    private Context mCtxt;
    private NotificationManagerCompat notificationManager;
    private int download_count = 0;
    private List<List<Integer>> resultList;
    Intent mServiceIntent;
    DatabaseHelper myDb;
    String phone_num, regi_id;




    private static List<Integer> convertStringToIntAra(String str) {
        String[] result = str.split("[ ,\\]\\[]");
        List<Integer> list = new ArrayList<Integer>();

        for (String res : result) {
            try {
                list.add(Integer.parseInt(res));
            } catch (NumberFormatException e) {
            }
        }
        return list;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");
        AndroidNetworking.initialize(getApplicationContext());  // for api request
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

        notificationManager = NotificationManagerCompat.from(this);  // for pushing notification
        myDb = new DatabaseHelper(this);
        phone_num = myDb.get_profile().getPhone_num();
        regi_id = myDb.get_profile().getRegi_id();
        mCtxt = getApplicationContext();
        downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d("tag", "here    ---");
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
//                            Log.d("file_rcvd", filePath);

                            filePath = (filePath.split(":", 2)[1]).substring(2);
//                            Log.d("filepath", filePath);
                            Log.d("file_rcvd", filePath);

                            String[] parts = filePath.split("\\.");
                            Log.d("file_rcvd", String.valueOf(parts));

                            String fileExtension = parts[parts.length - 1];
                            Log.d("file_rcvd", fileExtension+ "  ---" + (fileExtension != "csv"));

                            if(!fileExtension.equalsIgnoreCase("csv")) return;

                            boolean isInserted = myDb.insertFileInfo(filePath,
                                    SERVER_SRC_KEYWORD, // SERVER_SRC_KEYWORD
                                    getTimeStampFromFile(filePath),
                                    1,
                                    1);
                            Log.d("file_rcvd", fileExtension);

                            new ModelRunner(context).execute(filePath);

                        }
                        Log.d("file_rcvd_here", String.valueOf(status) + " " + String.valueOf(reason));

                    }

                    cursor.close();

                }
            }
        };



        registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        new Downloader().execute();
        new FileUploadToServer(mCtxt).execute();
//
        return true;
    }

    private class ModelRunnerTmp extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String csvFileName = "_nf8VNRdtlCse1yloaZqM0ykXQA-001_1626025296";
            Log.d("tag", "before from python " + csvFileName);
            Python py = Python.getInstance();
            PyObject pyObject = py.getModule("model_runner");


            try {
                PyObject obj = pyObject.callAttr("input_preprocessingTmp");
                Log.d("tag", "Result from python " + obj.toString());
                String jsonString = obj.toString();
                JSONObject jsonObject = new JSONObject(jsonString);
                String predict_ara = jsonObject.getString("predict_ara");
                JSONObject hear_rate_data = jsonObject.getJSONObject("hear_rate_data");
                myDb.createResult(csvFileName, getTimeStampFromFile(csvFileName), predict_ara,
                        hear_rate_data.getString("activity"), hear_rate_data.getDouble("hr"));
                return obj.toString();

            } catch (Exception e) {
                Log.d("tag", String.valueOf(e));

                return "";
            }
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }


    public class SendFileRecvAck extends AsyncTask<String, String, String> {

        String fileList;

        public SendFileRecvAck(String fileList) {
            this.fileList = fileList;
        }

        @Override
        protected String doInBackground(String... params) {
            OkHttpClient client = new OkHttpClient();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("file_list", fileList);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody body = RequestBody.create(jsonObject.toString(), JSON); // new
            Request request = new Request.Builder()
                    .url(FILE_RCV_ACK_URL) // The URL to send the data to
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {

                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "";
        }

    }


    private class Downloader extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

//            mServiceIntent = new Intent(getApplicationContext(), FileTransferReceiver.class);
//            mServiceIntent.setAction(String.valueOf(Constants.ACTION.STARTFOREGROUND_ACTION));
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                getApplicationContext().startForegroundService(mServiceIntent);
//            } else {
//                getApplicationContext().startService(mServiceIntent);
//            }


            AndroidNetworking.get(FILE_UPLOAD_GET_URL)
//                        .addPathParameter("pageNumber", "0")
                    .addQueryParameter("selective", "true")
//                    .addQueryParameter("start_time", "2020-11-28T01:58:19")
//                    .addQueryParameter("end_time", "2020-11-28T01:58:19")
                    .addQueryParameter("registration_id", regi_id)
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
                                resultList = new ArrayList<List<Integer>>();

                                download_count = ara.length();
                                String recvFileList = "";
                                for (int i = 0; i < ara.length(); i++) {
                                    JSONObject obj = ara.getJSONObject(i);
                                    String down_url = String.valueOf(obj.getString("file"));
                                    String file_name = String.valueOf(obj.getString("file_name"));
                                    Log.d("json_response", down_url);
                                    Uri uri = Uri.parse(down_url);

                                    DownloadManager.Request request = new DownloadManager.Request(uri);
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

                                    request.setTitle("CSV File");
                                    request.setDescription("Downloading");
                                    recvFileList += file_name + ",";
//                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setVisibleInDownloadsUi(false);
                                    request.setDestinationUri(Uri.parse("file://" + CSV_FILE_DIR + file_name));
                                    Log.d("download", file_name);
                                    long downloadID = downloadmanager.enqueue(request);
                                }
                                Log.d("download", recvFileList);
                                new SendFileRecvAck(recvFileList).execute();

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

            if (!(FileTransferReceiver.isRunning())) {
                mServiceIntent = new Intent(getApplicationContext(), FileTransferReceiver.class);
                mServiceIntent.setAction(String.valueOf(Constants.ACTION.STARTFOREGROUND_ACTION));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getApplicationContext().startForegroundService(mServiceIntent);
                } else {
                    getApplicationContext().startService(mServiceIntent);
                }
            }
            return "hello";
        }

    }



    private void doBackgroundWork(final JobParameters params) {
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        unregisterReceiver(receiver);
        myDb.close();
        jobCancelled = true;
        return true;
    }

}