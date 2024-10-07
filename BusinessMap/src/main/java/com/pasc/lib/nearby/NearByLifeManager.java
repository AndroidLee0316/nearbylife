package com.pasc.lib.nearby;


import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.google.gson.Gson;
import com.pasc.lib.log.PascLog;
import com.pasc.lib.nearby.bean.NearByLifeBean;
import com.pasc.lib.nearby.bean.NearByLifePoi;
import com.pasc.lib.nearby.bean.NearLifeConfigBean;
import com.pasc.lib.nearby.net.NearByNetManager;
import com.pasc.lib.nearby.utils.AssetsUtil;
import java.util.List;

/**
 * 附近生活提供部分UI及功能可配置
 */
public class NearByLifeManager {

    protected String mSearchTypeList;
    protected String mApiUrl;

    protected MapSearchTypeConfig searchTypeConfig;

    private NearLifeConfigBean nearLifeConfigBean;
    private NearByLifeBean.NearbyConfigBean nearbyLife;
    private List<NearByLifePoi> contents;
    private LatLonPoint latLonPoint;
    private int searchRange;
    private String title;

    public NearByLifeManager() {

    }

    public static NearByLifeManager getInstance() {
        return SingletonInstance.instance;
    }

    public List<NearByLifePoi> getData() {
        return contents;
    }

    public void setData(List<NearByLifePoi> contents) {
        this.contents = contents;
    }

    public void setDefaultLoc(LatLonPoint latLonPoint){
        this.latLonPoint=latLonPoint;
    }
    public LatLonPoint getDefaultLoc(){
        return this.latLonPoint;
    }

    private static class SingletonInstance {
        private static final NearByLifeManager instance = new NearByLifeManager();
    }

    private void init(NearLifeConfigBean configBean) {
        this.nearLifeConfigBean = configBean;
    }

    public int getSearchRange() {
        return searchRange;
    }

    public void setSearchRange(int searchRange) {
        this.searchRange = searchRange;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void startMapWithSearchType(Activity activity, String[] searchType) {

//        searchTypeConfig = new MapSearchTypeConfig
//                .Builder()
//                .setNearLifePoiSearchType(searchType)
//                .create();
//
//        NearbyLifeHomeActivity.start(activity, searchTypeConfig);
    }

    public void setSearchTypeList(String searchTypeList) {
        this.mSearchTypeList = searchTypeList;
    }

    public String getSearchTypeList() {
        return mSearchTypeList;
    }

    public void setApiUrl(String apiUrl) {
        this.mApiUrl = apiUrl;
    }

    public String getApiUrl() {
        if (TextUtils.isEmpty(mApiUrl)) {
            return NearByNetManager.NEARBY_POI_INFO;
        }
        return mApiUrl;
    }

    public String getTabTxtColor() {
        return nearLifeConfigBean == null ? "" : nearLifeConfigBean.tabTxtColor;
    }

    public String getDefaultCity() {
        return nearLifeConfigBean == null ? "" : nearLifeConfigBean.defaultCity;
    }

    public void initConfig(Context context, String jsonPath) {
        if (TextUtils.isEmpty(jsonPath)) {
            throw new NullPointerException("请传入正确的serviceConfigPath");
        }
        try {
            NearByLifeBean nearByLifeBean = new Gson().fromJson(AssetsUtil.parseFromAssets(context, jsonPath), NearByLifeBean.class);
            if (nearByLifeBean != null) {
                nearbyLife = nearByLifeBean.nearbyLife;
            }

        } catch (Exception e) {
            PascLog.v("NearByLifeUrlDispatcher", e.getMessage());
        }
    }

    public boolean enable() {
        return nearbyLife == null || nearbyLife.enable;
    }


    public boolean showStore() {
        return nearbyLife == null || nearbyLife.store;
    }

    public boolean showPublicWashroom() {
        return nearbyLife == null || nearbyLife.publicWashroom;
    }

    public boolean showBank() {
        return nearbyLife == null || nearbyLife.bank;
    }

    public boolean showAtm() {
        return nearbyLife == null || nearbyLife.atm;
    }

    public boolean showChargingPile() {
        return nearbyLife == null || nearbyLife.chargingPile;
    }

    public boolean showPhotoStudio() {
        return nearbyLife == null || nearbyLife.photoStudio;
    }

    public boolean showGasStation() {
        return nearbyLife == null || nearbyLife.gasStation;
    }

    public boolean showCarCarePoint() {
        return nearbyLife == null || nearbyLife.carCarePoint;
    }

    public boolean showPharmacy() {
        return nearbyLife == null || nearbyLife.pharmacy;
    }

    public boolean showTicketSales() {
        return nearbyLife == null || nearbyLife.ticketSales;
    }

    public boolean showSocialWelfareInstitute() {
        return nearbyLife == null || nearbyLife.socialWelfareInstitute;
    }

    public boolean showPostOffice() {
        return nearbyLife == null || nearbyLife.postOffice;
    }

    public boolean showMuseum() {
        return nearbyLife == null || nearbyLife.museum;
    }

    public boolean showDesignatedDriver() {
        return nearbyLife == null || nearbyLife.designatedDriver;
    }

    public boolean showMedicalInsuranceNetwork() {
        return nearbyLife == null || nearbyLife.medicalInsuranceNetwork;
    }

    public boolean showEquipmentOutlet() {
        return nearbyLife == null || nearbyLife.equipmentOutlet;
    }

}
