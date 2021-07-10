package com.samsung.android.sdk.accessory.example.filetransfer.receiver;


import android.app.DatePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

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

    Button submitBtn;
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
        submitBtn = (Button) getView().findViewById(R.id.buttonSubmit);

        heightText = (EditText) getView().findViewById(R.id.height);
        weightText = (EditText) getView().findViewById(R.id.weight);

        Log.d("register", "here");

        setRadioTextToButtonMapping();

        new loadValues().execute();

        submitBtn.setOnClickListener(new View.OnClickListener() {
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

        int buttonIndx = -1;
        switch (key){
            case "has_eating_outside":
                buttonIndx = getEatingOutsideRadioButtonIndexByText(value);
                break;
            default:
                buttonIndx = getBinaryRadioButtonIndexByText(value);
                break;
        }
        if(buttonIndx == -1) return;
        Log.d("setting", key +"    "+ buttonIndx);
        RadioGroup rg;
        rg = (RadioGroup) getView().findViewById(grpId);
        rg.check(rg.getChildAt(buttonIndx).getId());
    }

    private int getBinaryRadioButtonIndexByText(String str){

        switch (str){
            case "Yes":
                return 0;
            case "No":
                return 1;
        }
        return -1;
    }

    private int getEatingOutsideRadioButtonIndexByText(String str){

        switch (str){
            case "Frequently":
                return 0;
            case "Sometimes":
                return 1;
            case "Never":
                return 2;
        }
        return -1;
    }

    private class loadValues extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            final DatabaseHelper myDb = new DatabaseHelper(thisContext);
            String regi_id = myDb.get_profile().getRegi_id();

            JSONObject Jobject = Utils.getMedicalProfileJson(getContext());

            try {
                setRadioValue("has_heart_disease", Jobject.getString("has_heart_disease"));
                setRadioValue("has_parent_heart_disease", Jobject.getString("has_parent_heart_disease"));
                setRadioValue("has_hyper_tension", Jobject.getString("has_hyper_tension"));
                setRadioValue("has_covid", Jobject.getString("has_covid"));
                setRadioValue("has_smoking", Jobject.getString("has_smoking"));
                setRadioValue("has_eating_outside", Jobject.getString("has_eating_outside"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

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
            String regi_id = myDb.get_profile().getRegi_id();
            String HD = getRadioValue(R.id.radioGroupHD);
            String parent_HD = getRadioValue(R.id.radioGroupPHD);
            String hyperTension = getRadioValue(R.id.radioGroupHT);
            String covid = getRadioValue(R.id.radioGroupCovid);
            String smoking = getRadioValue(R.id.radioGroupSmoking);
            String eatingOutside = getRadioValue(R.id.radioGroupEatOutside);

            HashMap<String, String> map = new HashMap<>();
            map.put("has_heart_disease", HD);
            map.put("has_parent_heart_disease", parent_HD);
            map.put("has_hyper_tension", hyperTension);
            map.put("has_covid", covid);
            map.put("has_smoking", smoking);
            map.put("has_eating_outside", eatingOutside);

            myDb.createOrUpdateMedicalProfile(regi_id, map);
            return "hello";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }


}