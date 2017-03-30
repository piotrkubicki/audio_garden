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
                updateNotification(1);
            } else if (intent.getAction().equals(Constants.ACTION.STOP_ACTION)) {
                updateNotification(0);
                Intent i = new Intent(Constants.ACTION.STOP_ACTION);
                sendBroadcast(i);
            } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
                updateNotification(1);
                Intent i = new Intent(Constants.ACTION.PLAY_ACTION);
                sendBroadcast(i);
            } else if (intent.getAction().equals(Constants.ACTION.PAUSE_ACTION)) {
                updateNotification(0);
                Intent i = new Intent(Constants.ACTION.PAUSE_ACTION);
                sendBroadcast(i);
            } else if (intent.getAction().equals(Constants.ACTION.RESET_ACTION)) {
                updateNotification(1);
                Intent i = new Intent(Constants.ACTION.RESET_ACTION);
                sendBroadcast(i);
            } else if (intent.getAction().equals( Constants.ACTION.STOPFOREGROUND_ACTION)) {
                stopForeground(true);
                stopSelf();
            }
        return START_STICKY;
    }
    Notification status;

    //Update the notification and show either play or pause button
    private void updateNotification(int playPause) {
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

        Intent playIntent = new Intent(this, NotificationService.class);
        if(playPause == 1) {
            playIntent.setAction(Constants.ACTION.PAUSE_ACTION);
        } else if (playPause == 0) {
            playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        }
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent stopIntent = new Intent(this, NotificationService.class);
        stopIntent.setAction(Constants.ACTION.STOP_ACTION);
        PendingIntent pstopIntent = PendingIntent.getService(this, 0,
                stopIntent, 0);;

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_stop, pstopIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_stop, pstopIntent);

        views.setOnClickPendingIntent(R.id.status_bar_reset, presetIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_reset, presetIntent);

        if(playPause == 1) {
            views.setImageViewResource(R.id.status_bar_play,
                    R.drawable.ic_pause);
            bigViews.setImageViewResource(R.id.status_bar_play,
                    R.drawable.ic_pause);
        } else if (playPause == 0){
            views.setImageViewResource(R.id.status_bar_play,
                    R.drawable.ic_play);
            bigViews.setImageViewResource(R.id.status_bar_play,
                    R.drawable.ic_play);
        }

        int currentapiVersion = Build.VERSION.SDK_INT;

        if (currentapiVersion < Build.VERSION_CODES.N)
        {
            status = new Notification.Builder(this).build();
            status.contentView = views;
            status.bigContentView = bigViews;
            status.flags = Notification.FLAG_ONGOING_EVENT;
            status.icon = R.drawable.ic_napier;
            status.contentIntent = pendingIntent;

        } else {
            notificationBuilder(views,bigViews,pendingIntent);
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

