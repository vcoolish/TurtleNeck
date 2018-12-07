package com.example.vcoolish.turtleneck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.JobIntentService;

public class ServiceStarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final SharedPreferences sp = context.getSharedPreferences("MODE", context.MODE_PRIVATE);
        if(sp.getInt("flag",1)!=3) {
            Intent serviceLauncher = new Intent(context, AlrmActivity.class);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceLauncher);
            } else {
                context.startService(serviceLauncher);
            }
        }
    }
}

