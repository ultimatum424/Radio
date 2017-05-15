package com.example.ultim.radio5.Radio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.example.ultim.radio5.AppConstant;
import com.example.ultim.radio5.NavigationDrawerActivity;
import com.example.ultim.radio5.R;

import static com.example.ultim.radio5.Pojo.RadioStateEvent.SateEnum.PLAY;

/**
 * Created by Ultim on 15.05.2017.
 */

public class RadioNotification {

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public Notification getRadioNotification() {
        return radioNotification;
    }

    private NotificationManager notificationManager = null;
    private Notification radioNotification = null;
    private RemoteViews views = null;
    private RemoteViews expandedViews = null;


    void showNotification(Context context, String title){
        views = new RemoteViews(context.getPackageName(),
                R.layout.radio_push);
        expandedViews = new RemoteViews(context.getPackageName(), R.layout.radio_push_expanded);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Select action
        Intent notificationIntent = new Intent(context, NavigationDrawerActivity.class);
        notificationIntent.setAction(AppConstant.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingSelectIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        // Close action
        Intent closeNotificationIntent = new Intent();
        closeNotificationIntent.setAction(AppConstant.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pendingIntentClose = PendingIntent.getBroadcast(context, 2, closeNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.button_close, pendingIntentClose);
        expandedViews.setOnClickPendingIntent(R.id.button_close, pendingIntentClose);
        // Play action
        Intent playNotificationIntent = new Intent(AppConstant.ACTION.PLAY_ACTION);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 1, playNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.button_play_push, pendingIntentPlay);
        expandedViews.setOnClickPendingIntent(R.id.button_play_push, pendingIntentPlay);

        views.setTextViewText(R.id.singer, title);
        expandedViews.setTextViewText(R.id.singer, title);

        views.setTextViewText(R.id.song, "Исполнитель: Название трека");
        expandedViews.setTextViewText(R.id.song, "Исполнитель: Название трека");

        radioNotification = new NotificationCompat.Builder(context)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContent(views)
                .setCustomBigContentView(expandedViews)
                .setSmallIcon(R.drawable.ic_radio_black_24dp)
                .setContentIntent(pendingSelectIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();
    }

    void updateNotification(){
        if (RadioService.stateRadio != PLAY){
            views.setInt(R.id.button_play_push, "setBackgroundResource", R.mipmap.play_ic);
            expandedViews.setInt(R.id.button_play_push, "setBackgroundResource", R.mipmap.play_ic);
        } else {
            views.setInt(R.id.button_play_push, "setBackgroundResource", R.mipmap.pause_ic);
            expandedViews.setInt(R.id.button_play_push, "setBackgroundResource", R.mipmap.pause_ic);
        }
        notificationManager.notify(AppConstant.NOTIFICATION_ID.FOREGROUND_SERVICE, radioNotification);
    }
}
