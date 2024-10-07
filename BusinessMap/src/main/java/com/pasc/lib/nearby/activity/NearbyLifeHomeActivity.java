package com.pasc.lib.nearby.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.pasc.lib.base.activity.BaseActivity;
import com.pasc.lib.base.util.DensityUtils;
import com.pasc.lib.base.util.ScreenUtils;
import com.pasc.lib.base.util.StatusBarUtils;
import com.pasc.lib.lbs.location.bean.PascLocationData;
import com.pasc.lib.nearby.NearByLifeManager;
import com.pasc.lib.nearby.NearbyConstant;
import com.pasc.lib.nearby.R;
import com.pasc.lib.nearby.adapter.SearchNearbyAdapter;
import com.pasc.lib.nearby.bean.NearByLifePoi;
import com.pasc.lib.nearby.map.GpsInfoBean;
import com.pasc.lib.nearby.map.LocatorWrapper;
import com.pasc.lib.nearby.map.base.Locator;
import com.pasc.lib.nearby.net.NearbyBiz;
import com.pasc.lib.nearby.utils.AMapUtil;
import com.pasc.lib.nearby.utils.NearByUtil;
import com.pasc.lib.nearby.utils.RomUtil;
import com.pasc.lib.nearby.widget.BottomSheetBehavior;
import com.pasc.lib.nearby.widget.tablayout.TabLayout;
import com.pasc.lib.widget.dialognt.LoadingDialog;
import com.pasc.lib.widget.toast.Toasty;
import com.pasc.lib.widget.toolbar.PascToolbar;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.subscribers.DisposableSubscriber;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

//import com.amap.api.maps.MapView;

/**
 * 附近的生活主页
 * Created by huanglihou519 on 17/11/24.
 * Modified by qinguohuai on 18/11/23.
 */
