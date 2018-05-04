package com.example.vcoolish.turtleneck;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.UUID;


public class AlrmActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {

    public final static UUID HT_SERVICE_UUID = UUID.fromString("00001523-1212-efde-1523-785feabcd123");
    final static int RQS_1 = 1;
    private static final String TAG = "MainActivity";
    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID HT_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00001524-1212-efde-1523-785feabcd123");
    BluetoothGatt mConnectedGatt;
    Handler mHandler;
    TextView hello;
    private BluetoothAdapter mBluetoothAdapter;
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };
    //    @Override
//    protected void onResume() {
//        super.onResume();
//        /*
//         * We need to enforce that Bluetooth is first enabled, and take the
//         * user to settings to enable it if they have not done so.
//         */
//        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
//            //Bluetooth is disabled
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivity(enableBtIntent);
//            finish();
//            return;
//        }
//
//        /*
//         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
//         * from installing on these devices, but this will allow test devices or other
//         * sideloads to report whether or not the feature exists.
//         */
//        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        //Begin scanning for LE devices
//        startScan();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        //Make sure dialog is hidden
//        //mProgress.dismiss();
//        //Cancel any scans in progress
//        mHandler.removeCallbacks(mStopRunnable);
//        mHandler.removeCallbacks(mStartRunnable);
//        mBluetoothAdapter.stopLeScan(this);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        //Disconnect from any active tag connection
//        if (mConnectedGatt != null) {
//            mConnectedGatt.close();
//            mConnectedGatt.disconnect();
//            mConnectedGatt = null;
//        }
//    }
    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /* State Machine Tracking */
        private int mState = 0;

        private void reset() {
            mState = 0;
        }

        private void advance() {
            mState++;
        }

        private String connectionState(int status) {
            switch (status) {
                case BluetoothProfile.STATE_CONNECTED:
                    return "Connected";
                case BluetoothProfile.STATE_DISCONNECTED:
                    return "Disconnected";
                case BluetoothProfile.STATE_CONNECTING:
                    return "Connecting";
                case BluetoothProfile.STATE_DISCONNECTING:
                    return "Disconnecting";
                default:
                    return String.valueOf(status);
            }
        }


        private void enableNextSensor(BluetoothGatt gatt) {
            Log.i(TAG, "******************************************************************enableNextSensor");
            BluetoothGattCharacteristic characteristic;
            characteristic = gatt.getService(HT_SERVICE_UUID)
                    .getCharacteristic(HT_MEASUREMENT_CHARACTERISTIC_UUID);
            // Check characteristic property
            final int properties = characteristic.getProperties();

//            if ((properties | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                Log.i(TAG,"**************************READ");
//                // If there is an active notification on a characteristic, clear
//                // it first so it doesn't update the data field on the user interface.
//                /*if (mNotifyCharacteristic != null) {
//                    mBluetoothLeService.setCharacteristicNotification(
//                            mNotifyCharacteristic, false);
//                    mNotifyCharacteristic = null;
//                }
//                mBluetoothLeService.readCharacteristic(characteristic);*/
//            }
            if ((properties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                Log.i(TAG, "**************************NOTIFY");
                //mNotifyCharacteristic = characteristic;
                gatt.setCharacteristicNotification(
                        characteristic, true);

                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                        CONFIG_DESCRIPTOR);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "*********************************************************onCharacteristicWrite****************************");
            //After writing the enable flag, next we read the initial value
//            readNextSensor(gatt);
        }

        /*
         * Read the data characteristic's value for each sensor explicitly
         */
//        private void readNextSensor(BluetoothGatt gatt) {
//            BluetoothGattCharacteristic characteristic;
//            characteristic = gatt.getService(HT_SERVICE_UUID)
//                    .getCharacteristic(HT_MEASUREMENT_CHARACTERISTIC_UUID);
//            gatt.readCharacteristic(characteristic);
//        }

//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            //For each read, pass the data up to the UI thread to update the display
//            if (HT_MEASUREMENT_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
//                //mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
//                //  updateTemperatureValue(characteristic);
//                Log.i(TAG,"*********************************************************onCharacteristicRead****************************");
//            }
//            setAlarm();
//            setNotifyNextSensor(gatt);
//        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //Once notifications are enabled, we move to the next sensor and start over with enable
            advance();
//            enableNextSensor(gatt);
        }

        private void setNotifyNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            characteristic = gatt.getService(HT_SERVICE_UUID)
                    .getCharacteristic(HT_MEASUREMENT_CHARACTERISTIC_UUID);
            Log.i(TAG, "******************setNotify");

            //Enable local notifications
            gatt.setCharacteristicNotification(characteristic, true);
            //Enabled remote notifications
            BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(desc);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "******************************************************************Connetion State change =>" + status + "<= " + connectionState(newState));
            Log.d(TAG, "******************************************************************Gatt success =>" + BluetoothGatt.GATT_SUCCESS + "<= ");
            Log.d(TAG, "******************************************************************Connetion State connect =>" + BluetoothProfile.STATE_CONNECTED + "<= ");
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                //hello.setText("Device Connected");
                Log.d(TAG, "***********************GATT_SUCCESS");
                /*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 */
                gatt.discoverServices();

            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                //hello.setText("Gatt Disconnected");
                /*
                 * If there is a failure at any stage, simply disconnect
                 */
                gatt.close();
                gatt.disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "Services Discovered: " + status);

            //hello.setText("Services Discovered");

            //if(status == BluetoothGatt.GATT_SUCCESS)
            //mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Enabling Sensors..."));
            /*
             * With services discovered, we are going to reset our state machine and start
             * working through the sensors we need to enable
             */
            reset();
            enableNextSensor(gatt);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            Log.i(TAG, "*********************************************************onCharacteristicChanged**** I am here************************");
            /*
             * After notifications are enabled, all updates from the device on characteristic
             * value changes will be posted here.  Similar to read, we hand these up to the
             * UI thread to update the display.
             */
            setAlarm();
            if (HT_MEASUREMENT_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                // mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
                Log.i(TAG, "*********************************************************onCharacteristicChanged****************************");
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_checker);
        mHandler = new Handler();
        hello = findViewById(R.id.temp);

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        startScan();
    }

    private void setAlarm() {
        Intent intent = new Intent(getBaseContext(), MyAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), RQS_1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 300, pendingIntent);

    }

    private void startScan() {
        mBluetoothAdapter.startLeScan(new UUID[]{HT_SERVICE_UUID}, this);
        mHandler.postDelayed(mStopRunnable, 30000);
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
        mHandler.postDelayed(mStartRunnable, 27500);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);

        if (device.getName() != null && device.getName().equals("MetaMotionR custom_")) {
            Log.i(TAG, "*******Inside connectGatt" + device.getName());

            mConnectedGatt = device.connectGatt(this, false, mGattCallback);
        }

    }
}




