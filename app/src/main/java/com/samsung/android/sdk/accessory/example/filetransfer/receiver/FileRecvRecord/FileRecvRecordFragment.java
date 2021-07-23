package com.samsung.android.sdk.accessory.example.filetransfer.receiver.FileRecvRecord;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;


import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.R;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FileRecvRecordFragment extends Fragment {


    private ListView listview;

    Context thisContext;
    java.util.ArrayList<String> listItems;
    ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        thisContext = container.getContext();
        return inflater.inflate(R.layout.file_rcv_record, container, false);
    }
    private List<String> getNHoursList(int startHourBefore){
        List<String> titleArray = new ArrayList<>();
        for(int i=startHourBefore;i>0;i--){

            String start = new SimpleDateFormat(" HH:mm").format(new Date(System.currentTimeMillis() - i *  3600 * 1000));
            String end = new SimpleDateFormat(" HH:mm").format(new Date(System.currentTimeMillis() - (i-1) *  3600 * 1000));
            titleArray.add(String.format("%s - %s", start, end));

        }


        return titleArray;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseHelper myDb =  new DatabaseHelper(thisContext);;

        listview = (ListView) getView().findViewById(R.id.file_record_list);
        listItems = new java.util.ArrayList<String>();


        listItems.clear();
        listItems.add("Previous History");

        List<String> titleArray = getNHoursList(24);
        List<String> detailArray = titleArray;

        List<Map<String, String>> listArray = new ArrayList<>();



//        adapter = new ArrayAdapter<String>(thisContext,
//                android.R.layout.simple_list_item_1,
//                listItems);

//        SimpleAdapter adapter = new SimpleAdapter(thisContext, listArray,
//                android.R.layout.activity_listview,
//                new String[] {"titleKey", "detailKey" },
//                new int[] {android.R.id.text1, android.R.id.text2 });


        ArrayList<CustomListItem> items=new ArrayList<>();
        Map<String, Integer> colorMap = new HashMap<String, Integer>() {{
            put("EVEN", Color.parseColor("#ffffff"));
            put("ODD", Color.parseColor("#E8E8E8"));
            put("RED", Color.parseColor("#ff0000"));
            //etc
        }};

        for(int i=0; i<titleArray.size() ; i++)
        {
            int color = 0;
            if(i%2==0) color = colorMap.get("EVEN");
            if(i%2!=0) color = colorMap.get("ODD");
            items.add(new CustomListItem(titleArray.get(i),"Files Received " + String.valueOf(myDb.getCountOfGeneratedFileInBetweenN_Nplus1Hours( 24-i)), color));
        }
        CustomTwoLineListItemAdapter mla=new CustomTwoLineListItemAdapter(thisContext, items);
        listview.setAdapter(mla);
//        listview.setAdapter(adapter);
        myDb.close();
    }


}