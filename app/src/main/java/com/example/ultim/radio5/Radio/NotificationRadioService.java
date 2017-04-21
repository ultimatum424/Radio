package com.example.ultim.radio5.Radio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.ultim.radio5.AppConstant;
import com.example.ultim.radio5.MainActivity;
import com.example.ultim.radio5.R;

import org.greenrobot.eventbus.EventBus;

public class NotificationRadioService extends Service {
    Notification status;
    RemoteViews views;
    private NotificationManager mNotificationManager;
    private final String LOG_TAG = "NotificationRadioService";
    public NotificationRadioService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(AppConstant.ACTION.STARTFOREGROUND_ACTION)) {
            showNotification();
            Intent intentStartRadio = new Intent(this, RadioService.class);
            intentStartRadio.setAction(AppConstant.ACTION.STARTFOREGROUND_ACTION);
            startService(intentStartRadio);
        }
        else if (intent.getAction().equals(AppConstant.ACTION.PLAY_ACTION)) {
            Toast.makeText(this, "Clicked Play", Toast.LENGTH_SHORT).show();
            Log.i(LOG_TAG, "Clicked Play");
            Intent intentPauseRadio = new Intent(this, RadioService.class);
            if (RadioService.isRunning){
                intentPauseRadio.setAction(AppConstant.ACTION.STOPFOREGROUND_ACTION);
                stopService(intentPauseRadio);
            } else {
                intentPauseRadio.setAction(AppConstant.ACTION.STARTFOREGROUND_ACTION);
                startService(intentPauseRadio);
            }
            UpdateNotification();
        }
        else if (intent.getAction().equals(
                AppConstant.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            Toast.makeText(this, "Service Stoped", Toast.LENGTH_SHORT).show();
            Intent intentStopeRadio = new Intent(this, RadioService.class);
            intentStopeRadio.setAction(AppConstant.ACTION.STOPFOREGROUND_ACTION);
            stopService(intentStopeRadio);
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    private void showNotification() {
        views = new RemoteViews(getPackageName(),
                R.layout.custom_push);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(AppConstant.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent playIntent = new Intent(this, NotificationRadioService.class);
        playIntent.setAction(AppConstant.ACTION.PLAY_ACTION);
        PendingIntent pPlayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent closeIntent = new Intent(this, NotificationRadioService.class);
        closeIntent.setAction(AppConstant.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pCloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);
        views.setOnClickPendingIntent(R.id.button_play_push, pPlayIntent);
        views.setOnClickPendingIntent(R.id.button_close, pCloseIntent);
        views.setTextViewText(R.id.titlePush, "Исполнитель: Название трека");
        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.ic_radio_black_24dp;
        status.contentIntent = pendingIntent;
        startForeground(AppConstant.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }

    private void UpdateNotification(){
        if (RadioService.isRunning){
            views.setInt(R.id.button_play_push, "setBackgroundResource", R.mipmap.play_ic);
        } else {
            views.setInt(R.id.button_play_push, "setBackgroundResource", R.mipmap.pause_ic);
        }
        mNotificationManager.notify(AppConstant.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
