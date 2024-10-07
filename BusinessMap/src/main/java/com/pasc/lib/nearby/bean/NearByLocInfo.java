package com.pasc.lib.nearby.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zhangxu678 on 2019-08-06.
 */
public class NearByLocInfo {
  @SerializedName("locName") public String locName;
  @SerializedName("locAddr") public String locAddr;
  @SerializedName("tel") public String tel;
  @SerializedName("longitude") public double longitude;
  @SerializedName("latitude") public double latitude;

  private NearByLocInfo(String locName, String locAddr, String tel, double longitude,
      double latitude) {
    this.locName = locName;
    this.locAddr = locAddr;
    this.tel = tel;
    this.longitude = longitude;
    this.latitude = latitude;
  }

  public static class Builder {
    private String locName;
    private String locAddr;
    private String tel;
    private double longitude;
    private double latitude;

    public Builder locName(String locName) {
      this.locName = locName;
      return this;
    }

    public Builder locAddr(String locAddr) {
      this.locAddr = locAddr;
      return this;
    }

    public Builder tel(String tel) {
      this.tel = tel;
      return this;
    }

    public Builder longitude(double longitude) {
      this.longitude = longitude;
      return this;
    }

    public Builder latitude(double latitude) {
      this.latitude = latitude;
      return this;
    }

    public NearByLocInfo build() {
      return new NearByLocInfo(locName, locAddr, tel, longitude, latitude);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
