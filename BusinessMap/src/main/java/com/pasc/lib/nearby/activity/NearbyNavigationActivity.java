package com.pasc.lib.nearby.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnPageChangeListener;
import com.google.gson.Gson;
import com.pasc.lib.base.activity.BaseActivity;
import com.pasc.lib.base.util.DensityUtils;
import com.pasc.lib.base.util.DeviceUtils;
import com.pasc.lib.base.util.StatusBarUtils;
import com.pasc.lib.base.util.ToastUtils;
import com.pasc.lib.nearby.NearbyConstant;
import com.pasc.lib.nearby.R;
import com.pasc.lib.nearby.map.GpsInfoBean;
import com.pasc.lib.nearby.map.overlay.BusRouteOverlay;
import com.pasc.lib.nearby.map.overlay.DrivingRouteOverlay;
import com.pasc.lib.nearby.map.overlay.RideRouteOverlay;
import com.pasc.lib.nearby.map.overlay.RouteOverlay;
import com.pasc.lib.nearby.map.overlay.WalkRouteOverlay;
import com.pasc.lib.nearby.utils.AMapUtil;
import com.pasc.lib.nearby.utils.NearByUtil;
import com.pasc.lib.widget.dialog.OnCloseListener;
import com.pasc.lib.widget.dialog.OnSingleChoiceListener;
import com.pasc.lib.widget.dialog.bottomchoice.BottomChoiceDialogFragment;
import com.pasc.lib.widget.toolbar.PascToolbar;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * 分类地图导航
 * Created by huanglihou519 on 17/12/5.
 */
public class NearbyNavigationActivity extends BaseActivity {
    private static final String START_POINT_KEY = "start_point";
    private static final String END_POINT_KEY = "end_point";
    private static final String CITY_NAME_KEY = "city_name_key";
    private static final String TO_SITE_KEY = "to_site_key";
    private static final String EVENT_KEY = "eventId";
    private static final String EVENT_MAP_KEY = "eventMapId";

    TabLayout bottomNavigation;
    //MapView mapView;
    TextureMapView mapView;

    //LinearLayout pathShowPad;
    PascToolbar titleBar;
    private ImageButton mCloseImageButton;
    private GpsInfoBean currentGpsInfo;
//    private FrameLayout pathShowPadDrive;

    ConvenientBanner pathShowPadBus;
    private int position = 0;//公交路线方案翻页index
    private View v_divider_line;


    public static void start(Activity activity, LatLonPoint startPoint, LatLonPoint endPoint,
            String cityName, String toSite) {
        Intent intent = new Intent(activity, NearbyNavigationActivity.class);
        intent.putExtra(START_POINT_KEY, startPoint)
                .putExtra(END_POINT_KEY, endPoint)
                .putExtra(CITY_NAME_KEY, cityName)
                .putExtra(TO_SITE_KEY, toSite);
        activity.startActivity(intent);
    }

    protected Handler uiHandler;
    protected String currentCityName = "常熟";
    protected AMap aMap;

    private Runnable emptyCommand = new Runnable() {

        @Override public void run() {
        }
    };

    RouteSearch routeSearch;
    protected LatLonPoint startPoint;
    protected LatLonPoint endPoint;
    protected String toSite;
    protected RouteSearch.FromAndTo fromAndTo;
    protected String mEventId;
    protected String mEventMapKey;

    public @interface Type {
        int BUS = 1;
        int WALK = 2;
        int DRIVE = 0;
        int BIKE = 3;
    }

    SparseArray<String> baiduModeArray = new SparseArray<>();

