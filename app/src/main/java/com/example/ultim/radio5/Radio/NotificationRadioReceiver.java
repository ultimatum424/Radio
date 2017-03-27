package com.example.ultim.radio5.Radio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.ultim.radio5.AppConstant;

public class NotificationRadioReceiver extends BroadcastReceiver {

    public static NotificationRadioReceiverListener notificationRadioReceiverListener;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
         if (AppConstant.STOP_ACTION.equals(action)){

         }
         if (notificationRadioReceiverListener != null){
             notificationRadioReceiverListener.NotificationListener(action);
         }
    }

    public interface NotificationRadioReceiverListener{
        void NotificationListener(String action);
    }
}
