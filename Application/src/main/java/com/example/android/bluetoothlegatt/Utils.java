package com.example.android.bluetoothlegatt;

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;

/**
 * Created by penglian on 2017/10/16.
 */

public class Utils {
    //gps是否可用
    public static final boolean isGpsEnable(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    public static final boolean isLOLLIPOP(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return true;
        }
        return false;
    }
}
