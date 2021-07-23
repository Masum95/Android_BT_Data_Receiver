package com.samsung.android.sdk.accessory.example.filetransfer.receiver.FileRecvRecord;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.R;

import java.util.ArrayList;

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


        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = (View) inflater.inflate(
                    R.layout.activity_listview, null);
        }

        TextView name = (TextView)convertView.findViewById(R.id.text1);
        TextView summary=(TextView)convertView.findViewById(R.id.text2);

        name.setText(customListItems.get(position).getTitle());
        summary.setText(customListItems.get(position).getDetails());
        View row = convertView;
        row.setBackgroundColor(customListItems.get(position).getBackgroundColor());
        return convertView;
    }
}