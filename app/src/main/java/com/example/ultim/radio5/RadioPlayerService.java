package com.example.ultim.radio5;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.session.MediaSessionManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Ultim on 16.05.2017.
 */

public class RadioPlayerService extends Service {

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_STOP = "action_stop";

    private MediaControllerCompat mController;
    private MediaSessionCompat mSession;
    private MediaSessionManager mManager;
    private NotificationCompat.Builder mBuild;
    private RadioPlayer mRadioPlayer;

    private String url;
    private String title;




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent(Intent intent){
        if (intent == null || intent.getAction() == null){
            return;
        }
        if (intent.getExtras() != null) {
            url = intent.getStringExtra("url");
            title = intent.getStringExtra("title");
        }
        String action = intent.getAction();

        if( action.equalsIgnoreCase( ACTION_PLAY ) ) {
            mController.getTransportControls().play();
        } else if( action.equalsIgnoreCase( ACTION_PAUSE ) ) {
            mController.getTransportControls().pause();
        } else if( action.equalsIgnoreCase( ACTION_STOP ) ) {
        mController.getTransportControls().stop();
        }
    }


    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent( getApplicationContext(), RadioPlayerService.class );
        intent.setAction( intentAction );
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder( icon, title, pendingIntent ).build();
    }

    private void buildNotification(NotificationCompat.Action action){
        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();
        style.setShowCancelButton(true);
        Intent notificationIntent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
        notificationIntent.setAction(AppConstant.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingSelectIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, 0);

        Bitmap largeIcon = BitmapFactory.decodeResource(getApplication().getResources(),
                R.mipmap.ic_launcher);

        mBuild = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_radio_black_24dp)
                .setContentTitle( title )
                .setContentText( url )
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingSelectIntent)
                .setOngoing(true)
                .setStyle(style);

        mBuild.addAction( action );
        mBuild.addAction(generateAction(R.drawable.ic_close_black_16dp_2x, "Stop", ACTION_STOP));
       style.setShowActionsInCompactView(0, 1);

        @SuppressLint("ServiceCast")
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify( 1, mBuild.build() );

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if( mManager == null ) {
            try {
                initMediaSessions();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (mRadioPlayer == null){
            mRadioPlayer = new RadioPlayer(getApplicationContext());
        }

        handleIntent( intent );
        //return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    private void initMediaSessions() throws RemoteException {
        mSession = new MediaSessionCompat(getApplicationContext(), "simple player session");
        mController = new MediaControllerCompat(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSessionCompat.Callback() {

            @Override
            public void onPlay() {
                super.onPlay();
                Log.e( "MediaPlayerService", "onPlay");
                buildNotification( generateAction( android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE ) );
                mRadioPlayer.play(url, title);
                startForeground(1, mBuild.build());
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.e( "MediaPlayerService", "onPause");
                buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
                mRadioPlayer.pause();
                //stopForeground(false);
            }


            @Override
            public void onStop() {
                super.onStop();
                mRadioPlayer.isPlaying();
                mRadioPlayer.stop();
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel( 1 );
                stopForeground(true);
                Intent intent = new Intent( getApplicationContext(), RadioPlayerService.class );
                stopService( intent );
            }
        });

    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }
}
