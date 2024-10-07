package com.pasc.lib.nearby.map.base;

import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by huanglihou519 on 2018/5/1.
 */

public class LocationChecker {
    private LocationManager manager;

    LocationChecker(Context context) {
        this.manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean test() {
        try {
            String provider = LocationManager.GPS_PROVIDER;// 指定LocationManager的定位方法
            return manager.isProviderEnabled(provider);
        } catch (SecurityException | IllegalArgumentException e) {
            Log.e("LocationChecker", e.getMessage());
            return false;
        } catch (RuntimeException e) {
            Log.e("LocationChecker", e.getMessage());
            return false;
        } catch (Exception e){
            Log.e("LocationChecker", e.getMessage());
            return false;
        }
    }
}
