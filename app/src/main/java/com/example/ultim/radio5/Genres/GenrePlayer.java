package com.example.ultim.radio5.Genres;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.example.ultim.radio5.IRadioPlayer;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

/**
 * Created by Ultim on 04.06.2017.
 */

public class GenrePlayer implements IRadioPlayer, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,  MediaPlayer.OnInfoListener {

    public static final float VOLUME_DUCK = 0.2f;
    public static final float VOLUME_NORMAL = 1.0f;

    // we don't have audio focus, and can't duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    // we don't have focus, but can duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    // we have full audio focus
    private static final int AUDIO_FOCUSED  = 2;

    private final Context mContext;
    private int mState;
    private boolean mPlayOnFocusGain;
    private volatile boolean mReceiversRegistered;
    private Uri mCurrentSource;
    private String mCurrentTitle;
    private int mCurrentPlay;

    // Type of audio focus we have:
    private int mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
    private final AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;

    private final IntentFilter mAudioNoisyIntentFilter =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private final IntentFilter mAudioActionHeadsetPlugFilter =
            new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);

    private final BroadcastReceiver mAudioNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                Log.d("GenrePlayer", "Headphones disconnected.");
                //TODO; ADD ACTION
                mContext.startService(new Intent(mContext, GenrePlayerService.class).setAction(GenrePlayerService.ACTION_PAUSE));
                // pause();
            }
        }
    };

    public GenrePlayer(Context context) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mState = PlaybackStateCompat.STATE_NONE;
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
        mContext.startService(new Intent(mContext, GenrePlayerService.class).setAction(GenrePlayerService.ACTION_NEXT));
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
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
        relaxResources(false);
        mState = PlaybackStateCompat.STATE_PAUSED;
        EventBus.getDefault().postSticky(new GenreMessage(mState, mCurrentTitle, mCurrentSource, mCurrentPlay));
        unregisterReceivers();
    }

    @Override
    public void stop() {
        mState = PlaybackStateCompat.STATE_STOPPED;
        unregisterReceivers();
        EventBus.getDefault().postSticky(new GenreMessage(mState, mCurrentTitle, mCurrentSource, mCurrentPlay));
        giveUpAudioFocus();
        relaxResources(true);
    }

    @Override
    public boolean isPlaying() {
        return mPlayOnFocusGain || (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    @Override
    public void play(String source, String title) {

    }

    @Override
    public void play(Uri source, String title) {

    }

    @Override
    public void play(Uri source, String title, int num) {
        mCurrentPlay = num;
        mPlayOnFocusGain = true;
        mCurrentTitle = title;
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
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(mContext, source);
            mMediaPlayer.prepare();
            configMediaPlayerState();
        } catch (IOException ex) {
            Log.e("RadioPlayer" + ex, "Exception playing song");
            //TODO: ADD EXIT PLAYER
        }
    }
        //EventBus.getDefault().postSticky(new GenreMessage(mState, mCurrentTitle, mCurrentSource));
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
        EventBus.getDefault().postSticky(new GenreMessage(mState, mCurrentTitle, mCurrentSource, mCurrentPlay));
        mPlayOnFocusGain = false;
    }

    private void createMediaPlayerIfNeeded() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();

            mMediaPlayer.setWakeMode(mContext.getApplicationContext(),
                    PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
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
    public void onSeekComplete(MediaPlayer mp) {

    }

    private void registerReceivers() {
        if (!mReceiversRegistered) {
            mContext.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter);
            mReceiversRegistered = true;
        }
    }
    private void unregisterReceivers() {
        if (mReceiversRegistered) {
            mContext.unregisterReceiver(mAudioNoisyReceiver);
            mReceiversRegistered = false;
        }
    }
}
