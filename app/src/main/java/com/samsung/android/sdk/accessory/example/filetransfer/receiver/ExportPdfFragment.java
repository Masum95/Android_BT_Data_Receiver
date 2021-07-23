package com.samsung.android.sdk.accessory.example.filetransfer.receiver;


import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.CSV_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.FILE_UPLOAD_GET_URL;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MEDICAL_PROFILE_URL;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.PDF_GENERATE_URL;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.RECORD_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.SHARED_PREF_ID;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Utils.haveNetworkConnection;


public class ExportPdfFragment extends Fragment {


    private TextView fromDateInput, toDateInput;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    Button submitBtn;

    EditText heightText, weightText;
    String dob;
    Context thisContext;
    DownloadManager downloadmanager;


    Dictionary<String, Integer> radioTextToButtonMapping = new Hashtable();

    private void setRadioTextToButtonMapping(){
        radioTextToButtonMapping.put("recordsTypeRadioGroup", R.id.recordsTypeRadioGroup);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        thisContext = container.getContext();
        return inflater.inflate(R.layout.export_toolkit, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        downloadmanager = (DownloadManager)  thisContext.getSystemService(Context.DOWNLOAD_SERVICE);



        submitBtn = (Button) getView().findViewById(R.id.exportToPdfButton);
        fromDateInput = (TextView) getView().findViewById(R.id.fromDateRecord);
        toDateInput = (TextView) getView().findViewById(R.id.toDateRecord);

        new FileUploadToServer(thisContext).execute();

        Log.d("register", "here");

        setRadioTextToButtonMapping();


        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!haveNetworkConnection(thisContext)){
                    Toast.makeText(thisContext,"Something Went Wrong.\nPlease Check Your Internet Connection",Toast.LENGTH_LONG).show();
                }
                new submitValues().execute();//                Toast.makeText(getApplicationContext(), selectedDropDown, Toast.LENGTH_SHORT).show();

                Toast.makeText(thisContext, "Record is being Downloded.\n You Will be Notified once downloaed", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private String getRadioValue(int radioId) {
        RadioGroup rg = (RadioGroup) getView().findViewById(radioId);

        int selectedId = rg.getCheckedRadioButtonId();
        if(selectedId == -1) {
            return "";
        }
        RadioButton radioButton;
        radioButton = (RadioButton) getView().findViewById(selectedId);
        return radioButton.getText().toString();
    }


    private class submitValues extends AsyncTask<String, Integer, String> {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected String doInBackground(String... params) {
            final DatabaseHelper myDb = new DatabaseHelper(thisContext);
            String regi_id = myDb.get_profile().getRegi_id();
            myDb.close();
            String recordType = getRadioValue(R.id.recordsTypeRadioGroup);

            String fromDate = fromDateInput.getText().toString();
            String toDate = toDateInput.getText().toString();

            Uri builtUri = Uri.parse(PDF_GENERATE_URL)
                    .buildUpon()
                    .appendQueryParameter("type", recordType)
                    .appendQueryParameter("start_time", fromDate)
                    .appendQueryParameter("end_time", toDate)
                    .appendQueryParameter("registration_id", regi_id)
                    .build();



            DownloadManager.Request request = new DownloadManager.Request(builtUri);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "record.pdf");

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            request.setTitle("Record File");
            request.setDescription("Downloading");
            request.setVisibleInDownloadsUi(true);
//            request.setDestinationUri(Uri.parse("file://" + RECORD_FILE_DIR +"record.pdf" ));
            Log.d("downloading", String.valueOf(request));
            downloadmanager.enqueue(request);


//            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                    new FileTransferReceiverFragment()).commit();
            return "hello";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }


}