    private @Type int currentType = Type.DRIVE;
    protected Runnable driveRouteCommand = new Runnable() {
        @Override public void run() {
            // 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo,
                    RouteSearch.DRIVING_MULTI_STRATEGY_FASTEST_SAVE_MONEY_SHORTEST,
                    //同时使用速度优先、费用优先、距离优先三个策略计算路径。
                    null, null, "");
            if (routeSearch != null) routeSearch.calculateDriveRouteAsyn(query);

//            Map<String, String> map = new HashMap<>();
//            map.put(mEventMapKey, toSite);
            //EventUtils.onEvent(mEventId, "去这里-驾车", map);
        }
    };

    private Runnable busRouteCommand = new Runnable() {
        @Override public void run() {
            // 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            RouteSearch.BusRouteQuery query =
                    new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BUS_DEFAULT,
                            currentCityName, 0);
            if (routeSearch != null) routeSearch.calculateBusRouteAsyn(query);

//            Map<String, String> map = new HashMap<>();
//            map.put(mEventMapKey, toSite);
            //EventUtils.onEvent(mEventId, "去这里-公交", map);
        }
    };

    private Runnable bikeRouteCommand = new Runnable() {
        @Override public void run() {
            RouteSearch.RideRouteQuery query = new RouteSearch.RideRouteQuery(fromAndTo);
            if (routeSearch != null) routeSearch.calculateRideRouteAsyn(query);

//            Map<String, String> map = new HashMap<>();
//            map.put(mEventMapKey, toSite);
            //EventUtils.onEvent(mEventId, "去这里-骑行", map);
        }
    };

    private Runnable walkRouteCommand = new Runnable() {
        @Override public void run() {
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo);
            if (routeSearch != null) routeSearch.calculateWalkRouteAsyn(query);

//            Map<String, String> map = new HashMap<>();
//            map.put(mEventMapKey, toSite);
            //EventUtils.onEvent(mEventId, "去这里-步行", map);
        }
    };

    private final TabEntity[] tabEntities = new TabEntity[] {
            new TabEntity(Type.DRIVE, "驾车", R.drawable.nearby_navigation_select_drive,
                    driveRouteCommand),
            new TabEntity(Type.BUS, "公交", R.drawable.nearby_navigation_select_bus, busRouteCommand),
            new TabEntity(Type.WALK, "步行", R.drawable.nearby_navigation_select_walk, walkRouteCommand),
            new TabEntity(Type.BIKE, "骑行", R.drawable.nearby_navigation_select_bike, bikeRouteCommand)
    };

    LayoutInflater layoutInflater;

    private BottomChoiceDialogFragment mapAppSelectDialog;

    private InfoWindowData infoWindowData;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.nearby_activity_nav_layout);

        bottomNavigation = findViewById(R.id.navigation_bottom_navigation);
        //mapView = (MapView)findViewById(R.id.navigation_map_view);
        mapView = (TextureMapView)findViewById(R.id.navigation_map_view);
        titleBar = findViewById(R.id.nearby_title_bar);

//        pathShowPad = findViewById(R.id.path_show_pad);
        pathShowPadBus = findViewById(R.id.path_show_pad_bus);
