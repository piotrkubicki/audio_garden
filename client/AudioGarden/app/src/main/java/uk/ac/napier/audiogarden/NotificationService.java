package uk.ac.napier.audiogarden;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.app.Notification;
import android.app.PendingIntent;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Created by Nathan on 30-Mar-17.
 */

public class NotificationService extends Service {
    private int PAUSE = 0;
    private int PLAY = 1;
    private int STOP = 0;
    private int REPLAY = 1;
    private int ENABLE = 0;
    private int DISABLE = 1;
    private int playPause = PAUSE;
    private int stopReplay = STOP;
    private int playPauseEnabled = ENABLE;
    private int stopReplayEnabled = DISABLE;

    public void onDestroy() {
        super.onDestroy();
    }

    public IBinder onBind(Intent intent)
    {

        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
                updateNotification();

            } else if (intent.getAction().equals(Constants.ACTION.STOP_ACTION)) {
                stopReplay = REPLAY;
                updateNotification();
                Intent i = new Intent(Constants.ACTION.STOP_ACTION);
                sendBroadcast(i);

            } else if (intent.getAction().equals(Constants.ACTION.REPLAY_ACTION)) {
                stopReplay = STOP;
                updateNotification();
                Intent i = new Intent(Constants.ACTION.REPLAY_ACTION);
                sendBroadcast(i);

            } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
                playPause = PAUSE;
                stopReplay = STOP;
                updateNotification();
                Intent i = new Intent(Constants.ACTION.PLAY_ACTION);
                sendBroadcast(i);

            } else if (intent.getAction().equals(Constants.ACTION.PAUSE_ACTION)) {
                playPause = PLAY;
                stopReplay = REPLAY;
                updateNotification();
                Intent i = new Intent(Constants.ACTION.PAUSE_ACTION);
                sendBroadcast(i);

            } else if (intent.getAction().equals(Constants.ACTION.RESET_ACTION)) {
                updateNotification();
                Intent i = new Intent(Constants.ACTION.RESET_ACTION);
                sendBroadcast(i);

            } else if (intent.getAction().equals( Constants.ACTION.STOPFOREGROUND_ACTION)) {
                stopForeground(true);
                stopSelf();

            } else if (intent.getAction().equals( Constants.ACTION.SEND_STOP_ACTION)) {
                stopReplay = REPLAY;
                updateNotification();

            } else if (intent.getAction().equals( Constants.ACTION.SEND_REPLAY_ACTION)) {
                stopReplay = STOP;
                updateNotification();

            } else if (intent.getAction().equals( Constants.ACTION.SEND_PLAY_ACTION)) {
                playPause = PAUSE;
                stopReplay = STOP;
                updateNotification();

            } else if (intent.getAction().equals( Constants.ACTION.SEND_PAUSE_ACTION)) {
                playPause = PLAY;
                stopReplay = REPLAY;
                updateNotification();

            } else if (intent.getAction().equals( Constants.ACTION.SEND_RESET_ACTION)) {
                updateNotification();

            } else if (intent.getAction().equals( Constants.ACTION.DISABLE_PLAY_PAUSE)) {
                playPauseEnabled = DISABLE;
                updateNotification();

            } else if (intent.getAction().equals( Constants.ACTION.DISABLE_STOP_REPLAY)) {
                stopReplayEnabled = DISABLE;
                updateNotification();

            } else if (intent.getAction().equals( Constants.ACTION.ENABLE_PLAY_PAUSE)) {
                playPauseEnabled = ENABLE;
                updateNotification();

            } else if (intent.getAction().equals( Constants.ACTION.ENABLE_STOP_REPLAY)) {
                stopReplayEnabled = ENABLE;
                updateNotification();
            }
        return START_STICKY;
    }
    Notification status;

    //Update the notification and show either play or pause button
    private void updateNotification() {

        // Using RemoteViews to bind custom layouts into Notification
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);
        // showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE);
        bigViews.setImageViewBitmap(R.id.status_bar_album_art,
                Constants.getDefaultAlbumArt(this));

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent resetIntent = new Intent(this, NotificationService.class);
        resetIntent.setAction(Constants.ACTION.RESET_ACTION);
        PendingIntent presetIntent = PendingIntent.getService(this, 0,
                resetIntent, 0);

        //check if play/pause button is enabled then check whether it should be played or paused
        PendingIntent pplayIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
        if (playPauseEnabled == ENABLE) {
            Intent playIntent = new Intent(this, NotificationService.class);
            if (playPause == PAUSE) {
                playIntent.setAction(Constants.ACTION.PAUSE_ACTION);
            } else if (playPause == PLAY) {
                playIntent.setAction(Constants.ACTION.PLAY_ACTION);
            }
            pplayIntent = PendingIntent.getService(this, 0,
                    playIntent, 0);

        } else if (playPauseEnabled == DISABLE) {
            Intent playIntent = new Intent(this, NotificationService.class);
            if (playPause == PAUSE) {
                playIntent.setAction(Constants.ACTION.BLANK_ACTION);
            } else if (playPause == PLAY) {
                playIntent.setAction(Constants.ACTION.BLANK_ACTION);
            }
            pplayIntent = PendingIntent.getService(this, 0,
                    playIntent, 0);
        }

        //check if stop/replay button is enabled then check whether it should be stopped or replayed
        PendingIntent pstopIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
        if (stopReplayEnabled == ENABLE) {
            Intent stopIntent = new Intent(this, NotificationService.class);
            if (stopReplay == STOP) {
                stopIntent.setAction(Constants.ACTION.STOP_ACTION);
            } else if (stopReplay == REPLAY) {
                stopIntent.setAction(Constants.ACTION.REPLAY_ACTION);
            }
            pstopIntent = PendingIntent.getService(this, 0,
                    stopIntent, 0);
        } else if (playPauseEnabled == DISABLE) {
            Intent stopIntent = new Intent(this, NotificationService.class);
            if (stopReplay == STOP) {
                stopIntent.setAction(Constants.ACTION.BLANK_ACTION);
            } else if (stopReplay == REPLAY) {
                stopIntent.setAction(Constants.ACTION.BLANK_ACTION);
            }
            pstopIntent = PendingIntent.getService(this, 0,
                    stopIntent, 0);
        }

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_stop, pstopIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_stop, pstopIntent);

        views.setOnClickPendingIntent(R.id.status_bar_reset, presetIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_reset, presetIntent);

        //check if play/pause button is enabled and set corresponding image for button
        if (playPauseEnabled == ENABLE) {
            if (playPause == PAUSE) {
                views.setImageViewResource(R.id.status_bar_play,
                        R.drawable.ic_pause);
                bigViews.setImageViewResource(R.id.status_bar_play,
                        R.drawable.ic_pause);
            } else if (playPause == PLAY) {
                views.setImageViewResource(R.id.status_bar_play,
                        R.drawable.ic_play);
                bigViews.setImageViewResource(R.id.status_bar_play,
                        R.drawable.ic_play);
            }
        } else if (playPauseEnabled == DISABLE) {
            if (playPause == PAUSE) {
                views.setImageViewResource(R.id.status_bar_play,
                        R.drawable.ic_pause_disabled);
                bigViews.setImageViewResource(R.id.status_bar_play,
                        R.drawable.ic_pause_disabled);
            } else if (playPause == PLAY) {
                views.setImageViewResource(R.id.status_bar_play,
                        R.drawable.ic_play_disabled);
                bigViews.setImageViewResource(R.id.status_bar_play,
                        R.drawable.ic_play_disabled);
            }
        }

        //check if stop/replay button is enabled and set corresponding image for button
        if (stopReplayEnabled == ENABLE) {
            if (stopReplay == STOP) {
                views.setImageViewResource(R.id.status_bar_stop,
                        R.drawable.ic_stop);
                bigViews.setImageViewResource(R.id.status_bar_stop,
                        R.drawable.ic_stop);
            } else if (stopReplay == REPLAY) {
                views.setImageViewResource(R.id.status_bar_stop,
                        R.drawable.ic_replay);
                bigViews.setImageViewResource(R.id.status_bar_stop,
                        R.drawable.ic_replay);
            }
        } else if (stopReplayEnabled == DISABLE){
            if (stopReplay == STOP) {
                views.setImageViewResource(R.id.status_bar_stop,
                        R.drawable.ic_stop_disabled);
                bigViews.setImageViewResource(R.id.status_bar_stop,
                        R.drawable.ic_stop_disabled);
            } else if (stopReplay == REPLAY) {
                views.setImageViewResource(R.id.status_bar_stop,
                        R.drawable.ic_replay_disabled);
                bigViews.setImageViewResource(R.id.status_bar_stop,
                        R.drawable.ic_replay_disabled);
            }
        }

            int currentapiVersion = Build.VERSION.SDK_INT;

            if (currentapiVersion < Build.VERSION_CODES.N) {
                status = new Notification.Builder(this).build();
                status.contentView = views;
                status.bigContentView = bigViews;
                status.flags = Notification.FLAG_ONGOING_EVENT;
                status.icon = R.drawable.ic_napier;
                status.contentIntent = pendingIntent;

            } else {
                notificationBuilder(views, bigViews, pendingIntent);
            }
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);


    }
    //Avoids deprecated methods for api 24 and above
    @TargetApi(24)
    public void notificationBuilder(RemoteViews views, RemoteViews bigViews, PendingIntent pendingIntent) {
        status = new Notification.Builder(this)
                .setCustomContentView(views)
                .setCustomBigContentView(bigViews)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_napier)
                .setContentIntent(pendingIntent)
        .build();
    }
}

