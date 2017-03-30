package com.example.ultim.radio5.Radio;

/**
 * Created by Ultim on 30.03.2017.
 */

enum AudioFocus {
    NoFocusNoDuck,    // we don't have audio focus, and can't duck
    NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
    Focused           // we have full audio focus
}
