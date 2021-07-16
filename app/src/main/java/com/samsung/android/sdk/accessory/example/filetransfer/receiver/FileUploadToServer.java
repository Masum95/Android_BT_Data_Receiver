package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.FileModel;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
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

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.FILE_UPLOAD_GET_URL;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Utils.getTimeStampFromFile;


public class FileUploadToServer extends AsyncTask<String, Integer, String> {
    DatabaseHelper myDb;
    private WeakReference<Context> contextRef;
    private NotificationManagerCompat notificationManager;

    public FileUploadToServer(Context context) {
        contextRef = new WeakReference<>(context);
        myDb = new DatabaseHelper(context);
        notificationManager = NotificationManagerCompat.from(context);  // for pushing notification


    }

    @Override
    protected String doInBackground(String... params) {



        Log.d("sending", "start here ");

        List<FileModel> filesList = myDb.getUnuploadedFilePaths();
        Log.d("sending", String.valueOf(filesList.size()));
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(1);

        OkHttpClient client = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .build();
        String device_id = myDb.get_profile().getDevice_id();
        int indx = 0;
//            while(true){
        for (FileModel file_details : filesList) {
            try {
                final String path = file_details.getFileName();
                Log.d("sending", path);

                File file = new File(path);
                Log.d("sending", "in try ");

                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.getName(),
                                RequestBody.create(file, MediaType.parse("text/csv")))
                        .addFormDataPart("device_id", device_id)
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
                        } else {

                            Log.d("sending", "successful -------->" + path);
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

