package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.jacksonandroidnetworking.JacksonParserFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.NotificationHandler.CHANNEL_1_ID;

public class FileJobService extends JobService {
    private static final String TAG = "ExampleJobService";
    private boolean jobCancelled = false;

    public static String PACKAGE_NAME;

    final static String pkgFolderName = "BayesBeat/";
    final static String csvFileDir = Environment.getExternalStorageDirectory() + File.separator + pkgFolderName + "csvFiles/";
    final static String modelFileDir = Environment.getExternalStorageDirectory() + File.separator + pkgFolderName + "model/";
    DownloadManager downloadmanager;
    private static final String DEST_DIRECTORY = csvFileDir;
    BroadcastReceiver receiver;
    final static String modelName = "bayesbeat_cpu.pt";
    private Context mCtxt;
    private NotificationManagerCompat notificationManager;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");
        AndroidNetworking.initialize(getApplicationContext());  // for api request
        AndroidNetworking.setParserFactory(new JacksonParserFactory());
        notificationManager = NotificationManagerCompat.from(this);  // for pushing notification

        mCtxt = getApplicationContext();
        downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d("tag" , "here    ---");
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
//                            boolean isInserted = myDb.insertData(filePath,
//                                    "server",
//                                    1,
//                                    1);
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
        new Downloader().execute();
//        doBackgroundWork(params);

        return true;
    }

    private class ModelRunner extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String csvFileName = params[0];
            Log.d("tag", "before from python " + csvFileName);

            Python py = Python.getInstance();
            PyObject pyObject = py.getModule("model_runner");
            try {
                PyObject obj = pyObject.callAttr("input_preprocessing", modelFileDir + modelName, csvFileName);
                Log.d("tag", "Result from python " + obj.toString());
                return obj.toString();

            } catch (Exception e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.isEmpty()) return;
            super.onPostExecute(result);
            String title = "Your Heart Update";
            Notification notification = new NotificationCompat.Builder(mCtxt, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_medicine)
                    .setContentTitle(title)
                    .setContentText(result)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build();
            notificationManager.notify(1, notification);

        }
    }



    private class Downloader extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

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
            return "hello";
        }

    }

    private void doBackgroundWork(final JobParameters params) {}

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        unregisterReceiver(receiver);

        jobCancelled = true;
        return true;
    }
}