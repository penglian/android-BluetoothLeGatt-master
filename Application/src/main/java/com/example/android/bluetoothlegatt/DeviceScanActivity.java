/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends Activity{
    private static final String TAG = "DeviceScanActivity";
    private NewDeviceAdapter mNewDeviceAdapter;
    private DeviceAdapter mDeviceAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private static final int REQUEST_ENABLE_BT = 1;
    ListView lvDevice;
    private NewScanManager newScanManager;
    private ScanManager scanManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.title_devices);
        setContentView(R.layout.activity_scan);
        initData();
        initView();
    }

    private void initData() {
        checkAvaluable();
        checkPermission();
//        checkOpenGps();
        if(Utils.isLOLLIPOP()){
            newScanManager = new NewScanManager(this);
        }else{
            scanManager = new ScanManager(this);
        }
    }

    private void checkAvaluable() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
            Toast.makeText(DeviceScanActivity.this,"当前系统版本过低，无法正常使用低功耗蓝牙功能",Toast.LENGTH_SHORT).show();
            finish();
        }

        // 检测是否支持ble蓝牙特性
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void initView() {
        lvDevice = findViewById(R.id.lv_list);
}

    private void checkOpenGps() {
        if(!Utils.isGpsEnable(this)){
            openGps();
        }
    }

    private void checkPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.ACCESS_COARSE_LOCATION"},1000);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1000 && grantResults[0] ==  PackageManager.PERMISSION_GRANTED){
            return ;
        }else{
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }


    /**
     * 打开GPS
     */
    private void openGps(){
       //跳转到gps设置页
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent,1000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                if(Utils.isLOLLIPOP()){
                    mNewDeviceAdapter.clear();
                }else{
                    mDeviceAdapter.clear();
                }

                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Utils.isLOLLIPOP()){
            newScanManager.setBluetoothAdapter(mBluetoothAdapter);
            newScanManager.registerReceiver();
            // Initializes list view adapter.
            mNewDeviceAdapter = new NewDeviceAdapter(this, new NewDeviceAdapter.NewOnItemClick() {
                @Override
                public void onItemClick(Intent intent) {
                    if (mScanning) {
                        scanLeDevice(false);
                        mScanning = false;
                    }
                    startActivity(intent);
                }
            });
            newScanManager.startAdvertising();
            lvDevice.setAdapter(mNewDeviceAdapter);
        }else {
            scanManager.setmBluetoothAdapter(mBluetoothAdapter);
            scanManager.setLeScanCallBack(mLeScanCallback);
            mDeviceAdapter = new DeviceAdapter(this, new DeviceAdapter.OnItemClick() {
                @Override
                public void OnItemClick(Intent intent) {
                    if (mScanning) {
                        scanLeDevice(false);
                        mScanning = false;
                    }
                    startActivity(intent);
                }
            });
            lvDevice.setAdapter(mDeviceAdapter);
        }

        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        if(Utils.isLOLLIPOP()){
            mNewDeviceAdapter.clear();
            newScanManager.unRegisterReceiver();
            newScanManager.stopAdvertising();
        }else{
            mDeviceAdapter.clear();
        }
    }

    /**
     * 扫描附近的设备
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if(Utils.isLOLLIPOP()){
            if(enable){
                newScanManager.startScanDevice(new SampleScanCallback());
            }else{
                newScanManager.stopScanDevice();
            }
        }else{
            if(enable){
                scanManager.startScanDevice();
            }else{
                scanManager.stopScanDevice();
            }
        }
        invalidateOptionsMenu();
    }

    /**
     * 5.0以上扫描设备回调接口
     * Custom ScanCallback object - adds to adapter on success, displays error on failure.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class SampleScanCallback extends ScanCallback {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            for (ScanResult result : results) {
                mNewDeviceAdapter.add(result);
            }
            mNewDeviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            mNewDeviceAdapter.add(result);
            mNewDeviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(DeviceScanActivity.this, "Scan failed with error: " + errorCode, Toast.LENGTH_LONG)
                    .show();
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDeviceAdapter.addDevice(device);
                            mDeviceAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
}