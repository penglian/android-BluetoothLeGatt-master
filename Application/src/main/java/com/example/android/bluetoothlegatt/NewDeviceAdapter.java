package com.example.android.bluetoothlegatt;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by penglian on 2017/10/16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NewDeviceAdapter extends BaseAdapter{
    private ArrayList<ScanResult> mLeDevices;
    private Context context;
    private NewOnItemClick itemClick;

    public NewDeviceAdapter(Context context, NewOnItemClick itemClick) {
        super();
        this.context = context;
        mLeDevices = new ArrayList<ScanResult>();
        this.itemClick = itemClick;
    }

    public ScanResult getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.listitem_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final ScanResult device = mLeDevices.get(i);
        final String deviceName = device.getDevice().getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);

        viewHolder.deviceAddress.setText(device.getDevice().getAddress());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (device == null) return;
                final Intent intent = new Intent(context, DeviceControlActivity.class);
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getDevice().getName());
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getDevice().getAddress());
                itemClick.onItemClick(intent);
            }
        });

        return view;
    }

    /**
     * Search the adapter for an existing device address and return it, otherwise return -1.
     */
    private int getPosition(String address) {
        int position = -1;
        for (int i = 0; i < mLeDevices.size(); i++) {
            if (mLeDevices.get(i).getDevice().getAddress().equals(address)) {
                position = i;
                break;
            }
        }
        return position;
    }


    /**
     * Add a ScanResult item to the adapter if a result from that device isn't already present.
     * Otherwise updates the existing position with the new ScanResult.
     */
    public void add(ScanResult scanResult) {

        int existingPosition = getPosition(scanResult.getDevice().getAddress());

        if (existingPosition >= 0) {
            // Device is already in list, update its record.
            mLeDevices.set(existingPosition, scanResult);
        } else {
            // Add new Device's ScanResult to list.
            mLeDevices.add(scanResult);
        }
    }

    public interface NewOnItemClick{
        void onItemClick(Intent intent);
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

}