//        pathShowPadDrive = findViewById(R.id.path_show_pad_drive);
        v_divider_line = findViewById(R.id.v_divider_line);

        getExtraData();
        fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        infoWindowData = new InfoWindowData(toSite);
        layoutInflater = LayoutInflater.from(NearbyNavigationActivity.this);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setLogoBottomMargin(-100);
        uiHandler = new Handler();
        baiduModeArray.put(Type.BUS, "transit");
        baiduModeArray.put(Type.DRIVE, "driving");
        baiduModeArray.put(Type.BIKE, "riding");
        baiduModeArray.put(Type.WALK, "walking");

        setupBottomNavigation();
        setupMapSelectDialog();
        setupMapListener();
        setupRouteSearch();

        mCloseImageButton = titleBar.addCloseImageButton();
        titleBar.enableUnderDivider(false);
        mCloseImageButton.setImageResource(R.drawable.nearby_ic_back_circle);
        mCloseImageButton.setBackgroundColor(Color.TRANSPARENT);

        mCloseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                onBackPressed();
            }
        });

        String locationStr = NearByUtil.getStrCacheFromSP(NearbyConstant.NEAR_CURRENT_LOCATION_KEY);
        Gson gson = new Gson();
        if (!TextUtils.isEmpty(locationStr)) {
            currentGpsInfo = gson.fromJson(locationStr, GpsInfoBean.class);
        }

        uiHandler.post(driveRouteCommand);
        aMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(AMapUtil.convertToLatLng(endPoint), 16));
    }

    @Override
    protected int layoutResId() {
        return R.layout.nearby_activity_nav_layout;
    }

    @Override
    protected void onInit(@Nullable Bundle bundle) {
        StatusBarUtils.setStatusBarColor(this, true);
    }

    public void getExtraData() {
        Intent intent = getIntent();
        if (intent != null) {
            startPoint = getIntent().getParcelableExtra(START_POINT_KEY);
            endPoint = getIntent().getParcelableExtra(END_POINT_KEY);
            currentCityName = getIntent().getStringExtra(CITY_NAME_KEY);
            toSite = getIntent().getStringExtra(TO_SITE_KEY);
            mEventId = getIntent().getStringExtra(EVENT_KEY);
            mEventMapKey = getIntent().getStringExtra(EVENT_MAP_KEY);
        }
    }

    private void goToMarket(String packName) {
        Uri uri = Uri.parse("market://details?id=" + packName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setupMapSelectDialog() {
        boolean hasGaodeMapApp = DeviceUtils.isAvilible(this, "com.autonavi.minimap");
        boolean hasBaiduMapApp = DeviceUtils.isAvilible(this, "com.baidu.BaiduMap");

        final ArrayList<CharSequence> items = new ArrayList<>();
        if (!hasGaodeMapApp && !hasBaiduMapApp) {
            items.add("高德地图");
            items.add("百度地图");
        }
        if (hasGaodeMapApp) {
            items.add("用高德地图导航");
        }
        if (hasBaiduMapApp) {
            items.add("用百度地图导航");
        }

        BottomChoiceDialogFragment.Builder builder = new BottomChoiceDialogFragment.Builder()
                .setItems(items)
                .setCloseText("关闭")
                .setOnSingleChoiceListener(new OnSingleChoiceListener<BottomChoiceDialogFragment>() {
                    @Override
                    public void onSingleChoice(BottomChoiceDialogFragment dialogFragment, int position) {
                        mapAppSelectDialog.dismiss();
                        String name = (String) items.get(position);
                        switch (name) {
                            case "高德地图":
                                goToMarket("com.autonavi.minimap");
                                break;
                            case "百度地图":
                                goToMarket("com.autonavi.minimap");
                                break;
                            case "用高德地图导航":
                                gotoGaode();
                                break;
                            case "用百度地图导航":
                                gotoBaidu();
                                break;
                            default:
                                break;
                        }

                    }
                })
                .setOnCloseListener(new OnCloseListener<BottomChoiceDialogFragment>() {

                    @Override
                    public void onClose(BottomChoiceDialogFragment dialogFragment) {
                        dialogFragment.dismiss();
                    }
                });
        if (!hasGaodeMapApp && !hasBaiduMapApp) {
            builder.setTitle("请选择地图下载");
        }
        mapAppSelectDialog = builder.build();
    }

    private void gotoGaode() {
        LatLng endLatLng = AMapUtil.convertToLatLng(endPoint);
        StringBuilder uri = new StringBuilder("amapuri://route/plan/?");
        uri.append("dlat=")
                .append(endLatLng.latitude)
                .append("&")
                .append("dlon=")
                .append(endLatLng.longitude)
                .append("&")
                .append("dname=")
                .append(toSite)
                .append("&")
                .append("t=")
                .append(currentType);
        Intent intent = new Intent("android.intent.action.VIEW",
                Uri.parse(uri.toString()));
        intent.setPackage("com.autonavi.minimap");
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.toastMsg("请先安装高德地图");
        }
    }

    private void gotoBaidu() {
        LatLng endLatLng = AMapUtil.convertToLatLng(endPoint);
        StringBuilder uri = new StringBuilder("baidumap://map/direction?");
        uri.append("destination=name:")
                .append(toSite)
                .append("|")
                .append("latlng:")
                .append(endLatLng.latitude)
                .append(",")
                .append(endLatLng.longitude)
                .append("&")
                .append("mode=")
                .append(baiduModeArray.get(currentType))
                .append("&")
                .append("target")
                .append(1);
        Intent intent = new Intent("android.intent.action.VIEW",
                Uri.parse(uri.toString()));
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.toastMsg("请先安装百度地图");
        }
    }

    private void setupBottomNavigation() {
        Observable.fromArray(tabEntities).map(new Function<TabEntity, TabLayout.Tab>() {
            @Override public TabLayout.Tab apply(TabEntity tabEntity) throws Exception {
                TabLayout.Tab tab =
                        bottomNavigation.newTab().setCustomView(R.layout.nearby_item_bottom_nav_way);
                View tabItemView = tab.getCustomView();
                if (tabItemView != null){
                    TextView text = tabItemView.findViewById(R.id.nearby_tab_txt);
                    text.setText(tabEntity.text);
                    ImageView icon = tabItemView.findViewById(R.id.nearby_tab_icon);
                    icon.setImageResource(tabEntity.iconResId);
                }
                tab.setTag(tabEntity);
                return tab;
            }
        }).subscribe(new Consumer<TabLayout.Tab>() {
            @Override public void accept(TabLayout.Tab tab) throws Exception {
                bottomNavigation.addTab(tab);
            }
        });
        bottomNavigation.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                ToastUtils.cancel();
                TabEntity tabEntity = (TabEntity) tab.getTag();
                if (tabEntity != null){
                    currentType = tabEntity.type;
                    uiHandler.post(tabEntity.command);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setupMapListener() {
        aMap.setInfoWindowAdapter(new AMap.InfoWindowAdapter() {
            @Override public View getInfoWindow(Marker marker) {
                InfoWindowData data = (InfoWindowData) marker.getObject();
                if (data == null){
                    return null;
                }

                View view = layoutInflater.inflate(R.layout.nearby_nav_window_pop, null);
                TextView poiName = (TextView) view.findViewById(R.id.poi_name);
                poiName.setText(data.title);
                View navigationStart = view.findViewById(R.id.navigation_start);
                navigationStart.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        mapAppSelectDialog.show(getSupportFragmentManager(), "BottomChoiceDialogFragment");
                    }
                });
                return view;
            }

            @Override public View getInfoContents(Marker marker) {
                return null;
            }
        });
        aMap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override public void onInfoWindowClick(Marker marker) {

            }
        });
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override public boolean onMarkerClick(Marker marker) {
                return marker.getObject() == null;
            }
        });
        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override public void onMapClick(LatLng latLng) {

            }
        });
    }

    private void setupMarket() {
        final Marker endMarker = aMap.addMarker(new MarkerOptions().position(AMapUtil.convertToLatLng(endPoint))
                .title(toSite)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.nearby_ic_nav_path_end))
                .draggable(true));
