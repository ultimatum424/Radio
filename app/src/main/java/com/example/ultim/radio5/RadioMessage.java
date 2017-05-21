package com.example.ultim.radio5;

import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Created by Ultim on 21.05.2017.
 */

public class RadioMessage {
    private int mState;
    private String mTitle;
    private String mUrl;

    public RadioMessage(int state, String title, String url){
        mState = state;
        mTitle = title;
        mUrl = url;
    }

    public RadioMessage(int state, String title){
        mState = state;
        mTitle = title;
        mUrl = "";
    }
    public RadioMessage(){
        mUrl = "";
        mUrl = "";
        mState = PlaybackStateCompat.STATE_NONE;

    }

    public int getState() {
        return mState;
    }

    public String getUniversityName() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }
}
