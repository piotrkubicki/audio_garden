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
                showPlay();
            } else if (intent.getAction().equals(Constants.ACTION.STOP_ACTION)) {
                showPlay();
                Intent i = new Intent(Constants.ACTION.STOP_ACTION);
                sendBroadcast(i);
            } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
                showPause();
                Intent i = new Intent(Constants.ACTION.PLAY_ACTION);
                sendBroadcast(i);
            } else if (intent.getAction().equals(Constants.ACTION.PAUSE_ACTION)) {
                showPlay();
                Intent i = new Intent(Constants.ACTION.PAUSE_ACTION);
                sendBroadcast(i);
            } else if (intent.getAction().equals(Constants.ACTION.RESET_ACTION)) {
                showPause();
                Intent i = new Intent(Constants.ACTION.RESET_ACTION);
                sendBroadcast(i);
            } else if (intent.getAction().equals( Constants.ACTION.STOPFOREGROUND_ACTION)) {
                stopForeground(true);
                stopSelf();
            }
        return START_STICKY;
    }
    Notification status;

    private void showPlay() {
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

        Intent previousIntent = new Intent(this, NotificationService.class);
        previousIntent.setAction(Constants.ACTION.STOP_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, NotificationService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, NotificationService.class);
        nextIntent.setAction(Constants.ACTION.RESET_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, NotificationService.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_stop, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_stop, pnextIntent);

        views.setOnClickPendingIntent(R.id.status_bar_reset, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_reset, ppreviousIntent);

        views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
        bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);

        views.setTextViewText(R.id.status_bar_track_name, "Song Title");
        bigViews.setTextViewText(R.id.status_bar_track_name, "Song Title");

        views.setTextViewText(R.id.status_bar_artist_name, "Artist Name");
        bigViews.setTextViewText(R.id.status_bar_artist_name, "Artist Name");

        bigViews.setTextViewText(R.id.status_bar_album_name, "Album Name");

        int notifyID = 1;
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

    private void showPause() {
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

        Intent previousIntent = new Intent(this, NotificationService.class);
        previousIntent.setAction(Constants.ACTION.STOP_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, NotificationService.class);
        playIntent.setAction(Constants.ACTION.PAUSE_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, NotificationService.class);
        nextIntent.setAction(Constants.ACTION.RESET_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, NotificationService.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_stop, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_stop, pnextIntent);

        views.setOnClickPendingIntent(R.id.status_bar_reset, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_reset, ppreviousIntent);

        views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_play);
        bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_play);

        views.setTextViewText(R.id.status_bar_track_name, "Song Title");
        bigViews.setTextViewText(R.id.status_bar_track_name, "Song Title");

        views.setTextViewText(R.id.status_bar_artist_name, "Artist Name");
        bigViews.setTextViewText(R.id.status_bar_artist_name, "Artist Name");

        bigViews.setTextViewText(R.id.status_bar_album_name, "Album Name");

        int notifyID = 1;
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

