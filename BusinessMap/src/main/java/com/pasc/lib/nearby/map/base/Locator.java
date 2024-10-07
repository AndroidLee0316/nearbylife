package com.pasc.lib.nearby.map.base;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.pasc.lib.base.permission.IPermissionClickListener;
import com.pasc.lib.base.permission.PermissionUtils;
import com.pasc.lib.base.util.DeviceUtils;
import com.pasc.lib.base.widget.IPermissionDialog;
import com.pasc.lib.base.widget.PermissionDialog;
import com.pasc.lib.gaode.location.GaoDeLocationFactory;
import com.pasc.lib.lbs.LbsManager;
import com.pasc.lib.lbs.location.ILocationFactory;
import com.pasc.lib.lbs.location.LocationException;
import com.pasc.lib.lbs.location.PascLocationListener;
import com.pasc.lib.lbs.location.bean.PascLocationData;
import com.pasc.lib.nearby.NearbyConstant;
import com.pasc.lib.nearby.R;
import com.pasc.lib.nearby.map.GpsInfoBean;
import com.pasc.lib.nearby.utils.IntentUtils;
import com.pasc.lib.nearby.utils.LocationUtils;
import com.pasc.lib.nearby.utils.NearByUtil;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * Created by huanglihou519 on 2018/3/22.
 */

public class Locator {
    private static final String TAG = Locator.class.getSimpleName();
    private static AtomicBoolean isSettingDialogShow = new AtomicBoolean(false);
    private static AtomicBoolean isGPSDialogShow = new AtomicBoolean(false);

    public static void init(Context context) {

        ILocationFactory locationFactory = new GaoDeLocationFactory(context);

        LbsManager.getInstance().initLbs(locationFactory);
    }

