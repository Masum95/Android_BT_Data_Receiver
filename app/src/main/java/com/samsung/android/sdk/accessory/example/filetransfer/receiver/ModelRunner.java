package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.app.Notification;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;

import org.json.JSONObject;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.lang.ref.WeakReference;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MODEL_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MODEL_NAME;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.NotificationHandler.CHANNEL_1_ID;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Utils.getTimeStampFromFile;

class ModelRunner extends AsyncTask<String, Integer, String> {
    DatabaseHelper myDb;
    private WeakReference<Context> contextRef;
    private NotificationManagerCompat notificationManager;

    public ModelRunner(Context context) {
        contextRef = new WeakReference<>(context);
        myDb = new DatabaseHelper(context);
        notificationManager = NotificationManagerCompat.from(context);  // for pushing notification


    }

    @Override
    protected String doInBackground(String... params) {


        String csvFileName = params[0];
        Log.d("tag", "before from python " + csvFileName);
        Python py = Python.getInstance();
        PyObject pyObject = py.getModule("model_runner");


        try {
            PyObject obj = pyObject.callAttr("input_preprocessing", MODEL_FILE_DIR + MODEL_NAME, csvFileName);
            Log.d("tag", "Result from python " + obj.toString());
            String jsonString = obj.toString();
            JSONObject jsonObject = new JSONObject(jsonString);
            String predict_ara = jsonObject.getString("predict_ara");
            String uncertain_score = jsonObject.getString("uncertainity_score");
            String accepted_sig_ratio = jsonObject.getString("accepted_sig_ratio");
            JSONObject hear_rate_data = jsonObject.getJSONObject("hear_rate_data");
            myDb.createResult(csvFileName, getTimeStampFromFile(csvFileName), predict_ara,
                    hear_rate_data.getString("activity"), hear_rate_data.getDouble("hr"),
                    Double.parseDouble(accepted_sig_ratio), uncertain_score
            );
            myDb.updateFileInfo(csvFileName, 1);
            return obj.toString();

        } catch (Exception e) {
            Log.d("tag", String.valueOf(e));
            myDb.createResult(csvFileName, getTimeStampFromFile(csvFileName), "[-1]",
                    "", -1,
                    -1,
                    "[-1]"
            );

            myDb.updateFileInfo(csvFileName, 1);
            return "";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result.isEmpty()) return;
        super.onPostExecute(result);
//        String res = "";
//        String title = "Your Heart Update";
//        Notification notification = new NotificationCompat.Builder(contextRef.get(), CHANNEL_1_ID)
//                .setSmallIcon(R.drawable.ic_medicine)
//                .setContentTitle(title)
//                .setContentText("file(s) recieved")
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
//                .build();
//        notificationManager.notify(2, notification);


    }
}
