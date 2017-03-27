package com.example.ultim.radio5.Radio;

/**
 * Created by Ultim on 22.03.2017.
 */

public interface MusicFocusable {
    public void onGainedAudioFocus();
    public void onLostAudioFocus(boolean canDuck);
}
