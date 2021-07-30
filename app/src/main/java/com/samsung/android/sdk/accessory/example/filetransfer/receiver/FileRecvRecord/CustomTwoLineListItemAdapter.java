package com.samsung.android.sdk.accessory.example.filetransfer.receiver.FileRecvRecord;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class CustomTwoLineListItemAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<CustomListItem> customListItems;
    public CustomTwoLineListItemAdapter(Context context, ArrayList<CustomListItem> customListItems) {
        this.context = context;
        this.customListItems = customListItems;
    }

    @Override
    public int getCount() {
        return customListItems.size();
    }

    @Override
    public Object getItem(int position) {
        return customListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Map<Integer, Integer> iconMap = new HashMap<Integer, Integer>() {{
            put(0, R.id.no_record);
            put(1, R.id.weak_record);
            put(2, R.id.ok_record);
            //etc
        }};

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = (View) inflater.inflate(
                    R.layout.activity_listview, null);
        }

        TextView name = (TextView)convertView.findViewById(R.id.text1);
        TextView summary=(TextView)convertView.findViewById(R.id.text2);
        ImageView okIcon = (ImageView)  convertView.findViewById(R.id.ok_record);
        ImageView noIcon = (ImageView)  convertView.findViewById(R.id.no_record);
        ImageView weakIcon = (ImageView)  convertView.findViewById(R.id.weak_record);

        name.setText(customListItems.get(position).getTitle());
        summary.setText(customListItems.get(position).getDetails());

        convertView.setBackgroundColor(customListItems.get(position).getBackgroundColor());
//        iconMap.get(customListItems.get(position).getRecord_strength())
        int record_strength = customListItems.get(position).getRecord_strength();

        if(record_strength == 0){
            Log.d("tag======++========", String.valueOf(customListItems.get(position).getRecord_strength())) ;

            noIcon.setVisibility(View.VISIBLE);

        }else if (record_strength == 1){
            weakIcon.setVisibility(View.VISIBLE);
        }else{
            okIcon.setVisibility(View.VISIBLE);

        }

        return convertView;
    }
}