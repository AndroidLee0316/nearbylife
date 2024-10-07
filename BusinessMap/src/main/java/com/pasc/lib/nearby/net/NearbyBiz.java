package com.pasc.lib.nearby.net;

import com.pasc.lib.base.AppProxy;
import com.pasc.lib.nearby.NearByLifeManager;
import com.pasc.lib.net.ApiGenerator;
import com.pasc.lib.net.transform.RespTransformer;
import java.util.List;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class NearbyBiz {

  /**
   * 获取poi检索列表
   * @return
   */
  public static Single<List<String>> getParingLotList(){
    RespTransformer<List<String>> respTransformer =
            RespTransformer.newInstance();
    return ApiGenerator.createApi(AppProxy.getInstance().getHost(), NearbyApi.class)
            .getPoiInfo(NearByLifeManager.getInstance().getApiUrl())
            .compose(respTransformer)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io());
  }
}
