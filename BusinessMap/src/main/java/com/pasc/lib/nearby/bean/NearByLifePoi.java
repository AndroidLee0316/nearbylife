package com.pasc.lib.nearby.bean;

import android.support.annotation.DrawableRes;
import com.google.gson.annotations.SerializedName;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import java.util.List;

/**
 * Created by zhangxu678 on 2019-07-29.
 */
public class NearByLifePoi {
  //服务名
  @SerializedName("name") @NonNull public final String name;
  //位置信息，可为空，为空的话使用默认搜索位置
  @SerializedName("locs") @Nullable public final List<NearByLocInfo> locs;
  @SerializedName("resId")@Nullable @DrawableRes final public int resId;

  private NearByLifePoi(String name,
      List<NearByLocInfo> locs, int resId) {
    this.name = name;
    this.locs = locs;
    this.resId = resId;
  }

  public static class Builder{
    @NonNull private String name;
    //位置信息，可为空，为空的话使用默认搜索位置
    @Nullable private List<NearByLocInfo> locs;
    @Nullable private String addr;
    @Nullable private String tel;
    private int resId;
    public Builder name(String name){
      this.name=name;
      return this;
    }
    public Builder locs(List<NearByLocInfo> locs){
      this.locs=locs;
      return this;
    }
    public Builder addr(String addr){
      this.addr=addr;
      return this;
    }
    public Builder tel(String tel){
      this.tel=tel;
      return this;
    }
    public Builder resId(int resId){
      this.resId=resId;
      return this;
    }
    public NearByLifePoi build(){
      return new NearByLifePoi(name,locs,resId);
    }
  }
  public static Builder builder(){
    return new Builder();
  }
}
