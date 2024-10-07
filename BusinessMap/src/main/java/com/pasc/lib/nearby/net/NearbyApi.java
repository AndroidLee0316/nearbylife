package com.pasc.lib.nearby.net;

import com.pasc.lib.net.resp.BaseResp;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.POST;
import retrofit2.http.Url;


public interface NearbyApi {

    /**
     * 获取poi检索列表
     *
     * @return
     */
    @POST
    Single<BaseResp<List<String>>> getPoiInfo(@Url String url);
}
