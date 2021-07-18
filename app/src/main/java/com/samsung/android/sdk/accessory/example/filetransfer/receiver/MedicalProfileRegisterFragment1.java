package com.samsung.android.sdk.accessory.example.filetransfer.receiver;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.MedicalProfileModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MEDICAL_PROFILE_URL;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.SHARED_PREF_ID;


public class MedicalProfileRegisterFragment1 extends Fragment {


    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    String[] bloodArray;
    CheckBox ch, ch1, ch2, ch3;
    RadioGroup rg;
    Button nextBtn;
    Spinner spinner;

    EditText nameInput, heightInput, weightInput, contactInput;
    String dob;
    Context thisContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        thisContext = container.getContext();
        return inflater.inflate(R.layout.activity_profile_register1, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        nextBtn = (Button) getView().findViewById(R.id.nextButton);

        nameInput = (EditText) getView().findViewById(R.id.nameTextInput);
        heightInput = (EditText) getView().findViewById(R.id.heightNumberDecimalInput);
        weightInput = (EditText) getView().findViewById(R.id.weightNumberDecimalInput);
        contactInput = (EditText) getView().findViewById(R.id.emergencyContactNumberInput);


        mDisplayDate = (TextView) getView().findViewById(R.id.dobDateInput);

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        thisContext,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d("tag", "onDateSet: mm/dd/yyy: " + year + "-" + month + "-" + day);

                String date = year + "-" + month + "-" + day;
                mDisplayDate.setText(date);
            }
        };

        new loadValues().execute();

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new submitValues().execute();//                Toast.makeText(getApplicationContext(), selectedDropDown, Toast.LENGTH_SHORT).show();

//                Toast.makeText(getApplicationContext(), selectedRadio, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getValidValue(String value) {
        if (value != "null") {
            return value;
        }
        return "";
    }


    void UpdateOnUi(JSONObject obj) {
        class OneShotTask implements Runnable {
            JSONObject Jobject;

            OneShotTask(JSONObject s) {
                Jobject = s;
            }

            public void run() {
                try {
                    heightInput.setText(getValidValue(Jobject.getString("height")));
                    weightInput.setText(getValidValue(Jobject.getString("weight")));
                    nameInput.setText(getValidValue(Jobject.getString("name")));
                    contactInput.setText(getValidValue(Jobject.getString("contact")));
                    mDisplayDate.setText(getValidValue(Jobject.getString("dob")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        getActivity().runOnUiThread(new OneShotTask(obj));
    }

    private class loadValues extends AsyncTask<String, Integer, String> {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... params) {
            final DatabaseHelper myDb = new DatabaseHelper(thisContext);
            String regi_id = "xyz";// myDb.get_profile().getRegi_id();
            myDb.close();
            JSONObject jsonObject = Utils.getMedicalProfileJson(getContext());
            UpdateOnUi(jsonObject);

//            HttpUrl.Builder urlBuilder = HttpUrl.parse(MEDICAL_PROFILE_URL).newBuilder();
//            urlBuilder.addQueryParameter("registration_id", regi_id);
//            String url = urlBuilder.build().toString();
//
//
//            Request request = new Request.Builder().url(url)// The URL to send the data to
//                    .build();

//            client.newCall(request).enqueue(new Callback() {
//
//                @Override
//                public void onFailure(final Call call, final IOException e) {
//                    // Handle the error
//                    Log.d("sending", String.valueOf(e));
//
//                }
//
//                @Override
//                public void onResponse(final Call call, final Response response) throws IOException {
//                    if (!response.isSuccessful()) {
//                        // Handle the error
//                        Log.d("sending", "un successful");
//                    } else {
//
//                        Log.d("sending", String.valueOf(response));
//
//                    }
//
//
//                    // Upload successful
//                }
//            });
            return "hello";
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }


    private class submitValues extends AsyncTask<String, Integer, String> {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected String doInBackground(String... params) {
            final DatabaseHelper myDb = new DatabaseHelper(thisContext);
            String regi_id = "xyz";// myDb.get_profile().getRegi_id();
            myDb.close();
            OkHttpClient client = new OkHttpClient();

//                jsonObject.put("registration_id", "3d2594ec-7c88-4f9c-9c2c-3e4ddf9891be");

            String dob = mDisplayDate.getText().toString();
            String height = heightInput.getText().toString();
            String weight = weightInput.getText().toString();
            String name = nameInput.getText().toString();
            String contact = contactInput.getText().toString();

            HashMap<String, String> map = new HashMap<>();
            map.put("dob", dob);
            map.put("height", height);
            map.put("weight", weight);
            map.put("name", name);
            map.put("contact", contact);

            myDb.createOrUpdateMedicalProfile(regi_id, map);
            myDb.close();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MedicalProfileRegisterFragment2()).commit();
            return "hello";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }


}