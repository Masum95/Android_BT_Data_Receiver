package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.MedicalProfileModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static String assetFilePath(Context context, String assetName) {
        File file = new File(context.getFilesDir(), assetName);

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("pytorchandroid", "Error process asset " + assetName + " to file path");
        }
        return null;
    }

    public static String getDateTimeFromTimestamp(String timestampStr){
        Long timestamp = Long.valueOf(timestampStr).longValue();
        Date date = new Date(timestamp * 1000L);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss:S");
        return simpleDateFormat.format(date);
    }

    public static JSONObject getMedicalProfileJson(Context context){
        final DatabaseHelper myDb = new DatabaseHelper(context);

        String regi_id = myDb.get_profile().getRegi_id();
        MedicalProfileModel profile = myDb.getMedicalProfile(regi_id);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("height", profile != null ?  profile.getHeight(): "");
            jsonObject.put("registration_id", profile != null ?  profile.getRegistration_id(): "");
            jsonObject.put("weight" , profile != null ?  profile.getWeight(): "");
            jsonObject.put("name", profile != null ?  profile.getName(): "");
            jsonObject.put("contact", profile != null ?  profile.getContact(): "");
            jsonObject.put("dob",profile != null ?  profile.getDob(): "");
            jsonObject.put("has_heart_disease",profile != null ?  profile.getHas_heart_disease(): "");
            jsonObject.put("has_parent_heart_disease", profile != null ?  profile.getHas_parent_heart_disease(): "");
            jsonObject.put("has_hyper_tension", profile != null ?  profile.getHas_hyper_tension(): "");
            jsonObject.put("has_covid", profile != null ?  profile.getHas_covid(): "");
            jsonObject.put("has_smoking",  profile != null ?  profile.getHas_smoking(): "");
            jsonObject.put("has_eating_outside", profile != null ?  profile.getHas_eating_outside(): "");
            jsonObject.put("min_hr", profile != null ?  profile.getMin_hr(): 65);
            jsonObject.put("max_hr", profile != null ?  profile.getMax_hr(): 100);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        myDb.close();
        return jsonObject;
    }

    public static String getTimeStampFromFile(String fileName) {
        String[] tmp = fileName.split("/");
        tmp = tmp[tmp.length - 1].split("_|-");
        String timestamp = tmp[tmp.length - 1].split("\\.")[0];
        return timestamp;
    }

    public static boolean haveNetworkConnection(Context ctxt) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) ctxt.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public static String getBdTimeFromUnixTimeStamp(String unixTime){
        long unixSeconds = Long.parseLong(unixTime);

        Date date = new Date(unixSeconds*1000L);
// the format of your date
        SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm");
// give a timezone reference for formatting (see comment at the bottom)
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+6"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB) // API 11
    public static void startMultpleModelRunnerAsyncTaskInParallel(ModelRunner asyncTask, String... params ) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        else
            asyncTask.execute(params);
    }

}
