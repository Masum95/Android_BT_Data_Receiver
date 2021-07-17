/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd. All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that
 * the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or
 *       other materials provided with the distribution.
 *     * Neither the name of Samsung Electronics Co., Ltd. nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.DatabaseHelper;
import com.samsung.android.sdk.accessoryfiletransfer.SAFileTransfer;
import com.samsung.android.sdk.accessoryfiletransfer.SAFileTransfer.EventListener;
import com.samsung.android.sdk.accessoryfiletransfer.SAft;
import androidx.annotation.NonNull;





import java.io.UnsupportedEncodingException;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.DEST_DIRECTORY;

public class FileTransferReceiver extends SAAgent {
    private static final String TAG = "FileTransferReceiver";
    private final IBinder mReceiverBinder = new ReceiverBinder();
    private static final Class<ServiceConnection> SASOCKET_CLASS = ServiceConnection.class;
    private ServiceConnection mConnection = null;
    private SAFileTransfer mSAFileTransfer = null;
    private EventListener mCallback;
    private FileAction mFileAction = null;
    DatabaseHelper myDb;
    private Context mCtxt;

    private static boolean isRunning;

    //-----------------------------------------------------
    public int counter=0;



    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);


        Log.d("intent", String.valueOf(intent));
        if (intent == null || intent.getAction().equals(String.valueOf( Constants.ACTION.STOPFOREGROUND_ACTION))) {

            stopForeground(true);
            stopSelfResult(startId);
            Log.d("tag", "Received Stop Foreground Intent-------");
            onDestroy();
            return START_NOT_STICKY;

        }

        else if (intent.getAction().equals(String.valueOf(Constants.ACTION.STARTFOREGROUND_ACTION) )) {
            Log.d("tag", "Received Start Foreground Intent ");

            return START_STICKY;
        }




        return START_STICKY;

    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //create a intent that you want to start again..
        Intent intent = new Intent(getApplicationContext(), FileTransferReceiver.class);
        intent.setAction(String.valueOf(Constants.ACTION.STARTFOREGROUND_ACTION));
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
        super.onTaskRemoved(rootIntent);
    }


