package com.example.ultim.radio5.Radio;

import android.app.Application;

/**
 * Created by Ultim on 20.03.2017.
 */

public class MyApplication extends Application {

    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    public void setNotificationRadioLister(NotificationRadioReceiver.NotificationRadioReceiverListener listener){
        NotificationRadioReceiver.notificationRadioReceiverListener = listener;
    }
}
