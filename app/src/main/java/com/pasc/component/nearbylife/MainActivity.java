package com.pasc.component.nearbylife;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.amap.api.services.core.LatLonPoint;
import com.pasc.lib.nearby.NearByLifeManager;
import com.pasc.lib.nearby.bean.NearByLifePoi;
import com.pasc.lib.nearby.bean.NearByLocInfo;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViewById(R.id.main_nearbylife).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        goNearByLifeMap();
      }
    });
  }

  private void goNearByLifeMap() {
    // 使用默认分类
    //NearByLifeManager.getInstance().startMap(MainActivity.this);

    // 使用自定义分类
    //        String[] poiSearchType = {"便利店", "停车场", "超市", "KTV", "医院/诊所", "派出所"};
    //        NearByLifeManager.getInstance().startMapWithSearchType(MainActivity.this, poiSearchType);

    // 通过路由跳转
    //        Bundle bundle = new Bundle();
    //        bundle.putString("typeIndex", "加油站");
    //        ARouter.getInstance()
    //                .build(NearbyConstant.JUMP_NEARBY_MAP_MAIN_KEY)
    //                .with(bundle)
    //                .navigation();
    List<NearByLifePoi> contentList = new ArrayList<NearByLifePoi>();

    NearByLocInfo loc =NearByLocInfo.builder()
        .longitude(114.066214)
        .latitude(22.54932)
        .locAddr("addr")
        .locName("name")
        .build();

    NearByLocInfo loc2 =NearByLocInfo.builder()
        .longitude(120.167045)
        .latitude(33.357732)
        .locAddr("测试")
        .locName("姓名")
        .build();
    List<NearByLocInfo> locs = new ArrayList<>();
    locs.add(loc);
    locs.add(loc2);
    NearByLifePoi nearByLifeContent = NearByLifePoi.builder()
        .addr("莽原")
        //.locs(locs)
        .name("早餐店")
        .resId(R.drawable.bg_close)
        .build();
    contentList.add(nearByLifeContent);
    contentList.add(NearByLifePoi.builder().name("公共自行车").build());
    NearByLifeManager.getInstance().setData(contentList);
    NearByLifeManager.getInstance().setDefaultLoc(new LatLonPoint(33.357732,120.167047));
    Bundle bundle = new Bundle();
    bundle.putString("searchTypeByTxt", "充电桩");
    //        bundle.putString("searchTypeByCode", "ticket_outlets");
    ARouter.getInstance()
        .build("/nearby/map/main")
        .with(bundle)
        .navigation();
  }
}
