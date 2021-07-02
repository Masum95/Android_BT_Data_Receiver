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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
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

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MEDICAL_PROFILE_URL;


public class MedicalProfileRegisterFragment2 extends Fragment {


    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    String[] bloodArray;

    Button nextBtn;
    Spinner spinner;

    EditText heightText, weightText;
    String dob;
    Context thisContext;


    Dictionary<String, Integer> radioTextToButtonMapping = new Hashtable();

    private void setRadioTextToButtonMapping(){
        radioTextToButtonMapping.put("has_heart_disease", R.id.radioGroupHD);
        radioTextToButtonMapping.put("has_parent_heart_disease", R.id.radioGroupPHD);
        radioTextToButtonMapping.put("has_hyper_tension", R.id.radioGroupHT);
        radioTextToButtonMapping.put("has_covid", R.id.radioGroupCovid);
        radioTextToButtonMapping.put("has_smoking", R.id.radioGroupSmoking);
        radioTextToButtonMapping.put("has_eating_outside", R.id.radioGroupEatOutside);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        thisContext = container.getContext();
        return inflater.inflate(R.layout.activity_profile_register2, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nextBtn = (Button) getView().findViewById(R.id.bNext);

        heightText = (EditText) getView().findViewById(R.id.height);
        weightText = (EditText) getView().findViewById(R.id.weight);

        bloodArray = getResources().getStringArray(R.array.blood_groups);
        Log.d("register", "here");

        setRadioTextToButtonMapping();



        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new submitValues().execute();//                Toast.makeText(getApplicationContext(), selectedDropDown, Toast.LENGTH_SHORT).show();

//                Toast.makeText(getApplicationContext(), selectedRadio, Toast.LENGTH_SHORT).show();
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
    private void setRadioValue(String key, String value){
        int grpId = radioTextToButtonMapping.get(key);
        int buttonIndx = getRadioButtonIndexByText(value);


        RadioGroup rg;
        rg = (RadioGroup) getView().findViewById(grpId);
        rg.check(rg.getChildAt(buttonIndx).getId());
    }

    private int getRadioButtonIndexByText(String str){
        switch (str){
            case "True":
                return 0;
            case "False":
                return 1;
        }
        return 0;
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
                    }else{

                        String jsonData = response.body().string();
                        try {
                            JSONObject Jobject = new JSONObject(jsonData);


                            setRadioValue("has_heart_disease", Jobject.getString("has_heart_disease"));
                            setRadioValue("has_parent_heart_disease", Jobject.getString("has_parent_heart_disease"));
                            setRadioValue("has_hyper_tension", Jobject.getString("has_hyper_tension"));
                            setRadioValue("has_covid", Jobject.getString("has_covid"));
                            setRadioValue("has_smoking", Jobject.getString("has_smoking"));
                            setRadioValue("has_eating_outside", Jobject.getString("has_eating_outside"));

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
            String HD = getRadioValue(R.id.radioGroupHD);
            String parent_HD = getRadioValue(R.id.radioGroupPHD);
            String hyperTension = getRadioValue(R.id.radioGroupHT);
            String covid = getRadioValue(R.id.radioGroupCovid);
            String smoking = getRadioValue(R.id.radioGroupSmoking);
            String eatingOutside = getRadioValue(R.id.radioGroupEatOutside);


            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("has_heart_disease", HD);
                jsonObject.put("registration_id", regi_id);
                jsonObject.put("has_parent_heart_disease", parent_HD);
                jsonObject.put("has_hyper_tension", hyperTension);
                jsonObject.put("has_covid", covid);
                jsonObject.put("has_smoking",  smoking);
                jsonObject.put("has_eating_outside", eatingOutside);

            } catch (JSONException e) {
                e.printStackTrace();
            }
//            Log.d("tag=======", jsonObject.toString());
//            Log.d("tag=======", myDb.get_profile().toString());
//            Log.d("tag=======", regi_id);



            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody body = RequestBody.create( jsonObject.toString(), JSON); // new
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
                    }else{
                        Log.d("sending", " successful");
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new FileTransferReceiverFragment()).commit();
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