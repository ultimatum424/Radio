package com.example.ultim.radio5;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

/**
 * Created by Ultim on 16.05.2017.
 */

public class RadioPlayer implements IRadioPlayer, AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener {

    public static final float VOLUME_DUCK = 0.2f;
    public static final float VOLUME_NORMAL = 1.0f;

    // we don't have audio focus, and can't duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    // we don't have focus, but can duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    // we have full audio focus
    private static final int AUDIO_FOCUSED  = 2;

    private final Context mContext;
    private final WifiManager.WifiLock mWifiLock;
    private int mState;
    private boolean mPlayOnFocusGain;
    private volatile boolean mReceiversRegistered;
    private String mCurrentSource = "";
    private String mCurrentTitle;
    private boolean headsetConnected = false;

    // Type of audio focus we have:
    private int mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
    private final AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;

    private final IntentFilter mAudioNoisyIntentFilter =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private final IntentFilter mAudioActionHeadsetPlugFilter =
            new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);

    private  final IntentFilter mConnectFilter =
            new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");


    private final BroadcastReceiver mAudioNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                Log.d("RadioPlayer", "Headphones disconnected.");
                //TODO; ADD ACTION
                mContext.startService(new Intent(mContext, RadioPlayerService.class).setAction(RadioPlayerService.ACTION_PAUSE));
               // pause();
            }
        }
    };

    private final BroadcastReceiver mAudioActionHeadsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!headsetConnected && intent.getIntExtra("state", 0) == 1) {
                Log.d("RadioPlayer", "Headphones connected.");
                headsetConnected = true;
                if (isPlaying()) {
                 //   play(mCurrentSource, mCurrentTitle);
                }
            }
        }
    };

    private final BroadcastReceiver mConnectionChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isConnected()){
                Toast.makeText(mContext, "Интернет соеденение отсутствует", Toast.LENGTH_SHORT).show();
                stop();
                mContext.startService(new Intent(mContext, RadioPlayerService.class).setAction(RadioPlayerService.ACTION_STOP));
            }
        }
    };

    @SuppressLint("WifiManagerPotentialLeak")
    public RadioPlayer(Context context) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.mWifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "uAmp_lock");
        mState = PlaybackStateCompat.STATE_NONE;

    }

    @Override
    public void start(String source) {

    }

    @Override
    public void pause() {
        if (mState == PlaybackStateCompat.STATE_PLAYING) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        if (mState == PlaybackStateCompat.STATE_BUFFERING){
            stop();
        }
        relaxResources(false);
        mState = PlaybackStateCompat.STATE_PAUSED;
        EventBus.getDefault().postSticky(new RadioMessage(mState, mCurrentTitle, mCurrentSource));
        unregisterReceivers();
    }

    @Override
    public void stop() {
        mState = PlaybackStateCompat.STATE_STOPPED;
        unregisterReceivers();
        EventBus.getDefault().postSticky(new RadioMessage(mState, mCurrentTitle, mCurrentSource));
        giveUpAudioFocus();
        relaxResources(true);
    }

    @Override
    public boolean isPlaying() {
        return mPlayOnFocusGain || (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    @Override
    public void play(String source, String title) {
        mPlayOnFocusGain = true;
        mCurrentTitle = title;
        /*
        if (!isConnected()){
            Toast.makeText(mContext, "Интернет соеденение отсутствет", Toast.LENGTH_SHORT).show();
            stop();
            mContext.startService(new Intent(mContext, RadioPlayerService.class).setAction(RadioPlayerService.ACTION_STOP));
        }*/
        tryToGetAudioFocus();
        registerReceivers();
        if (mState == PlaybackStateCompat.STATE_PAUSED && mMediaPlayer != null && mCurrentSource.equals(source)) {
            configMediaPlayerState();
        } else {
            mState = PlaybackStateCompat.STATE_STOPPED;
            mCurrentSource = source;
            relaxResources(false); // release everything except MediaPlayer

            try {
                createMediaPlayerIfNeeded();
                mState = PlaybackStateCompat.STATE_BUFFERING;
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(source);
                mMediaPlayer.prepareAsync();
                mWifiLock.acquire();

            } catch (IOException ex) {
                Log.e("RadioPlayer" + ex, "Exception playing song");
                //TODO: ADD EXIT PLAYER
            }
        }
        EventBus.getDefault().postSticky(new RadioMessage(mState, mCurrentTitle, source));
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public void setState(int state) {
        this.mState = state;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        Log.d("RadioPlayer", String.valueOf(focusChange));
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // We have gained focus:
            mAudioFocus = AUDIO_FOCUSED;

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // We have lost focus. If we can duck (low playback volume), we can keep playing.
            // Otherwise, we need to pause the playback.
            boolean canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
            mAudioFocus = canDuck ? AUDIO_NO_FOCUS_CAN_DUCK : AUDIO_NO_FOCUS_NO_DUCK;

            // If we are playing, we need to reset media player by calling configMediaPlayerState
            // with mAudioFocus properly set.
            if (mState == PlaybackStateCompat.STATE_PLAYING && !canDuck) {
                // If we don't have audio focus and can't duck, we save the information that
                // we were playing, so that we can resume playback once we get the focus back.
                mPlayOnFocusGain = true;
            }
        } else {
            Log.e("RadioPlayer onAudioFocusChange: Ignoring unsupported focusChange: ", String.valueOf(focusChange));
        }
        configMediaPlayerState();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //TODO: ADD ACTION
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if ( mState == PlaybackStateCompat.STATE_BUFFERING){
            configMediaPlayerState();
        }

    }

    private void tryToGetAudioFocus() {
        Log.e("RadioPlayer", "tryToGetAudioFocus");
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocus = AUDIO_FOCUSED;
        } else {
            mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.e("RadioPlayer", "BUFFERING_1");
                //stateRadio = BUFFERING;
                EventBus.getDefault().postSticky(new RadioMessage(PlaybackStateCompat.STATE_BUFFERING, mCurrentTitle, mCurrentSource));
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.e("RadioPlayer", "BUFFERING_0");
                //stateRadio = PLAY;
                EventBus.getDefault().postSticky(new RadioMessage(mState, mCurrentTitle, mCurrentSource));
                break;
        }
        return false;
    }

    private void giveUpAudioFocus() {
        Log.e("RadioPlayer", "giveUpAudioFocus");
        if (mAudioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    private void configMediaPlayerState() {
        Log.e("RadioPlayer", "configurable");

        //If we don't have audio focus and can't duck, we have to pause
        if (mAudioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
            if (mState == PlaybackStateCompat.STATE_PLAYING) {
                pause();
            }
        } else { // we have audio focus:
            registerReceivers();
            if (mAudioFocus == AUDIO_NO_FOCUS_CAN_DUCK) {
                mMediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK); // we'll be relatively quiet
            } else {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL); // we can be loud again
                } // else do something for remote client.
            }
        }

        // If we were playing when we lost focus, we need to resume playing.
        if (mPlayOnFocusGain) {
            if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
                mState = PlaybackStateCompat.STATE_PLAYING;
            }
        }
        EventBus.getDefault().postSticky(new RadioMessage(mState, mCurrentTitle, mCurrentSource));
        mPlayOnFocusGain = false;
    }

    private void createMediaPlayerIfNeeded() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();

            mMediaPlayer.setWakeMode(mContext.getApplicationContext(),
                    PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);
        } else {
            mMediaPlayer.reset();
        }
    }

    private void relaxResources(boolean releaseMediaPlayer) {
        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }

    }

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    private void registerReceivers() {
        if (!mReceiversRegistered) {
            mContext.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter);
            mContext.registerReceiver(mAudioActionHeadsReceiver, mAudioActionHeadsetPlugFilter);
            mContext.registerReceiver(mConnectionChangeReceiver, mConnectFilter);
            mReceiversRegistered = true;
        }
    }
    private void unregisterReceivers() {
        if (mReceiversRegistered) {
            mContext.unregisterReceiver(mAudioNoisyReceiver);
            mContext.unregisterReceiver(mAudioActionHeadsReceiver);
            mContext.unregisterReceiver(mConnectionChangeReceiver);
            mReceiversRegistered = false;
        }
    }
}