//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }


    ///----------------------------------------------end-----------------------------------------------------------------------------
    public FileTransferReceiver() {
        super(TAG, SASOCKET_CLASS);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        myDb = new DatabaseHelper(this);
        mCtxt = getApplicationContext();

        isRunning = true;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());


        Log.d(TAG, "On Create of Sample FileTransferReceiver Service");
        mCallback = new EventListener() {
            @Override
            public void onProgressChanged(int transId, int progress) {
                Log.d(TAG, "onProgressChanged : " + progress + " for transaction : " + transId);
//                if (mFileAction != null) {
//                    mFileAction.onFileActionProgress(progress);
//                }
            }

            @Override
            public void onTransferCompleted(int transId, String fileName, int errorCode) {
                Log.d(TAG, "onTransferCompleted: tr id : " + transId + " file name : " + fileName + " error : "
                        + errorCode);
                if (errorCode != SAFileTransfer.ERROR_NONE) {
                    boolean isInserted = myDb.insertFileInfo(fileName,
                            "sw",
                            0,
                            0);
                    new ModelRunner(mCtxt).execute(fileName);

                    if(isInserted == true)
                        Toast.makeText(mCtxt,"Data Inserted",Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(mCtxt,"Data not Inserted",Toast.LENGTH_LONG).show();

//                    mFileAction.onFileActionTransferComplete(fileName);
                }
            }

            @Override
            public void onTransferRequested(final int id, final String fileName) {
                Log.d(TAG, "onTransferRequested: id- " + id + " file name: " + fileName);

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {

                            String receiveFileName = fileName.substring(fileName.lastIndexOf("/"), fileName.length());
                            receiveFile(id, DEST_DIRECTORY
                                    + receiveFileName, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                thread.start();


//                if (FileTransferReceiverActivity.isUp()) {
//                    Log.d(TAG, "Activity is up");
////                    mFileAction.onFileActionTransferRequested(id, fileName);
//
//                } else {
//                    Log.d(TAG, "Activity is not up, invoke activity");
//                    mCtxt.startActivity(new Intent()
//                            .setClass(mCtxt, FileTransferReceiverActivity.class)
//                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            .setAction("incomingFT").putExtra("tx", id)
//                            .putExtra("fileName", fileName));
//                    int counter = 0;
//                    while (counter < 10) {
//                        counter++;
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        if (mFileAction != null) {
////                            mFileAction.onFileActionTransferRequested(id, fileName);
//                            break;
//                        }
//                    }
//                }
            }

            @Override
            public void onCancelAllCompleted(int errorCode) {
//                mFileAction.onFileActionError();
                Log.e(TAG, "onCancelAllCompleted: Error Code " + errorCode);
            }
        };

        SAft saft = new SAft();

        try {
            saft.initialize(this);

        } catch (SsdkUnsupportedException e) {
            if (e.getType() == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
                Toast.makeText(getBaseContext(), "Cannot initialize, DEVICE_NOT_SUPPORTED", Toast.LENGTH_SHORT).show();
            } else if (e.getType() == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
                Toast.makeText(getBaseContext(), "Cannot initialize, LIBRARY_NOT_INSTALLED.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), "Cannot initialize, UNKNOWN.", Toast.LENGTH_SHORT).show();
            }
            e.printStackTrace();
            return;
        } catch (Exception e1) {
            Toast.makeText(getBaseContext(), "Cannot initialize, SAft.", Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            return;
        }

        mSAFileTransfer = new SAFileTransfer(FileTransferReceiver.this, mCallback);

    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mReceiverBinder;
    }

    @Override
    public void onDestroy() {
        isRunning = false;

        try{
            mSAFileTransfer.close();
        }catch (Exception e1) {
            e1.printStackTrace();
        }
        mSAFileTransfer = null;
        super.onDestroy();

        Log.i(TAG, "FileTransferReceiver Service is Stopped.");
    }

    @Override
    protected void onFindPeerAgentsResponse(SAPeerAgent[] peerAgents, int result) {
        if (mConnection == null) {
            Log.d(TAG, "onFindPeerAgentResponse : mConnection is null");
        }
    }

    @Override
    protected void onServiceConnectionResponse(SAPeerAgent peer, SASocket socket, int result) {
        Log.i(TAG, "onServiceConnectionResponse: result - " + result);
        if (result == SAAgent.CONNECTION_SUCCESS) {
            if (socket != null) {
                mConnection = (ServiceConnection) socket;
                Toast.makeText(getBaseContext(), "Connection established for FT", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void receiveFile(int transId, String path, boolean bAccept) {
        Log.d(TAG, "receiving file : transId: " + transId + "bAccept : " + bAccept);
        if (mSAFileTransfer != null) {
            if (bAccept) {
                mSAFileTransfer.receive(transId, path);
            } else {
                mSAFileTransfer.reject(transId);
            }
        }
    }

    public void cancelFileTransfer(int transId) {
        if (mSAFileTransfer != null) {
            mSAFileTransfer.cancel(transId);
        }
    }

    public void registerFileAction(FileAction action) {
        this.mFileAction = action;
    }

    public class ReceiverBinder extends Binder {
        public FileTransferReceiver getService() {
            return FileTransferReceiver.this;
        }
    }

    public class ServiceConnection extends SASocket {
        public ServiceConnection() {
            super(ServiceConnection.class.getName());
        }

        @Override
        protected void onServiceConnectionLost(int reason) {
            Log.e(TAG, "onServiceConnectionLost: reason-" + reason);
            mConnection = null;
        }

        @Override
        public void onReceive(int channelId, byte[] data) {
            try {
                Log.i(TAG, "onReceive: channelId" + channelId + "data: " + new String(data, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(int channelId, String errorMessage, int errorCode) {
//            mFileAction.onFileActionError();
            Log.e(TAG, "Connection is not alive ERROR: " + errorMessage + "  " + errorCode);
        }
    }

    public interface FileAction {
        void onFileActionError();

        void onFileActionProgress(long progress);

        void onFileActionTransferComplete(String fileName);

        void onFileActionTransferRequested(int id, String path);
    }
}
