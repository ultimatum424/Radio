package com.example.ultim.radio5;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Rating;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.MediaController;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

/**
 * Created by Ultim on 15.05.2017.
 */

public class MediaPlayerService extends Service {

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_SELECT = "action_select";

    private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;
    private MediaSessionCompat mSession;
    private MediaControllerCompat mController;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent( Intent intent ) {
        if( intent == null || intent.getAction() == null )
            return;

        String action = intent.getAction();

        if( action.equalsIgnoreCase( ACTION_PLAY ) ) {
            mController.getTransportControls().play();
        } else if( action.equalsIgnoreCase( ACTION_PAUSE ) ) {
            mController.getTransportControls().pause();
        } else if( action.equalsIgnoreCase( ACTION_FAST_FORWARD ) ) {
            mController.getTransportControls().fastForward();
        } else if( action.equalsIgnoreCase( ACTION_REWIND ) ) {
            mController.getTransportControls().rewind();
        } else if( action.equalsIgnoreCase( ACTION_PREVIOUS ) ) {
            mController.getTransportControls().skipToPrevious();
        } else if( action.equalsIgnoreCase( ACTION_NEXT ) ) {
            mController.getTransportControls().skipToNext();
        } else if( action.equalsIgnoreCase( ACTION_STOP ) ) {
            mController.getTransportControls().stop();
        } else if (action.equalsIgnoreCase(ACTION_SELECT)) {
            mController.getTransportControls().sendCustomAction(ACTION_SELECT, new Bundle());
        }
    }

    private Notification.Action generateAction(int icon, String title, String intentAction ) {
        Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
        intent.setAction( intentAction );
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder( icon, title, pendingIntent ).build();
    }

    private void buildNotification( Notification.Action action ) {
        Notification.MediaStyle style = new Notification.MediaStyle();

        Intent selectIntent = new Intent( getApplicationContext(), MediaPlayerService.class );
        selectIntent.setAction( ACTION_SELECT );
        PendingIntent pendingSelectIntent = PendingIntent.getService(getApplicationContext(), 1, selectIntent, 0);

        Notification.Builder builder = new Notification.Builder( this )
                .setSmallIcon(R.drawable.ic_radio_black_24dp)
                .setContentTitle( "Media Title" )
                .setContentText( "Media Artist" )
               // .setDeleteIntent( pendingIntent )
                .setAutoCancel(false)
                .setContentIntent(pendingSelectIntent)
                .setShowWhen(false)
                .setStyle(style);

        builder.addAction( action );
        builder.addAction( generateAction( android.R.drawable.ic_notification_clear_all, "Stop", ACTION_STOP ) );
        style.setShowActionsInCompactView(0, 1);
        NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificationManager.notify( 1, builder.build() );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if( mManager == null ) {
            initMediaSessions();
        }

        handleIntent( intent );
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMediaSessions() {
        mMediaPlayer = new MediaPlayer();

        mSession = new MediaSessionCompat(getApplicationContext(), "simple player session");
        try {
            mController = new MediaControllerCompat(getApplicationContext(), mSession.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mSession.setCallback(new MediaSessionCompat.Callback(){
                                 @Override
                                 public void onPlay() {
                                     super.onPlay();
                                     Log.e( "MediaPlayerService", "onPlay");
                                     buildNotification( generateAction( android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE ) );
                                 }

                                 @Override
                                 public void onPause() {
                                     super.onPause();
                                     Log.e( "MediaPlayerService", "onPause");
                                     buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
                                 }

                                 @Override
                                 public void onCustomAction(String action, Bundle extras) {
                                     super.onCustomAction(action, extras);

                                     startActivity(new Intent( getApplicationContext(), NavigationDrawerActivity.class)
                                             .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                             | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                                 }

                                 @Override
                                 public void onStop() {
                                     super.onStop();
                                     Log.e( "MediaPlayerService", "onStop");
                                     //Stop media player here
                                     NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                     notificationManager.cancel( 1 );
                                     Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
                                     stopService( intent );
                                 }

                                 @Override
                                 public void onSeekTo(long pos) {
                                     super.onSeekTo(pos);
                                 }
                             }
        );
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mSession.release();
        return super.onUnbind(intent);
    }
}
