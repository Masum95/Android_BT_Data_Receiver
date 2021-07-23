package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Utils.getBdTimeFromUnixTimeStamp;

public class LineChartXAxisValueFormatter extends IndexAxisValueFormatter {

    @Override
    public String getFormattedValue(float value) {

        // Convert float value to date string
        // Convert from seconds back to milliseconds to format time  to show to the user


        return getBdTimeFromUnixTimeStamp(Integer.toString((int)Math.round(value)));
    }
}
