package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ResultModel;


public class LineChartModule {

    // variable for our bar chart
    private LineChart lineChart;

    Activity activity;
    Context context;


    DatabaseHelper myDb;

    XAxis xAxis;
    YAxis yAxis;

    public LineChartModule(Context context, Activity activity, int lineChartId) {
        this.context = context;
        this.activity = activity;
        myDb = new DatabaseHelper(context);

        // initializing variable for bar chart.
        lineChart = activity.findViewById(lineChartId);

        {   // // X-Axis Style // //
            xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
            // axis range
//            xAxis.setAxisMaximum(200f);
//            xAxis.setAxisMinimum(-50f);

            xAxis.setValueFormatter( new LineChartXAxisValueFormatter());
            xAxis.setLabelRotationAngle(-90);

        }
//
        {   // // Y-Axis Style // //
            yAxis = lineChart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            lineChart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

//            // axis range
//            yAxis.setAxisMaximum(200f);
//            yAxis.setAxisMinimum(-50f);
        }


        LineData mLineData = getLineData(24);
        showChart(lineChart, mLineData, Color.rgb(255, 255, 255));
        myDb.close();
    }



    // Set the display style
    private void showChart(LineChart lineChart, LineData lineData, int color) {
        lineChart.setDrawBorders(false);  //Whether to add a border on the line chart

        // no description text
        lineChart.getDescription().setText("Description of my chart");
        ;// Data description
        // If there is no data, this will be displayed, similar to the emtpyview of listview
//        lineChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable / disable grid background
        lineChart.setDrawGridBackground(false); // Whether to display the table color
        lineChart.setGridBackgroundColor(Color.WHITE & 0x70FFFFFF); // The color of the table, here is to set a transparency for the color

        // enable touch gestures
        lineChart.setTouchEnabled(true); // Set whether you can touch

        // enable scaling and dragging
        lineChart.setDragEnabled(true);// Is it possible to drag
        lineChart.setScaleEnabled(true);// Is it possible to zoom

        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(false);//

        lineChart.setBackgroundColor(color);// Set the background

        // add data
        lineChart.setData(lineData); // Setting data

        // get the legend (only possible after setting data)
        Legend mLegend = lineChart.getLegend(); // Set the scale icon to show that it is the value of the set of y

        // modify the legend ...
        // mLegend.setPosition(LegendPosition.LEFT_OF_CHART);
        mLegend.setForm(LegendForm.CIRCLE);// style
        mLegend.setFormSize(6f);// font
        mLegend.setTextColor(Color.WHITE);// colour
// mLegend.setTypeface(mTf);//Font

//        lineChart.animateX(2500); // Immediately executed animation, x-axis


        // draw limit lines behind data instead of on top


    }

    /**
     * Generate a data
     *
     * @return
     * @Param indicates the number of coordinate points in the chart
     * @Param range is used to generate random numbers within the range
     */
    private LineData getLineData(int hours) {

        List<ResultModel> resultList = myDb.getResultsOfNHours(hours);
        ArrayList<Entry> lineEntries = new ArrayList<Entry>();

        lineEntries = new ArrayList<Entry>();
        int i = 0;
        for (ResultModel result : resultList) {
            String timestamp = result.getTimestamp(); // id is column name in db
            double hr = result.getAvg_hr();
            lineEntries.add(new Entry(Float.parseFloat(timestamp) , (float) hr));
            Log.d("hrs", new SimpleDateFormat("HH:mm").format(Float.parseFloat(timestamp)) + "  " + (hr)  );
            i+= 1;
        }
        Log.d("create", String.valueOf(i));
        xAxis.setLabelCount(15, true);


        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Line1");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(3);
        lineDataSet.setColor(Color.WHITE);
        lineDataSet.setCircleColor(Color.BLUE);
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleHoleRadius(3);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setDrawValues(false);
        lineDataSet.setValueTextSize(14);
        lineDataSet.setValueTextColor(Color.GREEN);
        lineDataSet.setColor(Color.rgb(0, 255, 255)); // LINE_COLOR


        // create a data object with the datasets
        LineData lineData = new LineData(lineDataSet);

        return lineData;
    }

}
