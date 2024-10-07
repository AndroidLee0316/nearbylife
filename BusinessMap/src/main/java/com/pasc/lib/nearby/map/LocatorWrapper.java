package com.pasc.lib.nearby.map;

import android.app.Activity;

import com.pasc.lib.nearby.map.base.Locator;

import io.reactivex.Observable;

public class LocatorWrapper {
    public static Observable<Locator.PrepareStatus> prepareLocation(Activity mActivity) {

        return Locator.prepareLocation(mActivity, new Locator.EventHandler() {
            @Override
            public void onDialogCancel() {
                //EventUtils.onEvent("定位弹框","取消");
            }

            @Override
            public void onDialogSubmit() {
                //EventUtils.onEvent("定位弹框","去开启");

            }
        });
    }
}
