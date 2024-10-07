package com.pasc.lib.nearby.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lanshaomin
 * Date: 2019/7/5 下午2:40
 * Desc:配置信息
 */
public class NearByLifeBean {
    @SerializedName("nearbyLife")
    public NearbyConfigBean nearbyLife;

    public static class NearbyConfigBean {
        @SerializedName("name")
        public String name;
        @SerializedName("enable")
        public boolean enable;
        @SerializedName("store")
        public boolean store;
        @SerializedName("publicWashroom")
        public boolean publicWashroom;
        @SerializedName("bank")
        public boolean bank;
        @SerializedName("atm")
        public boolean atm;
        @SerializedName("chargingPile")
        public boolean chargingPile;
        @SerializedName("photoStudio")
        public boolean photoStudio;
        @SerializedName("gasStation")
        public boolean gasStation;
        @SerializedName("carCarePoint")
        public boolean carCarePoint;
        @SerializedName("pharmacy")
        public boolean pharmacy;
        @SerializedName("ticketSales")
        public boolean ticketSales;
        @SerializedName("socialWelfareInstitute")
        public boolean socialWelfareInstitute;
        @SerializedName("postOffice")
        public boolean postOffice;
        @SerializedName("museum")
        public boolean museum;
        @SerializedName("designatedDriver")
        public boolean designatedDriver;
        @SerializedName("equipmentOutlet")
        public boolean equipmentOutlet;
        @SerializedName("medicalInsuranceNetwork")
        public boolean medicalInsuranceNetwork;
    }
}
