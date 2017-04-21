package com.example.ultim.radio5;

/**
 * Created by Ultim on 23.03.2017.
 */

public class AppConstant {
    public static final String YES_ACTION = "YES_ACTION";
    public static final String STOP_ACTION = "STOP_ACTION";
    public interface ACTION {
        public static String MAIN_ACTION = "com.radio5.radionotification.action.main";
        public static String PLAY_ACTION = "com.radio5.radionotification.action.play";
        public static String STARTFOREGROUND_ACTION = "com.radio5.radionotification.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.radio5.radionotification.action.stopforeground";

    }
    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 1;
    }
}


