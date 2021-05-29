package com.samsung.android.sdk.accessory.example.filetransfer.receiver;


import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ProfileRegisterActivity extends AppCompatActivity  {

    EditText name, phone_num, email, phone, password;


    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    String[] planetArray;
    CheckBox ch, ch1, ch2, ch3;
    RadioGroup rg;
    Button nextBtn;
    Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_register);
        nextBtn = (Button) findViewById(R.id.bNext);


        planetArray = getResources().getStringArray(R.array.planets_array);
        Log.d("register", "here");
        spinner = (Spinner) findViewById(R.id.planets_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("Tag---", "clicekd : " + i);

                Toast.makeText(getApplicationContext(), "Selected User: " + planetArray[i], Toast.LENGTH_SHORT).show();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        mDisplayDate = (TextView) findViewById(R.id.tvDate);

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ProfileRegisterActivity.this,
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
                Log.d("tag", "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                String date = month + "/" + day + "/" + year;
                mDisplayDate.setText(date);
            }
        };

        rg = (RadioGroup) findViewById(R.id.radio);


        // Finding CheckBox by its unique ID
        ch = (CheckBox) findViewById(R.id.checkBox);
        ch1 = (CheckBox) findViewById(R.id.checkBox2);
        ch2 = (CheckBox) findViewById(R.id.checkBox3);
        ch3 = (CheckBox) findViewById(R.id.checkBox4);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> checkedList = getCheckedList();
                String selectedRadio = getSelectedRadioItem();
                String selectedDropDown = getSelectedDropDown();

                Toast.makeText(getApplicationContext(), selectedDropDown, Toast.LENGTH_SHORT).show();

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


    private String getSelectedDropDown() {

        String text = spinner.getSelectedItem().toString();
        return text;
    }

    private String getSelectedRadioItem() {

        int selectedId = rg.getCheckedRadioButtonId();

        RadioButton radioButton;
        radioButton = (RadioButton) findViewById(selectedId);
        return radioButton.getText().toString();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


}