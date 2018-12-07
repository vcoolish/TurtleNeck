package com.example.vcoolish.turtleneck;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;

import java.util.List;
import java.util.UUID;


public class AlrmActivity extends Service implements BluetoothAdapter.LeScanCallback {
    public final static UUID HT_SERVICE_UUID = UUID.fromString("00001523-1212-efde-1523-785feabcd123");
    public final static UUID IBEACON_UUID = UUID.fromString("01122334-4556-6778-899a-aabbccddeeff0");
    final static int RQS_1 = 1;
    private static final int FIRST_RUN_TIMEOUT_MILISEC = 5 * 1000;
    private static final int SERVICE_STARTER_INTERVAL_MILISEC = 30 * 1000;
    private static final int SERVICE_TASK_TIMEOUT_SEC = 30;
    private static final String TAG = "MainActivity";
    //    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID HT_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00001525-1212-efde-1523-785feabcd123");
    private final int REQUEST_CODE = 1;
    public int flag = 3;
    BluetoothGatt mConnectedGatt;
    Handler mHandler;
    private AlarmManager serviceStarterAlarmManager = null;
//    private MyTask asyncTask = null;
    private BluetoothAdapter mBluetoothAdapter;
    //
    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
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

            if ((properties | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                Log.i(TAG, "**************************READ");
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                /*if (mNotifyCharacteristic != null) {
                    mBluetoothLeService.setCharacteristicNotification(
                            mNotifyCharacteristic, false);
                    mNotifyCharacteristic = null;
                }*/
                readNextSensor(gatt);
            }
            if ((properties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                Log.i(TAG, "**************************NOTIFY");
                //mNotifyCharacteristic = characteristic;
//                gatt.setCharacteristicNotification(
//                        characteristic, true);

//                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                        CONFIG_DESCRIPTOR);
//                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                gatt.writeDescriptor(descriptor);
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "*********************************************************onCharacteristicWrite****************************");
            //After writing the enable flag, next we read the initial value
            readNextSensor(gatt);
        }

        private void readNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            characteristic = gatt.getService(HT_SERVICE_UUID)
                    .getCharacteristic(HT_MEASUREMENT_CHARACTERISTIC_UUID);
            gatt.readCharacteristic(characteristic);
            if (characteristic.getValue() == new byte[]{(byte) 01}) {
                BluetoothGattCharacteristic ch = gatt.getService(HT_SERVICE_UUID)
                        .getCharacteristic(HT_MEASUREMENT_CHARACTERISTIC_UUID);
                ch.setValue(new byte[]{(byte) 0});
                gatt.writeCharacteristic(ch);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //For each read, pass the data up to the UI thread to update the display
            if (HT_MEASUREMENT_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                //mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
                //  updateTemperatureValue(characteristic);
                Log.i(TAG, "*********************************************************onCharacteristicRead****************************");
            }
            System.out.println(characteristic.getValue()[0]);

            if (characteristic.getValue()[0] == 1) {
                characteristic.setValue(new byte[]{(byte) 0});
                setAlarm();
                mConnectedGatt.writeCharacteristic(characteristic);
            }
            gatt.close();
            gatt.disconnect();

//            setNotifyNextSensor(gatt);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //Once notifications are enabled, we move to the next sensor and start over with enable
            advance();
            enableNextSensor(gatt);
        }

        private void setNotifyNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            characteristic = gatt.getService(HT_SERVICE_UUID)
                    .getCharacteristic(HT_MEASUREMENT_CHARACTERISTIC_UUID);
            Log.i(TAG, "******************setNotify");

            //Enable local notifications
            gatt.setCharacteristicNotification(characteristic, true);

            //Enabled remote notifications
//            BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
//            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            gatt.writeDescriptor(desc);
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
            readNextSensor(gatt);
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
            readNextSensor(gatt);
        }
    };

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_battery_checker);
//        mHandler = new Handler();
//        hello = findViewById(R.id.temp);
//
//        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
//        mBluetoothAdapter = manager.getAdapter();
//        startScan();
//    }

    private void setAlarm() {
        Intent intent = new Intent(getBaseContext(), MyAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), RQS_1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 300, pendingIntent);

    }

    private void startScan() {
        if (flag==2)
            mBluetoothAdapter.startLeScan(this);
        if(flag==1)
            mBluetoothAdapter.startLeScan(new UUID[]{HT_SERVICE_UUID}, this);
        mHandler.postDelayed(mStopRunnable, 3000);
        if (mConnectedGatt != null) {
            try {
                BluetoothGattCharacteristic characteristic = mConnectedGatt.getService(HT_SERVICE_UUID)
                        .getCharacteristic(HT_MEASUREMENT_CHARACTERISTIC_UUID);
                mConnectedGatt.readCharacteristic(characteristic);
            } catch (NullPointerException e) {
                System.out.println("NullPointerEx");
            }
        }
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
        mHandler.postDelayed(mStartRunnable, 30000);

    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
        flag=getSharedPreferences("MODE", MODE_PRIVATE).getInt("flag",3);
        if (flag==1) {
            if (device.getName() != null && device.getName().equals("MetaMotionR custom0")) {
                Log.i(TAG, "*******Inside connectGatt" + device.getName());

                mConnectedGatt = device.connectGatt(this, false, mGattCallback);

            }
        }
        if(flag==2) {
            List<ADStructure> structures =
                    ADPayloadParser.getInstance().parse(scanRecord);

            // For each AD structure contained in the advertising packet.
            for (ADStructure structure : structures) {
                if (structure instanceof IBeacon) {
                    // iBeacon was found.
                    IBeacon iBeacon = (IBeacon) structure;

                    // Proximity UUID, major number, minor number and power.
                    UUID uuid = iBeacon.getUUID();
                    int minor = iBeacon.getMinor();
                    System.out.println(uuid);
                    System.out.println(minor);
                    if (uuid.equals(IBEACON_UUID) && minor == 1) {
                        setAlarm();
                        stopScan();
                    }
                }
            }
        }
//        if(flag==3){
//            if (Build.VERSION.SDK_INT >= 26) {
//                stopForeground(true);
//            }
//            stopSelf();
//            onDestroy();
//        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final SharedPreferences sp = getSharedPreferences("MODE", MODE_PRIVATE);
        flag = sp.getInt("flag",3);
        final Intent intent1 = new Intent(this, AlrmActivity.class);
        final PendingIntent pending = PendingIntent.getService(this, 0, intent1, 0);
        final AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if(flag!=0) {
            mHandler = new Handler();

            BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            mBluetoothAdapter = manager.getAdapter();
            startScan();

            // Start of timeout-autostarter for our service (watchdog)
//            startServiceStarter();

            if (Build.VERSION.SDK_INT >= 26) {

                Context context = getBaseContext();
                String message = "Alarm";
                // Set Notification Title
                String title = "Temperature warning";
                String text = "Check your medicine";
                // Open NotificationView Class on Notification Click
                Intent intent = new Intent(context, MainActivity.class);
                // Send data to NotificationView Class
                intent.putExtra("title", title);
                intent.putExtra("text", text);
                intent.putExtra("flag", flag);
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


                startForeground(1, builder.build());
            } else {
                alarm.cancel(pending);
                long interval = 30000;//milliseconds
                alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval, pending);
            }

            // Start performing service task
//            serviceTask();

            Toast.makeText(this, "Service Started!", Toast.LENGTH_LONG).show();
        }

    }

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

