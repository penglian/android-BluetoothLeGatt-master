package com.example.android.bluetoothlegatt;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 5.0以上的扫描管理
 * Created by penglian on 2017/10/16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NewScanManager {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private Context context;
    private BroadcastReceiver advertisingFailureReceiver;
    private Handler mHandler;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    public NewScanManager(Context context){
        this.context = context;
        mHandler = new Handler();
        initBroadCast();
    }

    private void initBroadCast() {
        advertisingFailureReceiver = new BroadcastReceiver() {

            /**
             * Receives Advertising error codes from {@code AdvertiserService} and displays error messages
             * to the user. Sets the advertising toggle to 'false.'
             */
            @Override
            public void onReceive(Context mContext, Intent intent) {

                int errorCode = intent.getIntExtra(AdvertiserService.ADVERTISING_FAILED_EXTRA_CODE, -1);

                String errorMessage = context.getString(R.string.start_error_prefix);
                switch (errorCode) {
                    case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                        errorMessage += " " + context.getString(R.string.start_error_already_started);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                        errorMessage += " " + context.getString(R.string.start_error_too_large);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                        errorMessage += " " + context.getString(R.string.start_error_unsupported);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                        errorMessage += " " + context.getString(R.string.start_error_internal);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                        errorMessage += " " + context.getString(R.string.start_error_too_many);
                        break;
                    case AdvertiserService.ADVERTISING_TIMED_OUT:
                        errorMessage = " " + context.getString(R.string.advertising_timedout);
                        break;
                    default:
                        errorMessage += " " + context.getString(R.string.start_error_unknown);
                }

                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
            }
        };
    }

    /**
     * Must be called after object creation by MainActivity.
     *
     * @param btAdapter the local BluetoothAdapter
     */
    public void setBluetoothAdapter(BluetoothAdapter btAdapter) {
        this.mBluetoothAdapter = btAdapter;
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    public void setScanCallback(ScanCallback mScanCallback){
        this.mScanCallback = mScanCallback;
    }


    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
        builder.setServiceUuid(ParcelUuid.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT));
        scanFilters.add(builder.build());

        return scanFilters;
    }

    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }

    /**
     * Starts BLE Advertising by starting {@code AdvertiserService}.
     */
    public void startAdvertising() {
        context.startService(getServiceIntent(context));
    }

    /**
     * Stops BLE Advertising by stopping {@code AdvertiserService}.
     */
    public void stopAdvertising() {
        context.stopService(getServiceIntent(context));
    }



    /**
     * Returns Intent addressed to the {@code AdvertiserService} class.
     */
    private static Intent getServiceIntent(Context c) {
        return new Intent(c, AdvertiserService.class);
    }


    public void stopScanDevice() {
        mBluetoothLeScanner.stopScan(mScanCallback);
        mScanCallback = null;
    }

    public void startScanDevice(ScanCallback mScanCallback) {
            // Will stop the scanning after a set time.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScanDevice();
                }
            }, SCAN_PERIOD);

            // Kick off a new scan.
            setScanCallback(mScanCallback);
            mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);

            String toastText = context.getString(R.string.scan_start_toast) + " "
                    + TimeUnit.SECONDS.convert(SCAN_PERIOD, TimeUnit.MILLISECONDS) + " "
                    + context.getString(R.string.seconds);
            Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
    }

    public void registerReceiver(){
        IntentFilter failureFilter = new IntentFilter(AdvertiserService.ADVERTISING_FAILED);
        context.registerReceiver(advertisingFailureReceiver, failureFilter);
    }

    public void unRegisterReceiver(){
        context.unregisterReceiver(advertisingFailureReceiver);
    }

}
