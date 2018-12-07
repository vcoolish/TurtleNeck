package com.example.vcoolish.turtleneck

import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.Toast

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import kotlinx.android.synthetic.main.activity_permissions.*
import android.support.v4.app.NotificationManagerCompat






class PermissionsActivity : AppCompatActivity() {
    val PERMISSION_REQUEST_CODE = 200
    val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)
        bt_toggle.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (bt_toggle.isChecked) {
                    mBluetoothAdapter.enable()
                } else {
                    mBluetoothAdapter.disable()
                }
            }
        })
        location_toggle.setOnClickListener {
                if (!checkPermission()) {
                    requestPermission()
                }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
            notification_toggle.setVisibility(View.GONE)

        notification_toggle.setOnClickListener {

                var weHaveNotificationListenerPermission = false
                for (service in NotificationManagerCompat.getEnabledListenerPackages(this)) {
                    if (service == packageName)
                        weHaveNotificationListenerPermission = true
                }
                if (!weHaveNotificationListenerPermission) {        //ask for permission
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    startActivity(intent)
                }

        }
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, ACCESS_FINE_LOCATION)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {

        ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty()) {

                val locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                if (locationAccepted )
                    Toast.makeText(applicationContext, "Permission Granted, Now you can access location data, bt and notification policy.", Toast.LENGTH_LONG).show()
                else {

                    Toast.makeText(applicationContext, "Permission Denied, You cannot access location data, bt and notification policy.", Toast.LENGTH_LONG).show()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                            showMessageOKCancel("You need to allow access to all the permissions",
                                    DialogInterface.OnClickListener { dialog, which ->
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(arrayOf(ACCESS_FINE_LOCATION),
                                                    PERMISSION_REQUEST_CODE)
                                        }
                                    })
                            return
                        }
                    }

                }
            }
        }
    }
    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@PermissionsActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }
}
