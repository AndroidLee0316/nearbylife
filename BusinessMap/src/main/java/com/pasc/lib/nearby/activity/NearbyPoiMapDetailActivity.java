package com.pasc.lib.nearby.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
//import com.amap.api.maps.MapView;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.pasc.lib.base.activity.BaseActivity;
import com.pasc.lib.base.util.StatusBarUtils;
import com.pasc.lib.nearby.R;
import com.pasc.lib.widget.toolbar.PascToolbar;
import java.text.DecimalFormat;


public class NearbyPoiMapDetailActivity extends BaseActivity {
    private static final String POI_ITEM_KEY = "poi_item_key";
    private static final String POI_INDEX_KEY = "poi_index_key";
    private static final String CURRENT_LOCATION_KEY = "current_location_key";
    private static final String TITLE_KEY = "title_key";

    PascToolbar titleView;
    //MapView mapView;
    TextureMapView mapView;
    TextView itemAddressTextView;
    TextView goNavigationButton;
    TextView itemTelTextView;
    TextView itemTitleTextView;

    private AMap aMap;

    private ImageButton mCloseImageButton;
    private ImageButton mRightImageButton;
    private TextView mTitleView;

    public static void start(Activity activity, LatLonPoint current, PoiItem poiItem, int index, String title) {
        Intent intent = new Intent(activity, NearbyPoiMapDetailActivity.class);
        intent.putExtra(POI_ITEM_KEY, poiItem);
        intent.putExtra(POI_INDEX_KEY, index);
        intent.putExtra(CURRENT_LOCATION_KEY, current);
        intent.putExtra(TITLE_KEY, title);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.nearby_activity_poi_detail);

        titleView = findViewById(R.id.nearby_title_bar);
        //mapView = (MapView) findViewById(R.id.nearby_map_view);
        mapView = (TextureMapView) findViewById(R.id.nearby_map_view);
        itemAddressTextView = (TextView) findViewById(R.id.nearby_tv_item_address);
        goNavigationButton = (TextView) findViewById(R.id.nearby_go_navigation_button);
        itemTelTextView = (TextView) findViewById(R.id.nearby_tv_item_tel);
        itemTitleTextView = (TextView) findViewById(R.id.nearby_tv_item_title);

        // title = getIntent().getStringExtra(TITLE_KEY);
        titleView.setTitle("");

        mCloseImageButton = titleView.addCloseImageButton();
        titleView.enableUnderDivider(false);
        mCloseImageButton.setImageResource(R.drawable.nearby_ic_back_circle);
        mCloseImageButton.setBackgroundColor(Color.TRANSPARENT);
        mTitleView = this.titleView.getTitleView();

        mCloseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setLogoBottomMargin(-100);
        showLocationDetail();
        goNavigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PoiItem poiItem = (PoiItem) goNavigationButton.getTag();
                NearbyNavigationActivity.start(NearbyPoiMapDetailActivity.this,
                        (LatLonPoint) getIntent().getParcelableExtra(CURRENT_LOCATION_KEY),
                        poiItem.getLatLonPoint(),
                        poiItem.getCityName(),
                        poiItem.getTitle()
                );
            }
        });
    }

    @Override
    protected int layoutResId() {
        return R.layout.nearby_activity_poi_detail;
    }

    @Override
    protected void onInit(@Nullable Bundle bundle) {
        StatusBarUtils.setStatusBarColor(this, true);
    }

    private void showLocationDetail() {
        final PoiItem poiItem = getIntent().getParcelableExtra(POI_ITEM_KEY);
        DecimalFormat df = new DecimalFormat("0.0");
        String distance =
                poiItem.getDistance() > 1000 ? df.format((float) poiItem.getDistance() / 1000) + "km"
                        : poiItem.getDistance() + "m";
        itemAddressTextView.setText(
                distance + " | " + poiItem.getSnippet() + "");
        itemTelTextView.setText(!TextUtils.isEmpty(poiItem.getTel()) ? poiItem.getTel() : "暂无");
        itemTitleTextView.setText(poiItem.getTitle());
        goNavigationButton.setTag(poiItem);
        LatLng latLng = new LatLng(poiItem.getLatLonPoint().getLatitude(),
                poiItem.getLatLonPoint().getLongitude());
        addUnSelectedPoiMarkerByLatlng(latLng);

        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
    }

    private Marker addUnSelectedPoiMarkerByLatlng(LatLng latLng) {
        MarkerOptions targetMO = new MarkerOptions();
        targetMO.anchor(0.5f, 1);
        targetMO.position(latLng);
        targetMO.draggable(true);
        targetMO.icon(BitmapDescriptorFactory.fromResource(R.drawable.nearby_ic_map_mark_default));
        Marker marker = aMap.addMarker(targetMO);
        return marker;
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatusBarUtils.setStatusBarColor(this, true);
        mapView.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
