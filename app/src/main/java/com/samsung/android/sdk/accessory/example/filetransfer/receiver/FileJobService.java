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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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
import okhttp3.FormBody;
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
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.WATCH_SRC_KEYWORD;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.NotificationHandler.CHANNEL_1_ID;

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

    private static String getTimeStampFromFile(String fileName){

        String[] tmp = fileName.split("/");
        tmp =  tmp[tmp.length-1].split("_");
        String timestamp =  tmp[tmp.length-1].split("\\.")[0];
        return timestamp;
    }

    private static List<Integer> convertStringToIntAra(String str){
        String[] result = str.split("[ ,\\]\\[]");
        List<Integer> list=new ArrayList<Integer>();

        for(String res: result){
            try{
                list.add(Integer.parseInt(res));
            }catch(NumberFormatException e){}
        }
        return list;
    }

    String device_id;
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");
        AndroidNetworking.initialize(getApplicationContext());  // for api request
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

        notificationManager = NotificationManagerCompat.from(this);  // for pushing notification
        final DatabaseHelper myDb = new DatabaseHelper(this);
        device_id = myDb.get_profile().getDevice_id();

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
//                            Log.d("file_rcvd", filePath);

                            filePath = (filePath.split(":",2)[1]).substring(2);
//                            Log.d("filepath", filePath);
                            Log.d("file_rcvd", filePath);
                            boolean isInserted = myDb.insertFileInfo(filePath,
                                    SERVER_SRC_KEYWORD, // SERVER_SRC_KEYWORD
                                    1,
                                    1);
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
        new Uploader().execute();
//
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
                PyObject obj = pyObject.callAttr("input_preprocessing", MODEL_FILE_DIR + MODEL_NAME, csvFileName);
                Log.d("tag", "Result from python " + obj.toString());
                resultList.add(convertStringToIntAra(obj.toString()));
                return obj.toString();

            } catch (Exception e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.isEmpty()) return;
            super.onPostExecute(result);
            download_count--;
            String res = "";
            if(download_count == 0 ){
                String title = "Your Heart Update";
                Notification notification = new NotificationCompat.Builder(mCtxt, CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_medicine)
                        .setContentTitle(title)
                        .setContentText(resultList.toString())
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .build();
                notificationManager.notify(2, notification);
            }


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

            RequestBody body = RequestBody.create( jsonObject.toString(), JSON); // new
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
                        .addQueryParameter("device_id", device_id)
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
                                for (int i = 0 ; i < ara.length(); i++) {
                                    JSONObject obj = ara.getJSONObject(i);
                                    String down_url = String.valueOf(obj.getString("file"));
                                    String file_name = String.valueOf(obj.getString("file_name"));
                                    Log.d("json_response", down_url);
                                    Uri uri = Uri.parse(down_url);

                                    DownloadManager.Request request = new DownloadManager.Request(uri);
                                    request.setTitle("CSV File");
                                    request.setDescription("Downloading");
                                    recvFileList += file_name + ",";
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setVisibleInDownloadsUi(false);
                                    request.setDestinationUri(Uri.parse("file://" + CSV_FILE_DIR + file_name));
                                    Log.d("download", file_name);
                                    downloadmanager.enqueue(request);
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

            if(!(FileTransferReceiver.isRunning())){
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


    private class Uploader extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {


            final DatabaseHelper myDb = new DatabaseHelper(getApplicationContext());

            Log.d("sending", "start here ");

            List<FileModel> filesList  = myDb.getUnuploadedFilePaths();
            Log.d("sending", String.valueOf(filesList.size()));
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(1);

            OkHttpClient client = new OkHttpClient.Builder()
                    .dispatcher(dispatcher)
                    .build();

            int indx = 0;
//            while(true){
            for(FileModel file_details: filesList){
                try {
                    final String path = file_details.getFileName();
                    Log.d("sending",  path);

                    File file = new File(path);
                    Log.d("sending", "in try ");

                    RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.getName(),
                                    RequestBody.create(file, MediaType.parse("text/csv") ))
                            .addFormDataPart("device_id",  device_id)
                            .addFormDataPart("timestamp", getTimeStampFromFile(path))
                            .addFormDataPart("file_src", "MOBILE")

                            .build();

                    Request request = new Request.Builder()
                            .url(FILE_UPLOAD_GET_URL)
                            .post(requestBody)
                            .build();

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
                            }else{

                                Log.d("sending", "successful -------->" +  path);
                                myDb.updateFileSendStatus(path);
                            }


                            // Upload successful
                        }
                    });

                } catch (Exception ex) {
                    // Handle the error
                    Log.d("sending", " no file ");
                }
            }

            return "ok";

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