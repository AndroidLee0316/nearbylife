package com.pasc.lib.nearby.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.pasc.lib.base.util.DensityUtils;
import com.pasc.lib.base.util.SPUtils;

public class NearByUtil {

  private static final int CLICK_AREA_VALUE = 50;
  private static double upX, upY;

  // 两次点击按钮之间的点击间隔不能少于200毫秒
  private static final int MIN_CLICK_DELAY_TIME = 1000;
  private static long lastClickTime = System.currentTimeMillis();
  private static final double EARTH_RADIUS = 6378.137;//地球半径,单位千米

  /**
   * 判断网络是否可用
   */
  public static boolean isNetworkAvailable(Context context) {

    boolean checkResult = false;
    if (context != null) {
      ConnectivityManager connectivityManager =
          (ConnectivityManager) context.getApplicationContext()
              .getSystemService(Context.CONNECTIVITY_SERVICE);
      if (connectivityManager != null) {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
          checkResult = true;
        }
      }
    }

    return checkResult;
  }

  public static boolean isFastClick() {
    long time = System.currentTimeMillis();
    long timeD = time - lastClickTime;
    if (0 < timeD && timeD < MIN_CLICK_DELAY_TIME) {
      return true;
    }
    lastClickTime = time;
    return false;
  }

  public static boolean isFastClick(MotionEvent ev) {
    long time = System.currentTimeMillis();
    long timeD = time - lastClickTime;
    if (0 < timeD && timeD < MIN_CLICK_DELAY_TIME) {
      if (Math.abs(upX) > 0 && Math.abs(upY) > 0) {
        if (Math.abs(ev.getX(ev.getActionIndex()) - upX) < DensityUtils.px2dp(CLICK_AREA_VALUE)
            && Math.abs(ev.getY(ev.getActionIndex()) - upY) < DensityUtils.px2dp(
            CLICK_AREA_VALUE)) {
          return true;
        }
      }
      upX = ev.getX(ev.getActionIndex());
      upY = ev.getY(ev.getActionIndex());
    }
    lastClickTime = time;
    return false;
  }

  /**
   * 调用拨号界面
   */
  public static void call(Context context, String phone) {

    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  /**
   * save String to SharedPreferences
   */
  public static void saveStrToSP(String key, String value) {
    if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
      return;
    }

    SPUtils.getInstance().setParam(key, value);
  }

  /**
   * get String From SharedPreferences by key
   */
  public static String getStrCacheFromSP(String key) {
    String cacheStr = "";

    if (!TextUtils.isEmpty(key)) {
      cacheStr = (String) SPUtils.getInstance().getParam(key, "");
    }

    return cacheStr;
  }

  private static double rad(double d) {
    return d * Math.PI / 180.0;
  }

  public static int getDistance(double lat1, double lng1, double lat2, double lng2) {
    double radLat1 = rad(lat1);
    double radLat2 = rad(lat2);
    double a = radLat1 - radLat2;
    double b = rad(lng1) - rad(lng2);

    double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
        Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
    s = s * EARTH_RADIUS;
    s = Math.round(s * 10000) / 10;
    return (int)s;
  }
}
