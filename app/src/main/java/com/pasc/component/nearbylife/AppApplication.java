package com.pasc.component.nearbylife;

import android.app.Application;
import com.pasc.lib.base.AppProxy;
import com.pasc.lib.base.util.AppUtils;
import com.pasc.lib.nearby.NearByLifeManager;
import com.pasc.lib.nearby.map.base.Locator;
import com.pasc.lib.net.NetConfig;
import com.pasc.lib.net.NetManager;
import com.pasc.lib.net.download.DownLoadManager;
import com.pasc.lib.router.RouterManager;



public class AppApplication extends Application {

    //private static final String HOST_URL = "https://sz-smt-zag-stg1.pingan.com.cn:10443/";
//    private static final String HOST_URL = "http://massc-smt-stg.yun.city.pingan.com/";
    private static final String HOST_URL = "http://cssc-smt-stg.yun.city.pingan.com/";

    @Override
    public void onCreate() {
        super.onCreate();

        // 高德定位初始化
        Locator.init(getApplicationContext());
        /**
         * AppProxy 必须完成初始化
         * SharedPreferences保存用到了libBase里面的SPUtil
         */
        if (AppUtils.getPIDName(this).equals(getPackageName())) {//主进程
            AppProxy.getInstance().init(this, false)
                    .setIsDebug(BuildConfig.DEBUG)
                    .setProductType(BuildConfig.PRODUCT_FLAVORS_TYPE)
                    .setHost(HOST_URL) // 自定义HostUrl
                    .setVersionName(BuildConfig.VERSION_NAME);

            //网络
            initNet();
            // ARouter
            initARouter();
            initUrlDispatch();


        }

    }

    /****初始化网络****/
    private void initNet() {

        NetConfig config = new NetConfig.Builder(this)
                //.baseUrl(UrlManager.getApiUrlRoot())
                .baseUrl(HOST_URL)
                .headers(HeaderUtil.getHeaders(BuildConfig.DEBUG, null))
                .gson(ConvertUtil.getConvertGson())
                .isDebug(BuildConfig.DEBUG)
                .build();

        NetManager.init(config);

        DownLoadManager.getDownInstance().init(this, 3, 5, 0);
    }

    private void initARouter() {
        RouterManager.initARouter(this, BuildConfig.DEBUG);
    }

    /**
     * 初始化配置文件
     */
    private void initUrlDispatch() {
        NearByLifeManager.getInstance().initConfig(this, "pasc.pingan.service.nearby.json");
    }
}
