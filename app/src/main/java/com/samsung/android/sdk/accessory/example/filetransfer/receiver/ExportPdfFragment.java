package com.samsung.android.sdk.accessory.example.filetransfer.receiver;


import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ResultModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
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

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.ACCEPTED_SIG_RATIO_FOR_PDF_PRINT;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.ACCEPTED_SIG_RATIO_FOR_RECORD_LIST;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.CSV_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.FILE_UPLOAD_GET_URL;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MEDICAL_PROFILE_URL;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MODEL_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MODEL_NAME;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.PDF_GENERATE_URL;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.RECORD_FILE_DIR;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.SHARED_PREF_ID;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Utils.formateDateTime;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Utils.getTimeStampFromFile;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Utils.haveNetworkConnection;


public class ExportPdfFragment extends Fragment {


    private TextView fromDateInput, toDateInput;
    private DatePickerDialog.OnDateSetListener fromDateListener, toDateListener;

    Button submitBtn;

    EditText heightText, weightText;
    String dob;
    Context thisContext;
    DownloadManager downloadmanager;
    TextView  nameTextView;

    DatabaseHelper myDb;

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
        myDb = new DatabaseHelper(thisContext);
        String regi_id = "xyz"; //regi_id = myDb.get_profile().getRegi_id();

        downloadmanager = (DownloadManager)  thisContext.getSystemService(Context.DOWNLOAD_SERVICE);


        nameTextView =  getView().findViewById(R.id.profileNameInExport);

        submitBtn = (Button) getView().findViewById(R.id.exportToPdfButton);
        fromDateInput = (TextView) getView().findViewById(R.id.fromDateRecord);
        toDateInput = (TextView) getView().findViewById(R.id.toDateRecord);
        String name = myDb.getMedicalProfile(regi_id).getName();
        nameTextView.setText(name);
        myDb.close();
        new FileUploadToServer(thisContext).execute();

        Log.d("register", "here");

        setRadioTextToButtonMapping();



        fromDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        thisContext,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        fromDateListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        toDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        thisContext,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        toDateListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        fromDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d("tag", "onDateSet: mm/dd/yyy: " + year + "-" + month + "-" + day);

                String date = year + "-" + String.format("%02d",month) + "-" + String.format("%02d", day) ;
                fromDateInput.setText(formateDateTime( day, month, year));
            }
        };
        toDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d("tag", "onDateSet: mm/dd/yyy: " + year + "-" + month + "-" + day);
                String date = year + "-" + String.format("%02d",month) + "-" + String.format("%02d", day) ;
                toDateInput.setText(formateDateTime( day, month, year));
            }
        };



        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if(!haveNetworkConnection(thisContext)){
//                    Toast.makeText(thisContext,"Something Went Wrong.\nPlease Check Your Internet Connection",Toast.LENGTH_LONG).show();
//                }
                new submitValues().execute();//                Toast.makeText(getApplicationContext(), selectedDropDown, Toast.LENGTH_SHORT).show();

                Toast.makeText(thisContext, "Records are being processed.\n You Will be Notified once file is ready", Toast.LENGTH_SHORT).show();
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
            String regi_id = "xyz"; //regi_id = myDb.get_profile().getRegi_id();
            List<ResultModel> resList = new ArrayList<>();


            String recordType = getRadioValue(R.id.recordsTypeRadioGroup);

            String fromDate = fromDateInput.getText().toString();
            String toDate = toDateInput.getText().toString();
            resList = myDb.getResults(-1, ACCEPTED_SIG_RATIO_FOR_PDF_PRINT, fromDate, toDate);
            if(resList.size() == 0 ){
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(thisContext, "No Record", Toast.LENGTH_SHORT).show();
                    }
                });
                return "";
            }
            // python block
            List<String> filesList = new ArrayList<>();
            List<String> predList = new ArrayList<>();
            List<String> uncertainList = new ArrayList<>();
            List<String> timestampList = new ArrayList<>();
            for(ResultModel res: resList){
                filesList.add(res.getFileName());
                predList.add(res.getResult());
                timestampList.add(res.getTimestamp());
                uncertainList.add(res.getUncertainity_score());
            }
            myDb.close();

            Python py = Python.getInstance();
            PyObject pyObject = py.getModule("model_runner");

            Log.d("tag", "before  from python "+ filesList.toString() );
            Log.d("tag", "before  from python "+ predList.toString() );
            Log.d("tag", "before  from python "+ timestampList.toString() );
            Log.d("tag", String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
            String output_file = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)) + "/record.pdf";
            try {
                PyObject obj = pyObject.callAttr("pdf_preprocessing", output_file, filesList.toString(), predList.toString(), uncertainList.toString(), timestampList.toString());
                String jsonString = obj.toString();
                Log.d("tag", "Result from python "+ jsonString );


                File file = new File(jsonString);
                Uri uri = FileProvider.getUriForFile(getContext(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        file);


                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);

            } catch (Exception e) {
                Log.d("tag", String.valueOf(e));

                return "";
            }
            // ---------

//
//            Uri builtUri = Uri.parse(PDF_GENERATE_URL)
//                    .buildUpon()
//                    .appendQueryParameter("type", recordType)
//                    .appendQueryParameter("start_time", fromDate)
//                    .appendQueryParameter("end_time", toDate)
//                    .appendQueryParameter("registration_id", regi_id)
//                    .build();
//
//
//
//            DownloadManager.Request request = new DownloadManager.Request(builtUri);
//            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "record.pdf");
//
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//
//            request.setTitle("Record File");
//            request.setDescription("Downloading");
//            request.setVisibleInDownloadsUi(true);
////            request.setDestinationUri(Uri.parse("file://" + RECORD_FILE_DIR +"record.pdf" ));
//            Log.d("downloading", String.valueOf(request));
//            downloadmanager.enqueue(request);


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