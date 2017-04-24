package com.example.ultim.radio5.Pojo;

/**
 * Created by Ultim on 30.03.2017.
 */

public class RadioStateEvent {

    private final SateEnum sateEnum;

    public RadioStateEvent(SateEnum sateEnum) {
        this.sateEnum = sateEnum;
    }

    public SateEnum getSateEnum() {
        return sateEnum;
    }

    public enum SateEnum{
        STOP,
        PAUSE,
        PLAY,
        BUFFERING,
        OFFLINE
    }
}
