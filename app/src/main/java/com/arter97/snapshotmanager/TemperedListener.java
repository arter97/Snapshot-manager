package com.arter97.snapshotmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by arter97 on 7/8/17.
 *
 * Waits until called by am.
 * Displays recovery notification.
 *
 * The service can be safely destroyed after showing the notification.
 */

public class TemperedListener extends Service {
    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder nBuilder = new Notification.Builder(this);

        nBuilder.setContentTitle("System recovery");
        nBuilder.setContentText("Your data might have been tempered by a malware");
        nBuilder.setSmallIcon(R.mipmap.ic_launcher);
        nBuilder.setOngoing(true); // Do not allow user to dismiss without interacting first

        mNotifyManager.notify(1, nBuilder.build());

        stopSelf(); // Destroy the service
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}