//        final Marker startMarker = aMap.addMarker(
//                new MarkerOptions().position(AMapUtil.convertToLatLng(startPoint))
//                        .title(toSite)
//                        .anchor(0.5f, 0.5f)
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.nearby_ic_nav_path_start))
//                        .draggable(true));
        uiHandler.post(new Runnable() {
            @Override public void run() {
                endMarker.hideInfoWindow();
                endMarker.showInfoWindow();
            }
        });
        aMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(AMapUtil.convertToLatLng(endPoint), 16));
    }

    private void initMap() {
        aMap.clear();
    }

    private static class TabEntity {
        public TabEntity(@Type int type, String text, int iconResId, Runnable command) {
            this.text = text;
            this.iconResId = iconResId;
            this.type = type;
            this.command = command;
        }

        public String text;
        public int iconResId;
        public @Type int type;
        public Runnable command;
    }

    public void setupRouteSearch() {
        try {
            routeSearch = new RouteSearch(this);
            routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
                @Override public void onBusRouteSearched(final BusRouteResult busRouteResult, int errorCode) {

                    initMap();
                    v_divider_line.setVisibility(View.VISIBLE);
                    pathShowPadBus.setVisibility(View.GONE);
//                pathShowPadDrive.removeAllViews();

                    if (errorCode == 1804 || errorCode == 1806) {
                        ToastUtils.toastMsg(R.string.nearby_network_unavailable);
                        return;
                    }
                    final List<BusPath> busPaths =busRouteResult==null|| busRouteResult.getPaths() == null ? new ArrayList<BusPath>()
                            : busRouteResult.getPaths();

                    if (busPaths.size() > 0) {
                        pathShowPadBus.setVisibility(View.VISIBLE);
                        ViewGroup.LayoutParams layoutParams = pathShowPadBus.getLayoutParams();
                        layoutParams.height = DensityUtils.dp2px(190);
                        pathShowPadBus.setLayoutParams(layoutParams);
                        if (busPaths.size() > 1) {
                            pathShowPadBus.setPageIndicator(new int[] {
                                    R.drawable.nearby_shape_path_unselected,
                                    R.drawable.nearby_shape_path_selected
                            });
                        }
                        pathShowPadBus.setPages(new CBViewHolderCreator() {
                                    @Override public Holder createHolder(View itemView) {
                                        return new BusPathViewHolder(itemView);
                                    }

                                    @Override public int getLayoutId() {
                                        return R.layout.nearby_item_nav_car_path;
                                    }
                                }, busPaths).setPointViewVisible(true)

                                .setOnPageChangeListener(new OnPageChangeListener() {
                                    @Override
                                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                            selectBusPath(busRouteResult, busPaths.get(position));
                                        }
                                    }

                                    @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                                    }

                                    @Override public void onPageSelected(int index) {
                                        //selectBusPath(busRouteResult, busPaths.get(index));
                                        position = index;
                                    }
                                });
                        //默认第一条路线
                        selectBusPath(busRouteResult, busPaths.get(0));
                    } else {
                        pathShowPadBus.setVisibility(View.GONE);
                        ToastUtils.toastMsg("暂未获取到公交信息");
                        setupMarket();
                    }

                }

                @Override
                public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int errorCode) {
                    initMap();
                    v_divider_line.setVisibility(View.VISIBLE);
                    pathShowPadBus.setVisibility(View.GONE);
//                pathShowPadDrive.removeAllViews();

                    if (errorCode == 1804 || errorCode == 1806) {
                        ToastUtils.toastMsg(R.string.nearby_network_unavailable);
                        return;
                    }

                    final List<DrivePath> drivePaths =driveRouteResult==null || driveRouteResult.getPaths() == null ? new ArrayList<DrivePath>()
                            : driveRouteResult.getPaths();

                    if (drivePaths.size() > 0){
                        pathShowPadBus.setVisibility(View.VISIBLE);
                        ViewGroup.LayoutParams layoutParams = pathShowPadBus.getLayoutParams();
                        layoutParams.height = DensityUtils.dp2px(150);
                        pathShowPadBus.setLayoutParams(layoutParams);
                        pathShowPadBus.setPages(new CBViewHolderCreator() {
                                    @Override public Holder createHolder(View itemView) {
                                        return new DrivePathViewHolder(itemView);
                                    }

                                    @Override public int getLayoutId() {
                                        return R.layout.nearby_item_nav_car_path;
                                    }
                                }, drivePaths).setPointViewVisible(false)
                                .setOnPageChangeListener(new OnPageChangeListener() {
                                    @Override
                                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                                    }
                                    @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                                    }

                                    @Override public void onPageSelected(int index) {

                                    }
                                });
                        //默认第一条路线
                        selectDrivePath(driveRouteResult, drivePaths.get(0));
                    }else {
                        pathShowPadBus.setVisibility(View.GONE);
                        ToastUtils.toastMsg("暂未获取到自驾信息");
                        setupMarket();
                    }
                }

                @Override public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
                    initMap();
                    v_divider_line.setVisibility(View.GONE);
                    pathShowPadBus.setVisibility(View.GONE);
