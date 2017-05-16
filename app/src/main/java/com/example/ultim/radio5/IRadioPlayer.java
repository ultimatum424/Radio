package com.example.ultim.radio5;

/**
 * Created by Ultim on 16.05.2017.
 */

public interface IRadioPlayer {


    void start();
    void pause();
    void stop();
    boolean isPlaying();
    void play();
    int getState();
    void setState(int state);

}
