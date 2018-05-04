package com.example.vcoolish.turtleneck;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyAlarmReceiver extends BroadcastReceiver {

    Vibrator v;
    Context ct;
    String  title;
    String text;


    @Override
    public void onReceive(Context context, Intent intent) {

        // TODO Auto-generated method stub
        ct=context;

        Log.e("onReceive", "ladskjflsakjdflskjdflskjdfslkjdflasdf");
        Toast.makeText(context, "OnReceive alarm test", Toast.LENGTH_SHORT).show();

        v=(Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        try {
            v.vibrate(3000);
        }catch (NullPointerException e){
            Toast.makeText(context,"Vibaration failed", Toast.LENGTH_LONG).show();
        }

        int badgeCount = 1;
        // ShortcutBadger.applyCount(context, badgeCount);

        Notification(context, "Alarm");


    }


    public void Notification(Context context, String message) {
        // Set Notification Title
        title = "Temperature warning";
        text = "Check your medicine";
        // Open NotificationView Class on Notification Click
        Intent intent = new Intent(context, AlrmActivity.class);
        // Send data to NotificationView Class
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        // Open NotificationView.java Activity
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.app_icon, "Previous", pIntent).build();
        // Create Notification using NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context)
                // Set Icon
                .setSmallIcon(R.drawable.icon)
                // Set Ticker Message
                .setTicker(message)
                // Set Title
                .setContentTitle(context.getString(R.string.app_name))
                // Set Text
                .setContentText(message)
                // Add an Action Button below Notification
                // Set PendingIntent into Notification
                .setContentIntent(pIntent)
                // Dismiss Notification
                .setAutoCancel(true);

        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(38, builder.build());

    }
}