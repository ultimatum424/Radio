package com.example.ultim.radio5.Radio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.ultim.radio5.AppConstant;
import com.example.ultim.radio5.NavigationDrawerActivity;
import com.example.ultim.radio5.Pojo.RadioStateEvent;
import com.example.ultim.radio5.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.Objects;

import static com.example.ultim.radio5.Pojo.RadioStateEvent.*;
import static com.example.ultim.radio5.Pojo.RadioStateEvent.SateEnum.*;

public class RadioService extends Service implements  MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener,
     ConnectivityReceiver.ConnectivityReceiverListener, MusicFocusable, NotificationRadioReceiver.NotificationRadioReceiverListener,
    MediaPlayer.OnInfoListener{

    private static final String TAG = "RADIO";
    public static final float DUCK_VOLUME = 0.1f;

    private NotificationManager notificationManager = null;
    Notification radioNotification;
    RemoteViews views;
    public static SateEnum stateRadio = STOP;
    MediaPlayer player;
    private WifiManager.WifiLock mWifiLock;

    private final BroadcastReceiver connectionBroadcast = new ConnectivityReceiver();
    private final BroadcastReceiver notificationBroadcast = new NotificationRadioReceiver();
    private final IBinder mBinder = new MyBinder();
    AudioFocusHelper mAudioFocusHelper = null;
    AudioManager mAudioManager;

    @Override
    public void NotificationListener(String action) {
        if (AppConstant.ACTION.STOPFOREGROUND_ACTION.equals(action)){
            giveUpAudioFocus();
            relaxRecourse();
            stopSelf();
        }
        if (AppConstant.ACTION.PLAY_ACTION.equals(action)){
            tryToGetAudioFocus();
            reConfigMediaPlayer(true);
        }
    }

    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    void showNotification(){
        views = new RemoteViews(getPackageName(),
                R.layout.custom_push);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Select action
        Intent notificationIntent = new Intent(this, NavigationDrawerActivity.class);
        notificationIntent.setAction(AppConstant.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingSelectIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        // Close action
        Intent closeNotificationIntent = new Intent();
        closeNotificationIntent.setAction(AppConstant.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pendingIntentClose = PendingIntent.getBroadcast(getBaseContext(), 2, closeNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.button_close, pendingIntentClose);
        // Play action
        Intent playNotificationIntent = new Intent(AppConstant.ACTION.PLAY_ACTION);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(getBaseContext(), 1, playNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.button_play_push, pendingIntentPlay);

        views.setTextViewText(R.id.titlePush, "Исполнитель: Название трека");
        radioNotification = new Notification.Builder(this).build();
        radioNotification.contentView = views;
        radioNotification.flags = Notification.FLAG_ONGOING_EVENT;
        radioNotification.icon = R.drawable.ic_radio_black_24dp;
        radioNotification.contentIntent = pendingSelectIntent;
        startForeground(AppConstant.NOTIFICATION_ID.FOREGROUND_SERVICE, radioNotification);
    }

    private void updateNotification(){
        if (RadioService.stateRadio != PLAY){
            views.setInt(R.id.button_play_push, "setBackgroundResource", R.mipmap.play_ic);
        } else {
            views.setInt(R.id.button_play_push, "setBackgroundResource", R.mipmap.pause_ic);
        }
        notificationManager.notify(AppConstant.NOTIFICATION_ID.FOREGROUND_SERVICE, radioNotification);
    }

    private void initPlayer() {
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnErrorListener(this);
        player.setOnPreparedListener(this);
        player.setOnInfoListener(this);
    }

    private void runPlayer() {

        try {
           player.setDataSource("http://217.22.172.49:8000/o5radio");
        } catch (IOException e) {
            e.printStackTrace();
            relaxRecourse();
            Toast.makeText(this, "Соеденение с сервером не установленно", Toast.LENGTH_SHORT).show();
            stopSelf();

        }
        player.prepareAsync();
        stateRadio = BUFFERING;
        showNotification();
        EventBus.getDefault().postSticky(new RadioStateEvent(stateRadio));
    }


    private void resetPlayer() {
        if (player.isPlaying()) {
            player.stop();
        }
        player.reset();
        stateRadio = BUFFERING;
        EventBus.getDefault().postSticky(new RadioStateEvent(stateRadio));
    }

    private void relaxRecourse() {
        stopForeground(true);
        if (player != null) {
            player.reset();
            player.release();
            player = null;
            stateRadio = STOP;
            EventBus.getDefault().postSticky(new RadioStateEvent(stateRadio));
        }
        notificationManager.cancel(AppConstant.NOTIFICATION_ID.FOREGROUND_SERVICE); // Remove notification
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    private void reConfigMediaPlayer(boolean isBroadcast) {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck || isBroadcast) {
            if (player.isPlaying()) {
                player.pause();
                stateRadio = PAUSE;
                updateNotification();
                EventBus.getDefault().postSticky(new RadioStateEvent(stateRadio));
                return;
            }
        } else if (mAudioFocus == AudioFocus.NoFocusCanDuck) {
            player.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
        } else {
            player.setVolume(1.0f, 1.0f); // we can be loud
        }
        if (!player.isPlaying()) {
            player.start();
            stateRadio = PLAY;
            EventBus.getDefault().postSticky(new RadioStateEvent(stateRadio));
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        stateRadio = STOP;
        if (!ConnectivityReceiver.isConnected()) {
            Toast.makeText(this, "Соеденение с интернетом не установленно", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);;
        MyApplication.getInstance().setConnectivityListener(this);
        MyApplication.getInstance().setNotificationRadioLister(this);
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction("com.radio5.radionotification.action.stopforeground");
        intentFilter1.addAction("com.radio5.radionotification.action.play");
        registerReceiver(notificationBroadcast, intentFilter1);
        IntentFilter netFilter = new IntentFilter();
        netFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectionBroadcast, netFilter);
        initPlayer();
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "Media Player Wi-Fi Lock");
        mWifiLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        runPlayer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        giveUpAudioFocus();
        relaxRecourse();
        unregisterReceiver(connectionBroadcast);
        unregisterReceiver(notificationBroadcast);
        super.onDestroy();
    }

    void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.Focused;
    }

    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.abandonFocus()){
            mAudioFocus = AudioFocus.NoFocusNoDuck;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(this, "Соеденение с сервером не установленно", Toast.LENGTH_SHORT).show();
        stopForeground(true);
        giveUpAudioFocus();
        stopSelf();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        tryToGetAudioFocus();
        reConfigMediaPlayer(false);
        stateRadio = PLAY;
        updateNotification();
        EventBus.getDefault().postSticky(new RadioStateEvent(stateRadio));
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        boolean isPlaying = player.isPlaying();
        if (isPlaying && !isConnected) {
            resetPlayer();
        }
        if (!isPlaying && isConnected && stateRadio != BUFFERING) {
            runPlayer();
        }
    }

    @Override
    public void onGainedAudioFocus() {
        mAudioFocus = AudioFocus.Focused;
            reConfigMediaPlayer(false);
    }

    @Override
    public void onLostAudioFocus(boolean canDuck) {
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;
        if (player != null && player.isPlaying()) {
            reConfigMediaPlayer(false);
        }
    }


    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.d(TAG, "BUFFERING_1");
                stateRadio = BUFFERING;
                EventBus.getDefault().postSticky(new RadioStateEvent(stateRadio));
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.d(TAG, "BUFFERING_0");
                stateRadio = PLAY;
                EventBus.getDefault().postSticky(new RadioStateEvent(stateRadio));
                break;
        }
            return false;
    }

    class MyBinder extends Binder {
        RadioService getService() {
            return RadioService.this;
        }
    }
}
