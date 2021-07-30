package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class Restarter extends BroadcastReceiver {


    Intent mServiceIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Broadcast Listened", "Service tried to stop");
        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();

        mServiceIntent = new Intent(context, FileTransferReceiver.class);
        mServiceIntent.setAction(String.valueOf(intent.getAction()));
        Log.d("tag", intent.getAction());
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            context.stopService(mServiceIntent);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(mServiceIntent);
            } else {
                context.startService(mServiceIntent);
            }
        }

    }
}