//    private void StopPerformingServiceTask() {
//        if(asyncTask!=null)
//            asyncTask.cancel(true);
//    }

    private void GoToDesktop() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    @Override
    public void onDestroy() {
        // performs when user or system kill our service
//        StopPerformingServiceTask();
        super.onDestroy();
    }

//    private void serviceTask() {
//        asyncTask = new MyTask();
//        asyncTask.execute();
//    }

    // We should to register our service in AlarmManager service
    // for performing periodical starting of our service by the system
//    private void startServiceStarter() {
//        Intent intent = new Intent(this, ServiceStarter.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, this.REQUEST_CODE, intent, 0);
//
//        if (pendingIntent == null) {
//            Toast.makeText(this, "Some problems with creating of PendingIntent", Toast.LENGTH_LONG).show();
//        } else {
//            if (serviceStarterAlarmManager == null) {
//                serviceStarterAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                serviceStarterAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
//                        SystemClock.elapsedRealtime() + FIRST_RUN_TIMEOUT_MILISEC,
//                        SERVICE_STARTER_INTERVAL_MILISEC, pendingIntent);
//            }
//        }
//    }

//    class MyTask extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... params) {
//            try {
//                for (; ; ) {
//                    TimeUnit.SECONDS.sleep(SERVICE_TASK_TIMEOUT_SEC);
//
//                    // check does performing of the task need
//                    if (isCancelled()) {
//                        break;
//                    }
////                    startBLE();
//                    // Initiating of onProgressUpdate callback that has access to UI
//                    publishProgress();
//                }
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(Void... progress) {
//            super.onProgressUpdate(progress);
//            mHandler = new Handler();
//
//            BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
//            mBluetoothAdapter = manager.getAdapter();
//            startScan();
//        }
//    }
}