    public static Flowable<PascLocationData> doLocation(final Context context, final boolean isOnce) {

        Flowable<PascLocationData> result = Flowable.create(new FlowableOnSubscribe<PascLocationData>() {
            @Override
            public void subscribe(final FlowableEmitter<PascLocationData> emitter) throws Exception {

                final PascLocationListener listener = new PascLocationListener() {
                    @Override
                    public void onLocationSuccess(PascLocationData pascLocationData) {
                        GpsInfoBean gpsInfo = new GpsInfoBean(pascLocationData.getLongitude(),
                                pascLocationData.getLatitude(), "中国", pascLocationData.getProvince(),
                                pascLocationData.getCity(), pascLocationData.getDistrict(),
                                pascLocationData.getAddress(), pascLocationData.getCityCode(),
                                pascLocationData.getAdCode(), pascLocationData.getStreet(),
                                pascLocationData.getAoiName());

                        // save current location data to SharedPreferences
                        Gson gson = new Gson();
                        NearByUtil.saveStrToSP(NearbyConstant.NEAR_CURRENT_LOCATION_KEY, gson.toJson(gpsInfo));

                        // 停止定位&释放资源
                        LbsManager.getInstance().stopLocation(0, this);
                        emitter.onNext(pascLocationData);
                    }

                    @Override
                    public void onLocationFailure(LocationException e) {
                        // 停止定位&释放资源
                        LbsManager.getInstance().stopLocation(0, this);
                        emitter.onError(e);
                    }
                };

                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        LbsManager.getInstance().stopLocation(isOnce ? 0 : 2000, listener);
                    }
                });

                LbsManager.getInstance().doLocation(isOnce ? 0 : 2000, listener);

            }
        }, BackpressureStrategy.LATEST);


        return isOnce ? result.take(1) : result;
    }

    public enum PrepareStatus {
        PERMISSION_GRANTED,     //有权限
        CANCEL_DIALOG,          //有权限还需要打开 gps，弹出 Dialog，被关闭
        OPEN_GPS_SETTING,       //有权限还需要打开 gps，打开 gps 设置界面
        PERMISSION_NOT_GRANTED, //没有权限
        OPEN_PERMISSION_SETTING,//没有权限，打开权限设置界面
        UNKNOWN_ERROR           //未知异常
    }

    /**
     * 定位开始前的准备，需要申请权限，对于华为机子需要打开gps
     */
    public static Observable<PrepareStatus> prepareLocation(final Activity context, final EventHandler handler) {
        return Observable.create(new ObservableOnSubscribe<PrepareStatus>() {
            @Override
            public void subscribe(final ObservableEmitter<PrepareStatus> observableEmitter)
                    throws Exception {
                final CompositeDisposable disposables = new CompositeDisposable();
                Observable<Boolean> locationPermissionObservable =
                        PermissionUtils.request(context, PermissionUtils.Groups.LOCATION)
                                .map(new Function<Boolean, Boolean>() {
                                    @Override
                                    public Boolean apply(Boolean aBoolean)
                                            throws Exception {
                                        if (DeviceUtils.isVivoMobilePhone()) {
                                            if (LocationUtils.checkGPSIsOpen(context)) {
                                                return new LocationChecker(context).test();
                                            } else {
                                                return true;
                                            }

                                        }
                                        return aBoolean;
                                    }
                                })
                                .share();
                // 有权限且不需要打开GPS
                disposables.add(locationPermissionObservable.filter(new Predicate<Boolean>() {
                    @Override
                    public boolean test(Boolean aBoolean) throws Exception {
                        return aBoolean && (!DeviceUtils.isLocationNeedGPSDevice()
                                || LocationUtils.checkGPSIsOpen(context));
                    }
                }).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        observableEmitter.onNext(PrepareStatus.PERMISSION_GRANTED);
                    }
                }));
                // 有权限且需要打开GPS
                disposables.add(locationPermissionObservable.filter(new Predicate<Boolean>() {
                    @Override
                    public boolean test(Boolean aBoolean) throws Exception {
                        return aBoolean
                                && DeviceUtils.isLocationNeedGPSDevice()
                                && !LocationUtils.checkGPSIsOpen(context);
                    }
                }).subscribe(new Consumer<Boolean>() {

                    @Override
                    public void accept(Boolean aBoolean) throws Exception {

                        if (isGPSDialogShow.get()) return;

                        final PermissionDialog dialog = new PermissionDialog(context);
                        dialog.setTitle(context.getResources().getString(R.string.nearby_base_open_gps));
                        dialog.setHint(context.getResources().getString(R.string.base_perm_loc_hint));
                        dialog.setIcon(R.drawable.pasclibbase_ic_loc);
                        dialog.setPermissionClickListener(new IPermissionClickListener() {
                            @Override
                            public void onSetting(IPermissionDialog iPermissionDialog) {
                                dialog.dismiss();
                                if (handler != null) {
                                    handler.onDialogSubmit();
                                }
                                observableEmitter.onNext(PrepareStatus.OPEN_GPS_SETTING);
                                Intent intent =
                                        new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                context.startActivity(intent);
                            }

                            @Override
                            public void onCancel(IPermissionDialog iPermissionDialog) {
                                dialog.dismiss();
                                if (handler != null) {
                                    handler.onDialogCancel();
                                }

                                if (!observableEmitter.isDisposed()) {
                                    observableEmitter.onNext(PrepareStatus.CANCEL_DIALOG);
                                }
                            }
                        });

                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                isGPSDialogShow.set(false);
                            }
                        });

                        isGPSDialogShow.set(true);
                        dialog.show();
                    }

                }));

                // 没有权限
                disposables.add(locationPermissionObservable.filter(new Predicate<Boolean>() {
                    @Override
                    public boolean test(Boolean aBoolean) throws Exception {
                        return !aBoolean;
                    }
                }).take(1).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (isSettingDialogShow.get()) return;
                        final PermissionDialog permissionDialog = new PermissionDialog(context);
                        permissionDialog.setTitle(context.getResources().getString(R.string.base_open_loc));
                        permissionDialog.setHint(context.getResources().getString(R.string.base_perm_loc_hint));
                        permissionDialog.setIcon(R.drawable.pasclibbase_ic_loc);
                        permissionDialog.setPermissionClickListener(new IPermissionClickListener() {
                            @Override
                            public void onSetting(IPermissionDialog iPermissionDialog) {
                                permissionDialog.dismiss();
                                if (handler != null) {
                                    handler.onDialogSubmit();
                                }
                                observableEmitter.onNext(
                                        PrepareStatus.OPEN_PERMISSION_SETTING);
                                IntentUtils.goToPermissionSetting(context);
                            }

                            @Override
                            public void onCancel(IPermissionDialog iPermissionDialog) {
                                permissionDialog.dismiss();
                                if (handler != null) {
                                    handler.onDialogCancel();
                                }
                                observableEmitter.onNext(PrepareStatus.PERMISSION_NOT_GRANTED);
                            }
                        });

                        permissionDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                isSettingDialogShow.set(false);
                            }
                        });
                        isSettingDialogShow.set(true);
                        permissionDialog.show();
                    }
                }));

                observableEmitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        disposables.clear();
                    }
                });
            }
        }).onErrorReturn(new Function<Throwable, PrepareStatus>() {
            @Override
            public PrepareStatus apply(Throwable throwable) throws Exception {
                Log.e(TAG, throwable.getMessage());
                return PrepareStatus.UNKNOWN_ERROR;
            }
        });
    }

    public interface EventHandler {

        void onDialogCancel();

        void onDialogSubmit();
    }
}