//                pathShowPadDrive.removeAllViews();
                    final WalkPath walkPath = walkRouteResult.getPaths().get(0);
                    WalkRouteOverlay walkRouteOverlay =
                            new WalkRouteOverlay(NearbyNavigationActivity.this, aMap, walkPath,
                                    walkRouteResult.getStartPos(), walkRouteResult.getTargetPos());
                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();
                    walkRouteOverlay.getEndMarker().setObject(infoWindowData);
                    walkRouteOverlay.getEndMarker().showInfoWindow();
                    aMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(AMapUtil.convertToLatLng(endPoint),
                                    16));
                }

                @Override public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
                    if (rideRouteResult == null || rideRouteResult.getPaths() == null || rideRouteResult.getPaths().size() == 0){
                        return;
                    }

                    initMap();
                    v_divider_line.setVisibility(View.GONE);
                    pathShowPadBus.setVisibility(View.GONE);
//                pathShowPadDrive.removeAllViews();
                    RidePath ridePath = rideRouteResult.getPaths().get(0);
                    RideRouteOverlay rideRouteOverlay =
                            new RideRouteOverlay(NearbyNavigationActivity.this, aMap, ridePath,
                                    rideRouteResult.getStartPos(), rideRouteResult.getTargetPos());
                    rideRouteOverlay.removeFromMap();
                    rideRouteOverlay.addToMap();
                    rideRouteOverlay.zoomToSpan();

                    rideRouteOverlay.getEndMarker().setObject(infoWindowData);
                    rideRouteOverlay.getEndMarker().showInfoWindow();
                    aMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(AMapUtil.convertToLatLng(endPoint),
                                    16));
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void selectBusPath(BusRouteResult busRouteResult, BusPath info) {

        initMap();// 清理地图上的所有覆盖物
        BusRouteOverlay busRouteOverlay =
                new BusRouteOverlay(NearbyNavigationActivity.this, aMap, info, busRouteResult.getStartPos(),
                        busRouteResult.getTargetPos());
        busRouteOverlay.removeFromMap();
        busRouteOverlay.addToMap();
        busRouteOverlay.zoomToSpan();
        busRouteOverlay.getEndMarker().setObject(infoWindowData);
        busRouteOverlay.getEndMarker().showInfoWindow();
        LatLng startPos = new LatLng(busRouteResult.getStartPos().getLatitude(),
                busRouteResult.getStartPos().getLongitude());
        LatLng endPos = new LatLng(busRouteResult.getTargetPos().getLatitude(),
                busRouteResult.getTargetPos().getLongitude());
        controlZoom(startPos, endPos, info.getDistance());
    }

    private void selectDrivePath(DriveRouteResult driveRouteResult, DrivePath info) {
        initMap();
        DrivingRouteOverlay drivingRouteOverlay =
                new DrivingRouteOverlay(NearbyNavigationActivity.this, aMap, info,
                        driveRouteResult.getStartPos(), driveRouteResult.getTargetPos(), null);
        drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
        drivingRouteOverlay.setIsColorfulline(false);//是否用颜色展示交通拥堵情况，默认true
        drivingRouteOverlay.removeFromMap();
        drivingRouteOverlay.addToMap();
        drivingRouteOverlay.zoomToSpan();
        drivingRouteOverlay.getEndMarker().setObject(infoWindowData);
        drivingRouteOverlay.getEndMarker().showInfoWindow();
        LatLng startPos = new LatLng(driveRouteResult.getStartPos().getLatitude(),
                driveRouteResult.getStartPos().getLongitude());
        LatLng endPos = new LatLng(driveRouteResult.getTargetPos().getLatitude(),
                driveRouteResult.getTargetPos().getLongitude());
        controlZoom(startPos, endPos, info.getDistance());
    }

    /**
     * 根据距离来控制地图缩放大小
     */
    private void controlZoom(LatLng startPos, LatLng endPos, float pathDistance) {
        int distance = RouteOverlay.calculateDistance(startPos, endPos);
        //中心点
        LatLng halfPos = RouteOverlay.getPointForDis(startPos, endPos, distance / 2.0);
        int zoom;
        if (pathDistance < 1000) {
            zoom = 16;
        } else if (pathDistance < 10000) {
            zoom = 14;
        } else if (pathDistance < 20000) {
            zoom = 12;
        } else {
            zoom = 11;
        }
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(halfPos, zoom));
    }

    public class BusPathViewHolder extends Holder<BusPath> {

        TextView tvPathStart;
        TextView tvPathEnd;
        TextView tvPathTime;
        TextView tvPathTransfer;
        TextView tvPathFast;
        TextView tvPathDistance;
        TextView tvPathPrice;

        public BusPathViewHolder(View itemView) {
            super(itemView);
        }

        @Override protected void initView(View itemView) {
            tvPathStart = itemView.findViewById(R.id.temp_tv_path_start);
            tvPathEnd = itemView.findViewById(R.id.temp_tv_path_end);
            tvPathTime = itemView.findViewById(R.id.temp_tv_path_time);
            tvPathFast = itemView.findViewById(R.id.temp_tv_path_desc);
            tvPathTransfer = itemView.findViewById(R.id.temp_tv_path_transfer);
            tvPathDistance = itemView.findViewById(R.id.temp_tv_path_distance);
            tvPathPrice = itemView.findViewById(R.id.temp_tv_path_price);
        }

        @Override public void updateUI(BusPath data) {
            String street = "";
            if (null != currentGpsInfo) {
                street = currentGpsInfo.street;
            }
            if (data != null){
                StringBuilder startStr = new StringBuilder();
                startStr.append(getText(R.string.navigation_path_start)).append(": ").append(street);
                StringBuilder endStr = new StringBuilder();
                endStr.append(getText(R.string.navigation_path_end)).append(": ").append(toSite);
                tvPathStart.setText(startStr);
                tvPathEnd.setText(endStr);
                tvPathTime.setText(AMapUtil.getFriendlyTime((int) data.getDuration()));
                tvPathTransfer.setText(AMapUtil.getBusPathTitle(data));
                tvPathTransfer.setVisibility(View.VISIBLE);
                tvPathFast.setVisibility(View.GONE);
                tvPathDistance.setText(AMapUtil.getFriendlyLength((int) data.getDistance()));
                String cost = getString(R.string.nearby_car_cost);
                cost = String.format(cost, data.getCost());
                tvPathPrice.setText(cost);
            }
        }
    }

    public class DrivePathViewHolder extends Holder<DrivePath> {

        TextView tvPathStart;
        TextView tvPathEnd;
        TextView tvPathTime;
        TextView tvPathTransfer;
        TextView tvPathFast;
        TextView tvPathDistance;
        TextView tvPathPrice;
        View viewDivider;

        public DrivePathViewHolder(View itemView) {
            super(itemView);
        }

        @Override protected void initView(View itemView) {
            tvPathStart = itemView.findViewById(R.id.temp_tv_path_start);
            tvPathEnd = itemView.findViewById(R.id.temp_tv_path_end);
            tvPathTime = itemView.findViewById(R.id.temp_tv_path_time);
            tvPathFast = itemView.findViewById(R.id.temp_tv_path_desc);
            tvPathTransfer = itemView.findViewById(R.id.temp_tv_path_transfer);
            tvPathDistance = itemView.findViewById(R.id.temp_tv_path_distance);
            tvPathPrice = itemView.findViewById(R.id.temp_tv_path_price);
            viewDivider = itemView.findViewById(R.id.divider);
        }

        @Override public void updateUI(DrivePath data) {
            String street = "";
            if (null != currentGpsInfo) {
                street = currentGpsInfo.street;
            }
            if(data != null){
                StringBuilder startStr = new StringBuilder();
                startStr.append(getText(R.string.navigation_path_start)).append(": ").append(street);
                StringBuilder endStr = new StringBuilder();
                endStr.append(getText(R.string.navigation_path_end)).append(": ").append(toSite);
                tvPathStart.setText(startStr);
                tvPathEnd.setText(endStr);
                tvPathTime.setText(AMapUtil.getFriendlyTime((int) data.getDuration()));
                tvPathTransfer.setVisibility(View.GONE);
                tvPathFast.setVisibility(View.VISIBLE);
                tvPathDistance.setText(AMapUtil.getFriendlyLength((int) data.getDistance()));
                if(data.getTolls()==0){
                    viewDivider.setVisibility(View.GONE);
                    tvPathPrice.setVisibility(View.GONE);
                }else {
                    String driveCost = getString(R.string.nearby_car_cost);
                    driveCost = String.format(driveCost, data.getTolls());

                    tvPathPrice.setText(driveCost);
                    viewDivider.setVisibility(View.VISIBLE);
                    tvPathPrice.setVisibility(View.VISIBLE);
                }

            }
        }
    }

    public static class InfoWindowData {
        public InfoWindowData(String title) {
            this.title = title;
        }

        public String title;
    }

    @Override protected void onResume() {
        super.onResume();
        StatusBarUtils.setStatusBarColor(this, true);
        mapView.onResume();
    }

    @Override protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override protected void onStop() {
        super.onStop();
    }

    @Override protected void onDestroy() {
        uiHandler.removeCallbacksAndMessages(null);
        routeSearch = null;
        if (mapView != null){
            mapView.onDestroy();
        }

        super.onDestroy();
    }

    @Override public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}