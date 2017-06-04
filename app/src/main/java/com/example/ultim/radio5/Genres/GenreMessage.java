package com.example.ultim.radio5.Genres;

import android.net.Uri;

/**
 * Created by Ultim on 04.06.2017.
 */

public class GenreMessage {
    private int mState;
    private String mTitle;
    private Uri mUri;

    public GenreMessage(int state, String title, Uri uri) {
        this.mState = state;
        this.mTitle = title;
        this.mUri = uri;
    }

    public GenreMessage() {
        this.mState = 0;
        this.mTitle = "";
        this.mUri = null;
    }

    public int getState() {
        return mState;
    }
    public String getGenreName() {
        return mTitle;
    }
}
