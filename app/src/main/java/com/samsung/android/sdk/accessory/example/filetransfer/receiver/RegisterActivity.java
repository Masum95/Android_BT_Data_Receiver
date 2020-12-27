package com.samsung.android.sdk.accessory.example.filetransfer.receiver;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {

    EditText name, device_id, email, phone, password;
    Button register;
    TextView login;
    boolean isNameValid, isDevice_id_Valid, isEmailValid, isPhoneValid, isPasswordValid;
    TextInputLayout nameError, device_id_error, emailError, phoneError, passError;
    String sharedPrefId = "FileXferAppPreference";
    SharedPreferences prefs;

    private void skipActivity(){
        prefs = getSharedPreferences(sharedPrefId, 0);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            Intent intent = new Intent(getApplicationContext(), FileTransferReceiverActivity.class);
            startActivity(intent);
        } else {
            final DatabaseHelper myDb = new DatabaseHelper(getApplicationContext());

            name = (EditText) findViewById(R.id.name);
            device_id = (EditText) findViewById(R.id.device_id);

//        email = (EditText) findViewById(R.id.email);
//        phone = (EditText) findViewById(R.id.phone);
//        password = (EditText) findViewById(R.id.password);
//        login = (TextView) findViewById(R.id.login);
            register = (Button) findViewById(R.id.register);
            nameError = (TextInputLayout) findViewById(R.id.nameError);
            device_id_error = (TextInputLayout) findViewById(R.id.device_id_error);
//        emailError = (TextInputLayout) findViewById(R.id.emailError);
//        phoneError = (TextInputLayout) findViewById(R.id.phoneError);
//        passError = (TextInputLayout) findViewById(R.id.passError);

            do_permissions_stuffs();
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (is_all_valid()) {
                        // redirect to LoginActivity

                        prefs.edit().putBoolean("isLoggedIn", true).apply();

                        myDb.createProfile(name.getText().toString().trim(), device_id.getText().toString().trim());
                        Intent intent = new Intent(getApplicationContext(), FileTransferReceiverActivity.class);
                        startActivity(intent);
                    }

                }
            });
        }
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


    public boolean is_all_valid() {
        // Check for a valid name.
        if (name.getText().toString().isEmpty()) {
            nameError.setError(getResources().getString(R.string.name_error));
            isNameValid = false;
        } else {
            isNameValid = true;
            nameError.setErrorEnabled(false);
        }

        if (device_id.getText().toString().isEmpty()) {
            device_id_error.setError(getResources().getString(R.string.device_id_error));
            isDevice_id_Valid = false;
        } else {
            isDevice_id_Valid = true;
            device_id_error.setErrorEnabled(false);
        }

//        // Check for a valid email address.
//        if (email.getText().toString().isEmpty()) {
//            emailError.setError(getResources().getString(R.string.email_error));
//            isEmailValid = false;
//        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
//            emailError.setError(getResources().getString(R.string.error_invalid_email));
//            isEmailValid = false;
//        } else  {
//            isEmailValid = true;
//            emailError.setErrorEnabled(false);
//        }
//
//        // Check for a valid phone number.
//        if (phone.getText().toString().isEmpty()) {
//            phoneError.setError(getResources().getString(R.string.phone_error));
//            isPhoneValid = false;
//        } else  {
//            isPhoneValid = true;
//            phoneError.setErrorEnabled(false);
//        }
//
//        // Check for a valid password.
//        if (password.getText().toString().isEmpty()) {
//            passError.setError(getResources().getString(R.string.password_error));
//            isPasswordValid = false;
//        } else if (password.getText().length() < 6) {
//            passError.setError(getResources().getString(R.string.error_invalid_password));
//            isPasswordValid = false;
//        } else  {
//            isPasswordValid = true;
//            passError.setErrorEnabled(false);
//        }

        if (isNameValid && isDevice_id_Valid) {
            return true;
        }

//        if (isNameValid && isEmailValid && isPhoneValid && isPasswordValid) {
//            Toast.makeText(getApplicationContext(), "Successfully", Toast.LENGTH_SHORT).show();
//        }

        return false;
    }

}