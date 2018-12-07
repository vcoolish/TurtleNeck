package com.example.vcoolish.turtleneck;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    Intent intent1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SharedPreferences sp = getSharedPreferences("MODE", MODE_PRIVATE);
        intent1 = new Intent(this, AlrmActivity.class);

        Button btn1 = findViewById(R.id.button1);
        Button btn2 = findViewById(R.id.button2);
        Button btn3 = findViewById(R.id.button3);
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isMyServiceRunning(AlrmActivity.class)) {
                    sp.edit().putInt("flag", 1).apply();
//                    try {
//                        // Initiate DevicePolicyManager.
//                        DevicePolicyManager policyMgr = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
//
//                        // Set DeviceAdminDemo Receiver for active the component with different option
//                        ComponentName componentName = new ComponentName(getApplicationContext(), DeviceAdminComponent.class);
//
//                        if (!policyMgr.isAdminActive(componentName)) {
//                            // try to become active
//                            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
//                            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
//                            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                                    "Click on Activate button to protect your application from uninstalling!");
//                            startActivity(intent);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

                    startService(intent1);
                }
                else
                    sp.edit().putInt("flag", 1).apply();
            }

        });
        btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isMyServiceRunning(AlrmActivity.class)) {
                    sp.edit().putInt("flag", 2).apply();

//                    try {
//                        // Initiate DevicePolicyManager.
//                        DevicePolicyManager policyMgr = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
//
//                        // Set DeviceAdminDemo Receiver for active the component with different option
//                        ComponentName componentName = new ComponentName(getApplicationContext(), DeviceAdminComponent.class);
//
//                        if (!policyMgr.isAdminActive(componentName)) {
//                            // try to become active
//                            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
//                            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
//                            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                                    "Click on Activate button to protect your application from uninstalling!");
//                            startActivity(intent);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

                    startService(intent1);
                }
                else
                    sp.edit().putInt("flag", 2).apply();
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                if(isMyServiceRunning(AlrmActivity.class)) {
//                    stopService(intent1);
                    sp.edit().putInt("flag", 3).apply();
//                }
            }
        });
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
}
