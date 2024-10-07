package com.pasc.lib.nearby.map;

import com.google.gson.annotations.SerializedName;

/**
 * 返回给web的GPS数据
 * create by wujianning385 on 2018/7/24.
 */
public class GpsInfoBean {

    //经度
    @SerializedName("longitude")
    public double longitude;

    //纬度
    @SerializedName("latitude")
    public double latitude;

    //国家
    @SerializedName("country")
    public String country;

    //省
    @SerializedName("province")
    public String province;

    //市
    @SerializedName("city")
    public String city;

    //区
    @SerializedName("district")
    public String district;

    //详细地址
    @SerializedName("address")
    public String address;

    //城市编码
    @SerializedName("cityCode")
    public String cityCode;

    //区域编码
    @SerializedName("adcode")
    public String adcode;

    //街道
    @SerializedName("street")
    public String street;

    //街道
    @SerializedName("aoiName")
    public String aoiName;

    public GpsInfoBean (double longitude, double latitude, String country, String province,
                        String city, String district, String address, String cityCode, String adcode, String street, String aoiName) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.country = country;
        this.province = province;
        this.city = city;
        this.district = district;
        this.address = address;
        this.cityCode = cityCode;
        this.adcode = adcode;
        this.street = street;
        this.aoiName = aoiName;
    }
}
