package com.pasc.lib.nearby.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;

import com.pasc.lib.nearby.R;


/**
 * 地理定位相关的工具类
 * Created by zhangcan603 on 2017/11/28.
 */

public class LocationUtils {

    /**
     * 检测GPS是否打开
     */
    public static boolean checkGPSIsOpen(Context context) {
        boolean isOpen = false;
        try {
            LocationManager locationManager =
                    (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                isOpen = locationManager.isProviderEnabled(
                        LocationManager.GPS_PROVIDER);
            }

        } catch (Exception e) {
        }
        return isOpen;
    }

    /**
     * 强制帮用户打开GPS
     */
    public static final void openGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转GPS设置
     */
    public static void openGPSSettings(final Context context, final MyCallBack myCallBack) {
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return;
        }
        if (!checkGPSIsOpen(context)) {//已经打开

            //没有打开则弹出对话框
            new AlertDialog.Builder(context).setTitle("提示")
                    .setMessage("当前应用需要打开定位功能。\n\n请点击\"设置\"-\"定位服务\"-打开定位功能")
                    // 拒绝, 退出应用
                    .setNegativeButton(R.string.nearby_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myCallBack.onCancel();
                        }
                    })

                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //跳转GPS设置界面
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            context.startActivity(intent);
                        }
                    })

                    .setCancelable(false)
                    .show();
        }
    }

    public interface MyCallBack {
        void onCancel();
    }

    /**
     * 注册监听广播
     *
     * @throws Exception
     */
    public void ready(Context context) throws Exception {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        context.registerReceiver(new GpsStatusReceiver(), filter);
    }

    boolean currentGPSState = false;

    /**
     * 监听GPS 状态变化广播
     */
    private class GpsStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                currentGPSState = getGPSState(context);
            }
        }
    }

    /**
     * 获取ＧＰＳ当前状态
     */
    private boolean getGPSState(Context context) {
        boolean isGpsEnable = false;
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            isGpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        return isGpsEnable;
    }
}
