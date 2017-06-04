package com.example.ultim.radio5;

import android.net.Uri;

/**
 * Created by Ultim on 16.05.2017.
 */

public interface IRadioPlayer {


    void start(String source);
    void pause();
    void stop();
    boolean isPlaying();
    void play(String source, String title);
    void play(Uri source, String title);
    int getState();
    void setState(int state);

}
