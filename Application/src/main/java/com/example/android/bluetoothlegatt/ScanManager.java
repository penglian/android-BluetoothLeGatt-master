package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;

/**
 * 4.3-5.0之间的扫描管理
 * Created by penglian on 2017/10/16.
 */

public class ScanManager {
    private Context context;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothAdapter.LeScanCallback leScanCallback;

    public ScanManager(Context context){
        this.context = context;
        mHandler = new Handler();
    }

    public void setmBluetoothAdapter(BluetoothAdapter bluetoothAdapter){
        this.mBluetoothAdapter = bluetoothAdapter;
    }

    public void startScanDevice(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(leScanCallback);
            }
        }, SCAN_PERIOD);

        mBluetoothAdapter.startLeScan(leScanCallback);
    }

    public void stopScanDevice(){
        mBluetoothAdapter.stopLeScan(leScanCallback);
    }

    public void setLeScanCallBack(BluetoothAdapter.LeScanCallback leScanCallBack){
        this.leScanCallback = leScanCallBack;
    }

}
