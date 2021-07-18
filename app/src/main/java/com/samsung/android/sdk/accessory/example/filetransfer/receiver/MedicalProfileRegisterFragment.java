package com.samsung.android.sdk.accessory.example.filetransfer.receiver;


import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.MEDICAL_PROFILE_URL;


public class MedicalProfileRegisterFragment extends Fragment {


    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    String[] bloodArray;
    CheckBox ch, ch1, ch2, ch3;
    RadioGroup rg;
    Button nextBtn;
    Spinner spinner;

    EditText heightText, weightText;
    String dob;
    Context thisContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        thisContext = container.getContext();
        return inflater.inflate(R.layout.activity_profile_register, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nextBtn = (Button) getView().findViewById(R.id.bNext);

        heightText = (EditText) getView().findViewById(R.id.height);
        weightText = (EditText) getView().findViewById(R.id.weight);

        bloodArray = getResources().getStringArray(R.array.blood_groups);
        Log.d("register", "here");
        spinner = (Spinner) getView().findViewById(R.id.blood_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(thisContext,
                R.array.blood_groups, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("Tag---", "clicekd : " + i);

                Toast.makeText(thisContext, "Selected User: " + bloodArray[i], Toast.LENGTH_SHORT).show();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        mDisplayDate = (TextView) getView().findViewById(R.id.tvDate);

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

        rg = (RadioGroup) getView().findViewById(R.id.genderRadio);


        // Finding CheckBox by its unique ID
        ch = (CheckBox) getView().findViewById(R.id.checkBox);
        ch1 = (CheckBox) getView().findViewById(R.id.checkBox2);
        ch2 = (CheckBox) getView().findViewById(R.id.checkBox3);
        ch3 = (CheckBox) getView().findViewById(R.id.checkBox4);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new submitValues().execute();//                Toast.makeText(getApplicationContext(), selectedDropDown, Toast.LENGTH_SHORT).show();

//                Toast.makeText(getApplicationContext(), selectedRadio, Toast.LENGTH_SHORT).show();
            }
        });
    }



    public List<String> getCheckedList() {
        String msg = "";
        List<String> checkedList = new ArrayList<>();


        if (ch.isChecked()) {
            checkedList.add(ch.getText().toString());
            msg += ch.getText().toString();

        }
        if (ch1.isChecked())
            checkedList.add(ch1.getText().toString());
        if (ch2.isChecked())
            checkedList.add(ch2.getText().toString());
        if (ch3.isChecked())
            checkedList.add(ch3.getText().toString());

        return checkedList;
    }


    private String getBG() {

        String text = spinner.getSelectedItem().toString();
        return text;
    }

    private String getGender() {
        int selectedId = rg.getCheckedRadioButtonId();

        RadioButton radioButton;
        radioButton = (RadioButton) getView().findViewById(selectedId);
        return radioButton.getText().toString();
    }

    private class submitValues extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            final DatabaseHelper myDb = new DatabaseHelper(thisContext);
            String regi_id = "xyz"; // myDb.get_profile().getRegi_id();
            myDb.close();
            OkHttpClient client = new OkHttpClient();
            String gender = getGender();
            String blood_group = getBG();
//                jsonObject.put("registration_id", "3d2594ec-7c88-4f9c-9c2c-3e4ddf9891be");

            String dob =  mDisplayDate.getText().toString();
            String height = heightText.getText().toString();
            String weight = weightText.getText().toString();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("gender", gender);
                jsonObject.put("blood_group", blood_group);
                jsonObject.put("height", height);
                jsonObject.put("registration_id", regi_id);
                jsonObject.put("weight",  weight);
                jsonObject.put("dob", dob);

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