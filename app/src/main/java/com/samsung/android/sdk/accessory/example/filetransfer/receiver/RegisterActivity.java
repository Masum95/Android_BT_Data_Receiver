package com.samsung.android.sdk.accessory.example.filetransfer.receiver;


import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Patterns;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.CSV_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.FILE_UPLOAD_GET_URL;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.WATCH_ID_URL;

public class RegisterActivity extends AppCompatActivity {

    EditText name, phone_num, email, phone, password;
    Button register;
    TextView login;
    boolean isNameValid, isEmailValid, isPasswordValid;
    TextInputLayout nameError, phone_num_error, emailError, phoneError, passError;
    String sharedPrefId = "FileXferAppPreference";
    SharedPreferences prefs;
    boolean isCheckingFinished = false;
    boolean isPhoneValid = false;

    private void skipActivity(){
        prefs = getSharedPreferences(sharedPrefId, 0);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else{
            final DatabaseHelper myDb = new DatabaseHelper(getApplicationContext());

            name = (EditText) findViewById(R.id.name);
            phone_num = (EditText) findViewById(R.id.phone_num);

            register = (Button) findViewById(R.id.register);
            nameError = (TextInputLayout) findViewById(R.id.nameError);
            phone_num_error = (TextInputLayout) findViewById(R.id.phone_num_error);


            do_permissions_stuffs();
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (is_client_side_valid()) {
                        // redirect to LoginActivity

                        new serverValidation().execute();

                    }

                }
            });
        }
    }

    private class serverValidation extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {


            final DatabaseHelper myDb = new DatabaseHelper(getApplicationContext());


            AndroidNetworking.get(WATCH_ID_URL)
                    .addQueryParameter("phone_num", phone_num.getText().toString())
                    .addQueryParameter("user_name",  name.getText().toString().trim())
                    .setPriority(Priority.LOW)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // do anything with response
//                            Toast.makeText(getApplicationContext(), String.valueOf(response) , Toast.LENGTH_LONG).show();

                            String status = null;
                            try {
                                status = response.getString("status");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
//                            Toast.makeText(getApplicationContext(), status , Toast.LENGTH_LONG).show();
                            Log.d("json_response", status);

                            String device_id, regi_id;
                            if(!status.equalsIgnoreCase("failure")){
                                try {
                                    device_id = response.getString("device_id");
                                    regi_id = response.getString("registration_id");
                                    Toast.makeText(getApplicationContext(), device_id , Toast.LENGTH_LONG).show();
                                    Log.d("json_response", String.valueOf(response));
                                    prefs.edit().putBoolean("isLoggedIn", true).apply();

                                    myDb.createProfile(name.getText().toString().trim(), phone_num.getText().toString(), device_id, regi_id);
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }

    public boolean is_client_side_valid() {
        // Check for a valid name.

        if (name.getText().toString().isEmpty()) {
            nameError.setError(getResources().getString(R.string.name_error));
            isNameValid = false;
        } else {
            isNameValid = true;
            nameError.setErrorEnabled(false);
        }

        if (phone_num.getText().toString().isEmpty()  ) {
            phone_num_error.setError(getResources().getString(R.string.phone_num_error));
            isPhoneValid = false;
        } else {
            isPhoneValid = true;
            phone_num_error.setErrorEnabled(false);
        }


        if (isNameValid && isPhoneValid) {
            return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.d("register","here");
        // Retrieving your app specific preference
        skipActivity();



    }

    @Override
    protected void onStart() {
        super.onStart();
        skipActivity();
    }


    private void do_permissions_stuffs() {
        if (!hasPermissions(this, PERMISSIONS)) {
            requestStoragePermission();
        } else {
            Log.d("permisionn  ", "----------ase already");
//            new FileTransferReceiverActivity.StarterTask().execute();

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }


    private boolean hasStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return true;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
//            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            android.Manifest.permission.READ_SMS,
    };


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void requestStoragePermission() {

        if (hasPermissions(this, PERMISSIONS)) {
            return;
        }
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 1:
                boolean isPerpermissionForAllGranted = false;
                if (grantResults.length > 0 && permissions.length == grantResults.length) {
                    for (int i = 0; i < permissions.length; i++) {
                        Log.d("tag-->", String.valueOf(permissions[i]) + " " + String.valueOf(grantResults[i]) + " " + PackageManager.PERMISSION_GRANTED);

                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            isPerpermissionForAllGranted = true;
                        } else {
                            isPerpermissionForAllGranted = false;
                        }
                    }

                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    isPerpermissionForAllGranted = true;
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                if (isPerpermissionForAllGranted) {
                    Log.d("tag", "permission granted ");
//                    new FileTransferReceiverActivity.StarterTask().execute("my string parameter");
                }
                break;
        }
    }



}