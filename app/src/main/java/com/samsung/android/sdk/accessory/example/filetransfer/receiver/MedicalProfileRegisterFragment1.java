package com.samsung.android.sdk.accessory.example.filetransfer.receiver;


import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MEDICAL_PROFILE_URL;


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

    private String getValidValue(String value){
        if(value!= "null"){
            return value;
        }
        return "";
    }

    private void setFieldsFromJson(Response response) throws IOException, JSONException {
        String jsonData = response.body().string();
        JSONObject json = new JSONObject(jsonData);

        JSONArray jsonarray = new JSONArray(json.getString("data"));
        if(jsonarray.length() > 0) {
            JSONObject Jobject = (JSONObject) jsonarray.getJSONObject(0);

            heightInput.setText(getValidValue(Jobject.getString("height")));
            weightInput.setText(getValidValue(Jobject.getString("weight")));
            nameInput.setText(getValidValue(Jobject.getString("name")));
            contactInput.setText(getValidValue(Jobject.getString("contact")));
            mDisplayDate.setText(getValidValue(Jobject.getString("dob")));
        }
    }


    private class loadValues extends AsyncTask<String, Integer, String> {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... params) {
            final DatabaseHelper myDb = new DatabaseHelper(thisContext);
            String regi_id = myDb.get_profile().getRegi_id();


            HttpUrl.Builder urlBuilder = HttpUrl.parse(MEDICAL_PROFILE_URL).newBuilder();
            urlBuilder.addQueryParameter("registration_id", regi_id);
            String url = urlBuilder.build().toString();


            Request request = new Request.Builder().url(url)// The URL to send the data to
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

                        try {
                            setFieldsFromJson(response);
                        } catch (JSONException e) {
                            e.printStackTrace();


                        }
                        Log.d("sending", String.valueOf(response));

                    }


                    // Upload successful
                }
            });
            return "hello";
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }


    private class submitValues extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            final DatabaseHelper myDb = new DatabaseHelper(thisContext);
            String regi_id = myDb.get_profile().getRegi_id();
            OkHttpClient client = new OkHttpClient();

//                jsonObject.put("registration_id", "3d2594ec-7c88-4f9c-9c2c-3e4ddf9891be");

            String dob = mDisplayDate.getText().toString();
            String height = heightInput.getText().toString();
            String weight = weightInput.getText().toString();
            String name = nameInput.getText().toString();
            String contact = contactInput.getText().toString();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("height", height);
                jsonObject.put("registration_id", regi_id);
                jsonObject.put("weight", weight);
                jsonObject.put("name", name);
                jsonObject.put("contact", contact);
                jsonObject.put("dob", dob);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody body = RequestBody.create(jsonObject.toString(), JSON); // new
            Log.d("tag=======", String.valueOf(body));

            Request request = new Request.Builder().url(MEDICAL_PROFILE_URL) // The URL to send the data to
                    .post(body)
                    .build();
            Log.d("tag=======", String.valueOf(request));


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
                        Log.d("sending", " successful");
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new MedicalProfileRegisterFragment2()).commit();
                    }


                    // Upload successful
                }
            });
            return "hello";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }


}