package com.tamboraagungmakmur.realtimetest.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.tamboraagungmakmur.realtimetest.R;

/**
 * Created by Tambora on 30/11/2016.
 */
public class NotificationContent {

    public static void push(Context context, int notificationId, String title, String message, Class activity) {

        Intent intent = new Intent(context, activity);
        intent.putExtra(AppConf.USERNAME_PREF, title);
        intent.putExtra(context.getString(R.string.message), message);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(message);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
        //notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());

    }

}
