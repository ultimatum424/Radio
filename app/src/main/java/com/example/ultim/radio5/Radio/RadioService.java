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
import com.example.ultim.radio5.MainActivity;
import com.example.ultim.radio5.Pojo.RadioStateEvent;
import com.example.ultim.radio5.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.Objects;

public class RadioService extends Service implements  MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener,
     ConnectivityReceiver.ConnectivityReceiverListener, MusicFocusable, NotificationRadioReceiver.NotificationRadioReceiverListener,
    MediaPlayer.OnInfoListener{

    private static final String TAG = "RADIO";
    public static final float DUCK_VOLUME = 0.1f;
    private int NOTIFICATION = 1; // Unique identifier for our notification
    public static boolean isRunning = false;
    private NotificationManager notificationManager = null;
    MediaPlayer player;
    private WifiManager.WifiLock mWifiLock;
    private boolean isPrepare = false;
    private final BroadcastReceiver connectionBroadcast = new ConnectivityReceiver();
    private final IBinder mBinder = new MyBinder();
    AudioFocusHelper mAudioFocusHelper = null;
    AudioManager mAudioManager;

    @Override
    public void NotificationListener(String action) {
        if (AppConstant.STOP_ACTION.equals(action)){
            giveUpAudioFocus();
            relaxRecourse();
            stopSelf();
        }
    }



    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

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
          // player.setDataSource("http://edge.live.mp3.mdn.newmedia.nacamar.net:80/radiosalueclassicrock/livestream96s.mp3");
           player.setDataSource("http://217.22.172.49:8000/o5radio");
        } catch (IOException e) {
            e.printStackTrace();
            relaxRecourse();
            Toast.makeText(this, "Соеденение с сервером не установленно", Toast.LENGTH_SHORT).show();
            stopSelf();

        }
        player.prepareAsync();
        isPrepare = true;
        EventBus.getDefault().postSticky(new RadioStateEvent(RadioStateEvent.SateEnum.BUFFERING));
    }


    private void resetPlayer() {
        if (player.isPlaying()) {
            player.stop();
        }
        player.reset();
        EventBus.getDefault().postSticky(new RadioStateEvent(RadioStateEvent.SateEnum.BUFFERING));
    }

    private void relaxRecourse() {
        stopForeground(true);
        if (player != null) {
            player.reset();
            player.release();
            player = null;
        }
        notificationManager.cancel(NOTIFICATION); // Remove notification
        isRunning = false;
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    private void reConfigMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            if (player.isPlaying()) {
                player.pause();
                EventBus.getDefault().postSticky(new RadioStateEvent(RadioStateEvent.SateEnum.PAUSE));
                return;
            }
        } else if (mAudioFocus == AudioFocus.NoFocusCanDuck) {
            player.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
        } else {
            player.setVolume(1.0f, 1.0f); // we can be loud
        }
        if (!player.isPlaying()) {
            player.start();
            EventBus.getDefault().postSticky(new RadioStateEvent(RadioStateEvent.SateEnum.PLAY));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        if (!ConnectivityReceiver.isConnected()) {
            Toast.makeText(this, "Соеденение с интернетом не установленно", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);;
        MyApplication.getInstance().setConnectivityListener(this);
        MyApplication.getInstance().setNotificationRadioLister(this);
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
        if (intent.getAction()!=  null) {
            if (intent.getAction().equals(AppConstant.ACTION.STARTFOREGROUND_ACTION)) {
                Toast.makeText(this, "Start Play", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Clicked Play");
                runPlayer();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        giveUpAudioFocus();
        EventBus.getDefault().postSticky(new RadioStateEvent(RadioStateEvent.SateEnum.STOP));
        unregisterReceiver(connectionBroadcast);
        relaxRecourse();
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
        isPrepare = false;
        tryToGetAudioFocus();
        reConfigMediaPlayer();
        EventBus.getDefault().postSticky(new RadioStateEvent(RadioStateEvent.SateEnum.PLAY));
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        boolean isPlaying = player.isPlaying();
        if (isPlaying && !isConnected) {
            resetPlayer();
        }
        if (!isPlaying && isConnected && !isPrepare) {
            runPlayer();
        }
    }

    @Override
    public void onGainedAudioFocus() {
        mAudioFocus = AudioFocus.Focused;
            reConfigMediaPlayer();
    }

    @Override
    public void onLostAudioFocus(boolean canDuck) {
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;
        if (player != null && player.isPlaying()) {
            reConfigMediaPlayer();
        }
    }


    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.d(TAG, "BUFFERING_1");
                EventBus.getDefault().postSticky(new RadioStateEvent(RadioStateEvent.SateEnum.BUFFERING));
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.d(TAG, "BUFFERING_0");
                EventBus.getDefault().postSticky(new RadioStateEvent(RadioStateEvent.SateEnum.PLAY));
                break;
        }
            return false;
    }

    class MyBinder extends Binder {
        RadioService getService() {
            return RadioService.this;
        }
    }

    @Subscribe
    public void onEvent(String start){
        if (Objects.equals(start, "start")) {
            tryToGetAudioFocus();
            reConfigMediaPlayer();
        }
    }
}
