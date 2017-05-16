package com.example.ultim.radio5;

/**
 * Created by Ultim on 16.05.2017.
 */

public interface IRadioPlayer {


    void start(String source);
    void pause();
    void stop();
    boolean isPlaying();
    void play(String source);
    int getState();
    void setState(int state);

}