//@Route(path = NearbyConstant.JUMP_NEARBY_MAP_MAIN_KEY)
@Route(path = "/nearby/map/main")
public class NearbyLifeHomeActivity
    extends BaseActivity implements PoiSearch.OnPoiSearchListener,
    GeocodeSearch.OnGeocodeSearchListener {
  private static final String TAG = "NearbyLifeHomeActivity";
  //MapView mapView;
  TextureMapView mapView;
  TabLayout poiClassTabLayout;
  View tabLayoutBelowDivider;
  RecyclerView mapRecyclerView;
  LinearLayout bottomSheet;
  PascToolbar titleBar;
  FrameLayout lookLocationListBar;
  View locationDetailbar;
  TextView itemAddressTextView;
  TextView goNavigationButton;
  TextView itemTelTextView;
  TextView itemTitleTextView;
  ImageView arrow;
  AMap aMap;
  //当前默认定位点
  private LatLonPoint curLatLonPoint;
  private String currentKey;
  private PoiSearch.Query targetSearchQuery;

  private static final String SEARCH_TYPE_BY_TXT = "searchTypeByTxt";
  private static final String SEARCH_TYPE_BY_CODE = "searchTypeByCode";
  private String[] poiKey;
  private String[] poiInfo;

  private SearchNearbyAdapter adapter;
  private PoiSearch poiSearch;
  private Handler uiHandler;
  private BottomSheetBehavior bottomSheetBehavior;
  private int typeIndex = 0;
  private String cityName;
  private PoiItem currentPoiItem;
  private Marker lastMarker = null;

  private String title;

  private LinkedList<Runnable> pendingTasks = new LinkedList<>();
  private LinkedList<Runnable> pendingTasksPermissionSetting = new LinkedList<>();

  private CompositeDisposable disposables = new CompositeDisposable();

  private PoiResult currentPoiResult;
  private ImageButton mCloseImageButton;
  private TextView mNearbyListItemTitle;
  private LoadingDialog mLoadingDialog;
  private NearByLifePoi currentPoiInfo;
  private int searchRange = 20000;

  @Override
  protected int layoutResId() {
    return R.layout.nearby_activity_home_map;
  }

  @Override
  protected void onInit(@Nullable Bundle bundle) {
    StatusBarUtils.setStatusBarColor(this, true);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //mapView = (MapView) findViewById(R.id.nearby_search_map_view);
    mapView = (TextureMapView) findViewById(R.id.nearby_search_map_view);
    poiClassTabLayout = (TabLayout) findViewById(R.id.nearby_poi_class_tab_layout);
    tabLayoutBelowDivider = (View) findViewById(R.id.nearby_tab_layout_below_divider);
    mapRecyclerView = (RecyclerView) findViewById(R.id.nearby_map_list_recycler_view);
    bottomSheet = (LinearLayout) findViewById(R.id.nearby_bottom_sheet);
    titleBar = (PascToolbar) findViewById(R.id.nearby_title_bar);
    lookLocationListBar = (FrameLayout) findViewById(R.id.nearby_look_location_list_bar);
    locationDetailbar = (View) findViewById(R.id.nearby_location_detail_bar);
    itemAddressTextView = (TextView) findViewById(R.id.nearby_tv_item_address);
    goNavigationButton = (TextView) findViewById(R.id.nearby_go_navigation_button);
    itemTelTextView = (TextView) findViewById(R.id.nearby_tv_item_tel);
    itemTitleTextView = (TextView) findViewById(R.id.nearby_tv_item_title);
    arrow = (ImageView) findViewById(R.id.nearby_arrow);
    mNearbyListItemTitle = findViewById(R.id.nearby_list_item_title);
    lookLocationListBar.setVisibility(View.GONE);
    if (NearByLifeManager.getInstance().getSearchRange() > 0) {
      searchRange = NearByLifeManager.getInstance().getSearchRange();
    }
    uiHandler = new Handler();
    mapView.onCreate(savedInstanceState);
    if (!NearByLifeManager.getInstance().enable()) {
      finish();
      return;
    }

    getParingLotList();
    String searchTypeList = getSearchTypeList();
    getSearchArray(searchTypeList);

    getBundleData();
    getCurrentKey();
    if (!TextUtils.isEmpty(searchTypeList)) {
      initTabLayout();
    }
    setupMap();
    initMapRecyclerView();
    setupBottomSheet();
    if (TextUtils.isEmpty(NearByLifeManager.getInstance().getTitle())) {
      titleBar.setTitle(title);
    } else {
      titleBar.setTitle(NearByLifeManager.getInstance().getTitle());
    }
    mCloseImageButton = titleBar.addCloseImageButton();
    mCloseImageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onBackPressed();
      }
    });
    titleBar.enableUnderDivider(false);
    goNavigationButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        PoiItem poiItem = (PoiItem) goNavigationButton.getTag();

        NearbyNavigationActivity.start(NearbyLifeHomeActivity.this, curLatLonPoint,
            poiItem.getLatLonPoint(), cityName, poiItem.getTitle());
      }
    });
    String barTips = getString(R.string.nearby_tab_bottom_sheet_tips);
    barTips = String.format(barTips, title);
    mNearbyListItemTitle.setText(barTips);

    searchLocation();
  }

  private void getParingLotList() {
    //poi检索列表
    Disposable subscribe = NearbyBiz.getParingLotList().map(new Function<List<String>,
        String[]>() {
      @Override
      public String[] apply(List<String> strings) throws Exception {
        String[] s = new String[] {};
        if (strings != null) {
          s = new String[strings.size()];
          strings.toArray(s);
        }
        return s;
      }
    }).subscribe(new Consumer<String[]>() {
      @Override
      public void accept(String[] strings) throws Exception {
        if (strings != null && strings.length > 0) {
          poiInfo = strings;
          //保存poi info
          StringBuilder stringBuilder = new StringBuilder();
          for (int i = 0; i < strings.length; i++) {
            if (i == strings.length - 1) {
              stringBuilder.append(strings[i]);
            } else {
              stringBuilder.append(strings[i]).append(",");
            }
          }

          NearByUtil.saveStrToSP(NearbyConstant.NEAR_HOME_SEARCH_TYPE_KEY,
              stringBuilder.toString());
        }
      }
    }, new Consumer<Throwable>() {
      @Override
      public void accept(Throwable throwable) throws Exception {
        Log.d(TAG, "accept: throwable -> " + throwable.getMessage());
      }
    });
    disposables.add(subscribe);
  }

  private String getSearchTypeList() {
    if (NearByLifeManager.getInstance().getData() != null) {
      return obtainTabStr();
    }
    //get search type data from cache
    String searchTypeList = NearByUtil.getStrCacheFromSP(NearbyConstant.NEAR_HOME_SEARCH_TYPE_KEY);
    if (TextUtils.isEmpty(searchTypeList)) {
      searchTypeList = NearByLifeManager.getInstance().getSearchTypeList();
      if (TextUtils.isEmpty(searchTypeList)) {
        searchTypeList = NearbyConstant.NEAR_DEFAULT_SEARCH_TYPE_LIST;
      }
    }
    searchTypeList = showDataList(searchTypeList);
    return searchTypeList;
  }

  /**
   * 得到搜索项列表Data
   */
  private void getSearchArray(String searchListStr) {
    if (!TextUtils.isEmpty(searchListStr)) {
      String[] searchArray = searchListStr.split(",");
      poiInfo = searchArray;
      poiKey = searchArray;
    }
  }

  // searchTypeByTxt 、 searchTypeByCode
  private void getBundleData() {
    typeIndex = 0;
    if (getIntent() != null && getIntent().getExtras() != null) {
      String searchTypeByTxt = getIntent().getExtras().getString(SEARCH_TYPE_BY_TXT);
      String searchTypeByCode = getIntent().getExtras().getString(SEARCH_TYPE_BY_CODE);

      if (!TextUtils.isEmpty(searchTypeByTxt)) {
        typeIndex = getTypeIndex(searchTypeByTxt);
        return;
      }

      if (!TextUtils.isEmpty(searchTypeByCode)) {
        String typeStr = NearbyConstant.getSearchTypeTxt(searchTypeByCode);
        typeIndex = getTypeIndex(typeStr);
        return;
      }
    }
  }

  private int getTypeIndex(String typeString) {
    int tabIndex = 0;
    if (poiInfo != null && poiInfo.length > 1) {
      for (int i = 0; i < poiInfo.length - 1; i++) {
        if (!TextUtils.isEmpty(poiInfo[i]) && poiInfo[i].startsWith(typeString)) {
          tabIndex = i;
          break;
        }
      }
    }
    // ToastUtils.toastMsg("tabIndex=" + typeIndex);
    return tabIndex;
  }

  private void getCurrentKey() {
    if (poiInfo != null && poiInfo.length > typeIndex) {
      title = poiInfo[typeIndex];
      currentKey = poiKey[typeIndex];
      currentPoiInfo = getCurrentPoi();
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    while (pendingTasks.size() > 0) {
      Runnable task = pendingTasks.remove();
      uiHandler.post(task);
    }
  }

  private void setupMap() {
    aMap = mapView.getMap();
    aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
      @Override
      public boolean onMarkerClick(Marker marker) {
        final PoiItem info = (PoiItem) marker.getObject();
        if (info == null) {
          return false;
        }
        if (info == currentPoiItem) {
          locationDetailbar.setVisibility(View.INVISIBLE);
          bottomSheet.setVisibility(View.VISIBLE);
          aMap.animateCamera(
              CameraUpdateFactory.newLatLngZoom(AMapUtil.convertToLatLng(info
                      .getLatLonPoint()),
                  16));
          return true;
        }

        if (lastMarker != null) {
          lastMarker.setIcon(
              BitmapDescriptorFactory.fromResource(R.drawable
                  .nearby_ic_map_mark_default));
        }
        lastMarker = marker;
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable
            .nearby_ic_target_location_selected));
        showLocationDetail(info);
        return true;
      }
    });
    aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
      @Override
      public void onMapLoaded() {
        // LatLng ShenZhenPoints = new LatLng(722.61667,7114.06667);
        // 设置一个超出范围的经纬度使地图不显示出地图层而是背景网格
        //LatLng centerPoints = new LatLng(722.61667, 7114.06667);
        LatLng centerPoints = new LatLng(722.61667, 7114.06667);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerPoints, 16));
      }
    });
    aMap.getUiSettings().setZoomControlsEnabled(false);
    aMap.getUiSettings().setLogoBottomMargin(-100);
  }

  private void setupBottomSheet() {
    bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
    //展开一半
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_ANCHOR_POINT);
    lookLocationListBar.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_ANCHOR_POINT) {
          bottomSheetBehavior.setState(BottomSheetBehavior.STATE_ANCHOR_POINT);
        } else {
          bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
      }
    });
    bottomSheetBehavior.setHideable(false);
    bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_ANCHOR_POINT) {
          arrow.animate().rotation(0).setDuration(200).start();
          lookLocationListBar.setVisibility(View.GONE);
        } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
          arrow.animate().rotation(180).setDuration(200).start();
          lookLocationListBar.setVisibility(View.VISIBLE);
        }
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {

      }
    });

    getWindow().getDecorView().post(new Runnable() {
      @Override
      public void run() {
        uiHandler.post(new Runnable() {
          @Override
          public void run() {

            ViewGroup.LayoutParams bottomSheetLayoutParams = bottomSheet
                .getLayoutParams();
            bottomSheetLayoutParams.height =
                mapView.getHeight() - titleBar.getHeight() + DensityUtils.dp2px(
                    getResources().getDimension(R.dimen
                        .pasc_list_single_text_item_height));
            bottomSheet.setLayoutParams(bottomSheetLayoutParams);
            ViewGroup.LayoutParams recyclerLayoutParams = mapRecyclerView
                .getLayoutParams();
            if (RomUtil.isEMUI3_1()) {
              recyclerLayoutParams.height = ScreenUtils.getScreenHeight()
                  - titleBar.getHeight()
                  - poiClassTabLayout.getHeight() - ScreenUtils
                  .getStatusBarHeight(NearbyLifeHomeActivity.this)
                  - ScreenUtils.getStatusBarHeight(NearbyLifeHomeActivity.this);
            } else {
              recyclerLayoutParams.height = ScreenUtils.getScreenHeight()
                  - titleBar.getHeight()
                  - poiClassTabLayout.getHeight()
                  - ScreenUtils.getStatusBarHeight(NearbyLifeHomeActivity.this);
            }
            mapRecyclerView.setLayoutParams(recyclerLayoutParams);
          }
        });
      }
    });

    //处理选择tab需要居中的问题
    poiClassTabLayout.getViewTreeObserver().addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            poiClassTabLayout.selectTab(poiClassTabLayout.getTabAt(typeIndex));
            poiClassTabLayout.getViewTreeObserver()
                .removeOnGlobalLayoutListener(this);
          }
        });
  }

  private void initTabLayout() {
    //支持滑动
    poiClassTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

    for (int i = 0; i < poiInfo.length; i++) {
      boolean isSelected = false;
      if (i == typeIndex) {
        isSelected = true;
      }
      poiClassTabLayout.addTab(poiClassTabLayout.newTab().setText(poiInfo[i]).setTag
          (i), isSelected);
    }
    //poiClassTabLayout.getTabAt(typeIndex).select();
    //poiClassTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    poiClassTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override
      public void onTabSelected(TabLayout.Tab tab) {
        int index = (int) tab.getTag();
        currentKey = poiKey[index];
        currentPoiInfo = getCurrentPoi();
        title = poiInfo[index];
        if (TextUtils.isEmpty(NearByLifeManager.getInstance().getTitle())) {
          titleBar.setTitle(title);
        } else {
          titleBar.setTitle(NearByLifeManager.getInstance().getTitle());
        }
        String barTips = getString(R.string.nearby_tab_bottom_sheet_tips);
        barTips = String.format(barTips, title);
        mNearbyListItemTitle.setText(barTips);
        targetSearchQuery = new PoiSearch.Query(currentKey, "", cityName);
        targetSearchQuery.setPageNum(0);
        targetSearchQuery.setPageSize(50);
        try{
          poiSearch = new PoiSearch(NearbyLifeHomeActivity.this, targetSearchQuery);
          poiSearch.setOnPoiSearchListener(NearbyLifeHomeActivity.this);
          poiSearch.setBound(new PoiSearch.SearchBound(curLatLonPoint, searchRange));
          poiSearch.searchPOIAsyn();
        }catch (Exception e){
          e.printStackTrace();
        }

        if (NearByUtil.isNetworkAvailable(NearbyLifeHomeActivity.this)) {
          showLoading("搜索中");
        } else {
          Toasty.init(NearbyLifeHomeActivity.this)
              .setMessage(getResources().getString(R.string.nearby_network_unavailable))
              .show();
        }
      }

      @Override
      public void onTabUnselected(TabLayout.Tab tab) {
      }

      @Override
      public void onTabReselected(TabLayout.Tab tab) {

      }
    });
  }

  private void searchLocation() {
    final Runnable searchTask = new Runnable() {
      @Override
      public void run() {
        if (!NearByUtil.isNetworkAvailable(NearbyLifeHomeActivity.this)) {
          Toasty.init(NearbyLifeHomeActivity.this)
              .setMessage(getResources().getString(R.string.nearby_network_unavailable))
              .show();
          return;
        }
        showLoading("搜索中");
        Locator.doLocation(NearbyLifeHomeActivity.this, true)
            .subscribeWith(new DisposableSubscriber<PascLocationData>() {
              @Override
              public void onNext(PascLocationData location) {
                curLatLonPoint =
                    new LatLonPoint(location.getLatitude(), location
                        .getLongitude());
                cityName = location.getCity();
                if (NearByLifeManager.getInstance().getDefaultLoc() != null) {
                  try{
                    curLatLonPoint = NearByLifeManager.getInstance().getDefaultLoc();
                    GeocodeSearch geocodeSearch = new GeocodeSearch(NearbyLifeHomeActivity.this);
                    geocodeSearch.setOnGeocodeSearchListener(NearbyLifeHomeActivity.this);
                    RegeocodeQuery query = new RegeocodeQuery(curLatLonPoint, 25, GeocodeSearch.AMAP);
                    geocodeSearch.getFromLocationAsyn(query);
                  }catch (Exception e){
                    e.printStackTrace();
                  }

                }

                targetSearchQuery = new PoiSearch.Query(currentKey, "", cityName);
                targetSearchQuery.setPageNum(0);
                targetSearchQuery.setPageSize(50);
                try {
                  poiSearch = new PoiSearch(NearbyLifeHomeActivity.this,
                          targetSearchQuery);
                  poiSearch.setOnPoiSearchListener(NearbyLifeHomeActivity.this);
                  poiSearch.setBound(new PoiSearch.SearchBound(curLatLonPoint,
                          searchRange));
                  poiSearch.searchPOIAsyn();
                }catch (Exception e){
                  e.printStackTrace();
                }

              }

              @Override
              public void onError(Throwable t) {
                Log.e(TAG, "onError: throwable " + t);
                dismissDialogs();
              }

              @Override
              public void onComplete() {
              }
            });
      }
    };

    disposables.add(
        LocatorWrapper.prepareLocation(this).subscribe(new Consumer<Locator
            .PrepareStatus>() {
          @Override
          public void accept(Locator.PrepareStatus prepareStatus)
              throws Exception {
            switch (prepareStatus) {
              case PERMISSION_GRANTED:
                runOnUiThread(searchTask);
                break;
              case OPEN_GPS_SETTING:
                pendingTasks.push(searchTask);
                break;
              case OPEN_PERMISSION_SETTING:
                pendingTasksPermissionSetting.push(searchTask);
                break;
              case CANCEL_DIALOG:
              case PERMISSION_NOT_GRANTED:
                notLocationPermission();
                break;
            }
          }
        }));
  }

  private void notLocationPermission() {
    Toasty.init(NearbyLifeHomeActivity.this).setMessage("没有定位权限 无法使用该功能").show();
    finish();
  }

  private void setCurrentLocationMarker() {
    final LatLng currentLatLng = new LatLng(curLatLonPoint.getLatitude(), curLatLonPoint
        .getLongitude());
    MarkerOptions currentMO = new MarkerOptions();
    currentMO.anchor(0.5f, 1);
    currentMO.position(currentLatLng);
    currentMO.draggable(true);
    currentMO.icon(BitmapDescriptorFactory.fromResource(R.drawable.nearby_ic_current_location));
    Marker currentMarker = aMap.addMarker(currentMO);
    currentPoiItem = new PoiItem("current", curLatLonPoint, "目前你所在的位置", "当前位置");
    currentPoiItem.setDistance(0);
    currentPoiItem.setTel("无");
    currentMarker.setObject(currentPoiItem);
    uiHandler.postDelayed(new Runnable() {
      @Override public void run() {
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
      }
    },500);

  }

  private void addPoiMarker(PoiResult poiResult) {
    int resID = R.drawable.nearby_ic_map_mark_default;
    if (!TextUtils.isEmpty(currentKey)) {
      if (currentKey.contains("银行")) {
        resID = R.drawable.nearby_ic_map_mark_bank;
      } else if (currentKey.contains("药店") || currentKey.contains("医保")) {
        resID = R.drawable.nearby_ic_map_mark_drugst;
      } else if (currentKey.contains("便利店")) {
        resID = R.drawable.nearby_ic_map_mark_store;
      } else if (currentKey.contains("邮局")) {
        resID = R.drawable.nearby_ic_map_mark_post;
      }
      if (currentPoiInfo != null && currentPoiInfo.resId != 0) {
        resID = currentPoiInfo.resId;
      }
    }
    addCustomMarker(poiResult.getPois(), resID);
  }

  private Marker addUnSelectedPoiMarkerByLatlng(LatLng latLng, int resourceID) {
    MarkerOptions targetMO = new MarkerOptions();
    targetMO.anchor(0.5f, 1);
    targetMO.position(latLng);
    targetMO.draggable(true);

    targetMO.icon(BitmapDescriptorFactory.fromResource(resourceID));
    Marker marker = aMap.addMarker(targetMO);
    return marker;
  }

  private void initMapRecyclerView() {
    mapRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    adapter = new SearchNearbyAdapter(this, new ArrayList<PoiItem>());
    mapRecyclerView.setAdapter(adapter);

    adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
      @Override
      public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {

        if (view.getId() == R.id.nearby_rl_middle) {
          NearbyPoiMapDetailActivity.start(NearbyLifeHomeActivity.this, curLatLonPoint, adapter
              .getItem(position), position, title);
        } else if (view.getId() == R.id.nearby_tv_near_loc) {
          PoiItem poiItem = adapter.getItem(position);
          if (poiItem != null) {
            NearbyNavigationActivity.start(NearbyLifeHomeActivity.this,
                curLatLonPoint,
                poiItem.getLatLonPoint(),
                poiItem.getCityName(),
                poiItem.getTitle());
          }
        }
      }
    });

    ViewGroup.LayoutParams params =
        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils
            .dp2px(50));
    View footView =
        View.inflate(NearbyLifeHomeActivity.this, R.layout.nearby_searchall_item_foot_view, null);
    footView.setLayoutParams(params);
    //adapter.removeAllFooterView();
    adapter.addFooterView(footView);
  }

  private void showLocationDetail(PoiItem poiItem) {
    locationDetailbar.setVisibility(View.VISIBLE);
    bottomSheet.setVisibility(View.INVISIBLE);
    if (poiItem == null) {
      return;
    }
    DecimalFormat df = new DecimalFormat("0.0");
    String distance =
        poiItem.getDistance() > 1000 ? df.format((float) poiItem.getDistance() / 1000) +
            "km"
            : poiItem.getDistance() + "m";
    itemAddressTextView.setText(new StringBuilder().append(distance)
        .append(" | ")
        .append(poiItem.getSnippet())
        .append("")
        .toString());
    itemTelTextView.setText(!TextUtils.isEmpty(poiItem.getTel()) ? poiItem.getTel() : "暂无");
    itemTitleTextView.setText(poiItem.getTitle());
    goNavigationButton.setTag(poiItem);
    LatLng latLng = new LatLng(poiItem.getLatLonPoint().getLatitude(),
        poiItem.getLatLonPoint().getLongitude());
    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
  }

  private void showBottomSheet() {
    locationDetailbar.setVisibility(View.INVISIBLE);
    bottomSheet.setVisibility(View.VISIBLE);

    if (lastMarker != null) {
      lastMarker.setIcon(
          BitmapDescriptorFactory.fromResource(R.drawable.nearby_ic_map_mark_default));
    }

    //        if (currentPoiResult != null) {
    //            addPoiMarker(currentPoiResult);
    //        }
    //        setCurrentLocationMarker();
    LatLng currentLatLng = new LatLng(curLatLonPoint.getLatitude(), curLatLonPoint
        .getLongitude());
    aMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
  }

  @Override
  protected void onResume() {
    super.onResume();
    StatusBarUtils.setStatusBarColor(this, true);
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
    disposables.clear();
    uiHandler.removeCallbacksAndMessages(null);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onPoiSearched(PoiResult poiResult, int i) {
    if (i == 1000) {
      if (poiResult != null
          && poiResult.getQuery() != null
          && poiResult.getPois() != null) {
        aMap.clear();
        setCurrentLocationMarker();
        currentPoiResult = poiResult;
        adapter.getData().clear();
        Log.d("onPoiSearched",
            "currentkey  " + currentKey + " poiResult " + poiResult.getQuery().getQueryString());

        if (!ifHasCustomData()) {
          addPoiMarker(poiResult);
          adapter.getData().addAll(poiResult.getPois());
        } else {
          List<PoiItem> poiItems = obtainPoiItems();
          addCustomMarker(poiItems, currentPoiInfo.resId);
          adapter.getData().addAll(poiItems);
        }
        adapter.notifyDataSetChanged();
        Log.d("onPoiSearched", "poi size:" + poiResult.getPois().size());
      }
    } else if (i == 27) {
      Log.d("onPoiSearched", "network error");
    } else if (i == 32) {
      Log.d("onPoiSearched", "key error");
    } else {
      Log.d("onPoiSearched", "other error");
    }
    //在小米9.0上dismissloading的时候会出现闪烁，增加延时，不会出现。
    uiHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
        dismissLoading();
      }
    }, 500);
  }

  @Override
  public void onPoiItemSearched(PoiItem poiItem, int i) {

  }

  private boolean isLocationDetailShowed() {
    return locationDetailbar.getVisibility() == View.VISIBLE;
  }

  @Override
  public void onBackPressed() {
    if (isLocationDetailShowed()) {
      showBottomSheet();
      return;
    }
    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
      bottomSheetBehavior.setState(BottomSheetBehavior.STATE_ANCHOR_POINT);
      return;
    }
    super.onBackPressed();
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    while (pendingTasksPermissionSetting.size() > 0) {
      final Runnable task = pendingTasksPermissionSetting.remove();
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
          != PackageManager.PERMISSION_GRANTED ||
          ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
              != PackageManager.PERMISSION_GRANTED) {
        //表示未授权时
        notLocationPermission();
        return;
      }
      Disposable subscribe = LocatorWrapper.prepareLocation(this)
          .subscribe(new Consumer<Locator.PrepareStatus>() {
            @Override
            public void accept(Locator.PrepareStatus prepareStatus)
                throws Exception {
              switch (prepareStatus) {
                case PERMISSION_GRANTED:
                  uiHandler.post(task);
                  break;
                default:
                  notLocationPermission();
                  break;
              }
            }
          });
      disposables.add(subscribe);
    }
  }

  @Override
  public void showLoading(String msg) {
    if (mLoadingDialog == null) {
      mLoadingDialog = new LoadingDialog(this, msg);
    }
    mLoadingDialog.setContent(msg);
    if (!mLoadingDialog.isShowing()) {
      mLoadingDialog.show();
    }
  }

  @Override
  public void dismissLoading() {
    if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
      mLoadingDialog.dismiss();
    }
  }

  private String showDataList(String str) {
    List<String> orginList = new ArrayList<>();
    List<String> configList = new ArrayList<>();
    String[] searchArray = str.split(",");
    for (int i = 0; i < searchArray.length; i++) {
      orginList.add(searchArray[i]);
    }
    if (!TextUtils.isEmpty(str)) {

      if (!NearByLifeManager.getInstance().showStore()) {
        configList.add("24H便利店");
      }
      if (!NearByLifeManager.getInstance().showPublicWashroom()) {
        configList.add("公共卫生间");
      }
      if (!NearByLifeManager.getInstance().showBank()) {
        configList.add("银行");
      }
      if (!NearByLifeManager.getInstance().showAtm()) {
        configList.add("ATM/自助银行");
      }
      if (!NearByLifeManager.getInstance().showChargingPile()) {
        configList.add("充电桩");
      }
      if (!NearByLifeManager.getInstance().showPhotoStudio()) {
        configList.add("照相馆");
      }
      if (!NearByLifeManager.getInstance().showGasStation()) {
        configList.add("加油站");
      }
      if (!NearByLifeManager.getInstance().showCarCarePoint()) {
        configList.add("机动车维修点");
      }
      if (!NearByLifeManager.getInstance().showPharmacy()) {
        configList.add("周边药店");
      }
      if (!NearByLifeManager.getInstance().showTicketSales()) {
        configList.add("火车票代售点");
      }
      if (!NearByLifeManager.getInstance().showSocialWelfareInstitute()) {
        configList.add("社会福利院");
      }
      if (!NearByLifeManager.getInstance().showPostOffice()) {
        configList.add("邮局");
      }
      if (!NearByLifeManager.getInstance().showMuseum()) {
        configList.add("博物馆");
      }
      if (!NearByLifeManager.getInstance().showMedicalInsuranceNetwork()) {
        configList.add("医保网店");
      }
      if (!NearByLifeManager.getInstance().showEquipmentOutlet()) {
        configList.add("设备网点");
      }
      if (!NearByLifeManager.getInstance().showDesignatedDriver()) {
        configList.add("代驾");
      }
      orginList.removeAll(configList);
      String result = "";
      for (int i = 0; i < orginList.size(); i++) {
        if (i < orginList.size() - 1) {
          result += orginList.get(i) + ",";
        } else {
          result += orginList.get(i);
        }
      }
      return result;
    }
    return "";
  }

  private List<PoiItem> obtainPoiItems() {
    if (currentPoiInfo == null) {
      return null;
    }
    List<PoiItem> items = new ArrayList<>();
    for (int i = 0; i < currentPoiInfo.locs.size(); i++) {
      int distance =
          NearByUtil.getDistance(curLatLonPoint.getLatitude(), curLatLonPoint.getLongitude(),
              currentPoiInfo.locs.get(i).latitude, currentPoiInfo.locs.get(i).longitude);
      if (distance > searchRange) {
        continue;
      }
      LatLonPoint latLonPoint = new LatLonPoint(currentPoiInfo.locs.get(i).latitude
          , currentPoiInfo.locs.get(i).longitude);
      PoiItem poiItem = new PoiItem(currentKey, latLonPoint, currentPoiInfo.locs.get(i).locName,
          currentPoiInfo.locs.get(i).locAddr);

      poiItem.setDistance(distance);
      poiItem.setEnter(curLatLonPoint);
      poiItem.setExit(latLonPoint);

      items.add(poiItem);
    }
    Collections.sort(items, comparator);
    return items;
  }

  private String obtainTabStr() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < NearByLifeManager.getInstance().getData().size(); i++) {
      if (i == NearByLifeManager.getInstance().getData().size() - 1) {
        sb.append(NearByLifeManager.getInstance().getData().get(i).name);
      } else {
        sb.append(NearByLifeManager.getInstance().getData().get(i).name).append(",");
      }
    }
    return sb.toString();
  }

  private void addCustomMarker(List<PoiItem> items, @DrawableRes int resId) {
    LatLngBounds.Builder builder = new LatLngBounds.Builder();

    for (PoiItem poiItem : items) {
      LatLng targetLL = new LatLng(poiItem.getLatLonPoint().getLatitude(),
          poiItem.getLatLonPoint().getLongitude());
      Marker marker = addUnSelectedPoiMarkerByLatlng(targetLL, resId);
      marker.setObject(poiItem);
      builder.include(targetLL);
    }
    aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));
  }

  private boolean ifHasCustomData() {
    if (NearByLifeManager.getInstance().getData() == null || NearByLifeManager.getInstance()
        .getData()
        .isEmpty()) {
      return false;
    }
    if (TextUtils.isEmpty(currentKey)) {
      return false;
    }
    for (NearByLifePoi content : NearByLifeManager.getInstance().getData()) {
      if (currentKey.equals(content.name) && (content.locs != null && !content.locs.isEmpty())) {
        return true;
      }
    }
    return false;
  }

  private NearByLifePoi getCurrentPoi() {
    if (NearByLifeManager.getInstance().getData() == null || NearByLifeManager.getInstance()
        .getData()
        .isEmpty()) {
      return NearByLifePoi.builder().name(currentKey).build();
    }
    for (NearByLifePoi content : NearByLifeManager.getInstance().getData()) {
      if (currentKey.equals(content.name)) {
        return content;
      }
    }
    return NearByLifePoi.builder().name(currentKey).build();
  }

  Comparator comparator = new Comparator<PoiItem>() {
    @Override public int compare(PoiItem o, PoiItem t1) {
      return o.getDistance() - t1.getDistance();
    }
  };

  @Override public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
    if (isDestroyed()) {
      return;
    }
    RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
    cityName = regeocodeAddress.getCity();
    Log.d("addr ", " addr " + cityName);
  }

  @Override public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

  }
}
