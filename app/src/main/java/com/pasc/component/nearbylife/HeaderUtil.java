package com.pasc.component.nearbylife;

import android.content.Context;
import android.content.pm.PackageManager;

import com.pasc.lib.base.AppProxy;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C) 2016 pasc Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author yangzijian
 * @date 2018/7/16
 * @des 公共头信息
 * @modify
 **/
public class HeaderUtil {

    public static Map<String, String> getHeaders(boolean isDebug,Map<String, String> headers) {
        Map<String, String> commonHeaders = new HashMap<>();

        // 下个版本在上，跟后台沟通过
//        commonHeaders.put("SN", "ANDROID_" + (isDebug ? "DEBUG" : "RELEASE"));
//        String versionName = "";
//        if (AppProxy.getInstance().getApplication() != null) {
//            versionName = AppUtils.getVersionName(AppProxy.getInstance().getApplication());
//        }
//        /****app软件标识为  【平台标识】【产品标识】
//         app的主次版本信息  【主版本次版本】
//         ANDROID_RE
//         **/
//        commonHeaders.put("VN", versionName);
//        /****app的发布类型  【发布类型 release、beta、…】***/
//        commonHeaders.put("PT", isDebug?"DEBUG" : "RELEASE");
//        /*****Build no  从rdm上获取build号****/
//        commonHeaders.put("BN", "220");
//        /******设备的厂商名****/
//        commonHeaders.put("VC", Build.BRAND);
//        /****机型信息****/
//        commonHeaders.put("MO", Build.MODEL);
//        /****屏幕分辨率         【屏幕宽】 _【屏幕高】*****/
//        commonHeaders.put("RL", ScreenUtils.getScreenWidth() + "_" + ScreenUtils.getScreenHeight());
//        /****渠道号****/
//        commonHeaders.put("CHID", "10000");
//        /****ROM版本号****/
//        commonHeaders.put("RV", Build.MANUFACTURER);
//        /*****操作系统版本****/
//        commonHeaders.put("OS", "Android"+Build.VERSION.RELEASE);
//        /*****Q-UA版本号*****/
//        commonHeaders.put("QV", "1.0");
        // 渠道号
        commonHeaders.put ("CHID",getAppMetaData (AppProxy.getInstance ().getContext (),"NT_CHANNEL","product"));
        /***后台版本***/
//        commonHeaders.put("x-api-version", "1.4.0");
        commonHeaders.put("x-api-version", "1.2.0");
        if (headers!=null){
            commonHeaders.putAll (headers);
        }
        commonHeaders.put("Content-Type", "application/json");

        return commonHeaders;

    }
    //不同的类型要区别获取，以下是布尔类型的
    public static String getAppMetaData(Context context, String metaName, String defaultValue) {
        try {
            //application标签下用getApplicationinfo，如果是activity下的用getActivityInfo
            String value = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.getString (metaName, defaultValue);
            return value;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }
}
