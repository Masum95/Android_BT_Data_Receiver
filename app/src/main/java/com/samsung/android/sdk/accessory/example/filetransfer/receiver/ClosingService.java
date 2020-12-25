package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class ClosingService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d("here ", "in closing service ");
        // Handle application closing
//        fireClosingNotification();

        // Destroy the service
        stopSelf();
    }
}