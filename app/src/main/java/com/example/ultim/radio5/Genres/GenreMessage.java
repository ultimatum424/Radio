package com.example.ultim.radio5.Genres;

import android.net.Uri;

/**
 * Created by Ultim on 04.06.2017.
 */

public class GenreMessage {
    private int mState;
    private String mTitle;
    private Uri mUri;
    private int mCurrentPlay;

    public GenreMessage(int state, String title, Uri uri, int currentPlay) {
        this.mState = state;
        this.mTitle = title;
        this.mUri = uri;
        this.mCurrentPlay = currentPlay;
    }

    public GenreMessage() {
        this.mState = 0;
        this.mTitle = "";
        this.mUri = null;
        this.mCurrentPlay = 0;
    }

    public int getState() {
        return mState;
    }
    public int getPlay() {
        return mCurrentPlay;
    }
    public String getGenreName() {
        return mTitle;
    }
}
