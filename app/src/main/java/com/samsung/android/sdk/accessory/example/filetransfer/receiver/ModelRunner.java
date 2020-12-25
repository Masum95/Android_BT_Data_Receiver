//package com.samsung.android.sdk.accessory.example.filetransfer.receiver;
//
//import android.app.Notification;
//import android.os.AsyncTask;
//import android.support.v4.app.NotificationCompat;
//import android.util.Log;
//
//import com.chaquo.python.PyObject;
//import com.chaquo.python.Python;
//
//import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MODEL_FILE_DIR;
//import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MODEL_NAME;
//import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.NotificationHandler.CHANNEL_1_ID;
//
//class ModelRunner extends AsyncTask<String, Integer, String> {
//
//    @Override
//    protected String doInBackground(String... params) {
//        String csvFileName = params[0];
//        Log.d("tag", "before from python " + csvFileName);
//
//        Python py = Python.getInstance();
//        PyObject pyObject = py.getModule("model_runner");
//        try {
//            PyObject obj = pyObject.callAttr("input_preprocessing", MODEL_FILE_DIR + MODEL_NAME, csvFileName);
//            Log.d("tag", "Result from python " + obj.toString());
////            resultList.add(convertStringToIntAra(obj.toString()));
//            return obj.toString();
//
//        } catch (Exception e) {
//            return "";
//        }
//    }
//
//    @Override
//    protected void onPostExecute(String result) {
//        if (result.isEmpty()) return;
//        super.onPostExecute(result);
////        download_count--;
//        String res = "";
////        if(download_count == 0 )
////        {
////            String title = "Your Heart Update";
////            Notification notification = new NotificationCompat.Builder(mCtxt, CHANNEL_1_ID)
////                    .setSmallIcon(R.drawable.ic_medicine)
////                    .setContentTitle(title)
////                    .setContentText(resultList.toString())
////                    .setPriority(NotificationCompat.PRIORITY_HIGH)
////                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
////                    .build();
////            notificationManager.notify(2, notification);
////        }
//
//
//    }
//}
