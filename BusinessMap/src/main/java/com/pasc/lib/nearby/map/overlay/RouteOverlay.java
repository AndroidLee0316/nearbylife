package com.pasc.lib.nearby.map.overlay;

import android.content.Context;
import android.graphics.Color;

import android.support.annotation.ColorInt;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.pasc.lib.nearby.R;

import java.util.ArrayList;
import java.util.List;

public class RouteOverlay {
	protected List<Marker> stationMarkers = new ArrayList<>();
	protected List<Polyline> allPolyLines = new ArrayList<>();
	protected Marker startMarker;
	protected Marker endMarker;
	protected LatLng startPoint;
	protected LatLng endPoint;
	protected AMap mAMap;
	private Context mContext;
	private BitmapDescriptor startBitD, endBitD, busBitD, walkBitD, driveBitD;
	protected boolean nodeIconVisible = true;

	public RouteOverlay(Context context) {
		mContext = context;
	}

	/**
	 * 去掉BusRouteOverlay上所有的Marker。
	 * @since V2.1.0
	 */
	public void removeFromMap() {
		if (startMarker != null) {
			startMarker.remove();

		}
		if (endMarker != null) {
			endMarker.remove();
		}
		for (Marker marker : stationMarkers) {
			marker.remove();
		}
		for (Polyline line : allPolyLines) {
			line.remove();
		}
		destroyBit();
	}

	public Marker getStartMarker(){
		return startMarker;
	}

	public Marker getEndMarker(){
		return endMarker;
	}

	private void destroyBit() {
		if (startBitD != null) {
			startBitD.recycle();
			startBitD = null;
		}
		if (endBitD != null) {
			endBitD.recycle();
			endBitD = null;
		}
		if (busBitD != null) {
			busBitD.recycle();
			busBitD = null;
		}
		if (walkBitD != null) {
			walkBitD.recycle();
			walkBitD = null;
		}
		if (driveBitD != null) {
			driveBitD.recycle();
			driveBitD = null;
		}
	}
	/**
	 * 给起点Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
	 * @return 更换的Marker图片。
	 * @since V2.1.0
	 */
	protected BitmapDescriptor getStartBitmapDescriptor() {
		if (startBitD == null || startBitD.getBitmap() == null) {
			startBitD = BitmapDescriptorFactory.fromResource(R.drawable.nearby_ic_nav_path_start);
		}
		return startBitD;
	}
	/**
	 * 给终点Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
	 * @return 更换的Marker图片。
	 * @since V2.1.0
	 */
	protected BitmapDescriptor getEndBitmapDescriptor() {
		if (endBitD == null || endBitD.getBitmap() == null) {
			endBitD = BitmapDescriptorFactory.fromResource(R.drawable.nearby_ic_nav_path_end);
		}
		return endBitD;
	}
	/**
	 * 给公交Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
	 * @return 更换的Marker图片。
	 * @since V2.1.0
	 */
	protected BitmapDescriptor getBusBitmapDescriptor() {
		if (busBitD == null || busBitD.getBitmap() == null) {
			busBitD = BitmapDescriptorFactory.fromResource(R.drawable.nearby_amap_bus_icon);
		}
		return busBitD;
	}
	/**
	 * 给步行Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
	 * @return 更换的Marker图片。
	 * @since V2.1.0
	 */
	protected BitmapDescriptor getWalkBitmapDescriptor() {
		if (walkBitD == null || walkBitD.getBitmap() == null) {
			walkBitD = BitmapDescriptorFactory.fromResource(R.drawable.nearby_amap_man_icon);
		}
		return walkBitD;
	}

	protected BitmapDescriptor getDriveBitmapDescriptor() {
		if (driveBitD == null || driveBitD.getBitmap() == null) {
			driveBitD = BitmapDescriptorFactory.fromResource(R.drawable.nearby_amap_car_icon);
		}
		return driveBitD;
	}

	protected void addStartAndEndMarker() {
		startMarker = mAMap.addMarker((new MarkerOptions())
				.position(startPoint).icon(getStartBitmapDescriptor())
				.title("\u8D77\u70B9"));
		//startMarker.showInfoWindow();

		endMarker = mAMap.addMarker((new MarkerOptions()).position(endPoint)
				.icon(getEndBitmapDescriptor()).title("\u7EC8\u70B9"));
		// mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint,
		// getShowRouteZoom()));
	}
	/**
	 * 移动镜头到当前的视角。
	 * @since V2.1.0
	 */
	public void zoomToSpan() {
		if (startPoint != null) {
			if (mAMap == null)
				return;
			try {
				LatLngBounds bounds = getLatLngBounds();
				mAMap.animateCamera(CameraUpdateFactory
						.newLatLngBounds(bounds, 50));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	protected LatLngBounds getLatLngBounds() {
		LatLngBounds.Builder b = LatLngBounds.builder();
		b.include(new LatLng(startPoint.latitude, startPoint.longitude));
		b.include(new LatLng(endPoint.latitude, endPoint.longitude));
		return b.build();
	}
	/**
	 * 路段节点图标控制显示接口。
	 * @param visible true为显示节点图标，false为不显示。
	 * @since V2.3.1
	 */
	public void setNodeIconVisibility(boolean visible) {
		try {
			nodeIconVisible = visible;
			if (this.stationMarkers != null && this.stationMarkers.size() > 0) {
				for (int i = 0; i < this.stationMarkers.size(); i++) {
					this.stationMarkers.get(i).setVisible(visible);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	protected void addStationMarker(MarkerOptions options) {
		if(options == null) {
			return;
		}
		Marker marker = mAMap.addMarker(options);
		if(marker != null) {
			stationMarkers.add(marker);
		}
		
	}

	protected void addPolyLine(PolylineOptions options) {
		if(options == null) {
			return;
		}
		Polyline polyline = mAMap.addPolyline(options);
		if(polyline != null) {
			allPolyLines.add(polyline);
		}
	}

	/**
	 * 获取两点间距离
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	public static int calculateDistance(LatLng start, LatLng end) {
		double x1 = start.longitude;
		double y1 = start.latitude;
		double x2 = end.longitude;
		double y2 = end.latitude;
		return calculateDistance(x1, y1, x2, y2);
	}

	public static int calculateDistance(double x1, double y1, double x2, double y2) {
		final double NF_pi = 0.01745329251994329; // 弧度 PI/180
		x1 *= NF_pi;
		y1 *= NF_pi;
		x2 *= NF_pi;
		y2 *= NF_pi;
		double sinx1 = Math.sin(x1);
		double siny1 = Math.sin(y1);
		double cosx1 = Math.cos(x1);
		double cosy1 = Math.cos(y1);
		double sinx2 = Math.sin(x2);
		double siny2 = Math.sin(y2);
		double cosx2 = Math.cos(x2);
		double cosy2 = Math.cos(y2);
		double[] v1 = new double[3];
		v1[0] = cosy1 * cosx1 - cosy2 * cosx2;
		v1[1] = cosy1 * sinx1 - cosy2 * sinx2;
		v1[2] = siny1 - siny2;
		double dist = Math.sqrt(v1[0] * v1[0] + v1[1] * v1[1] + v1[2] * v1[2]);

		return (int) (Math.asin(dist / 2) * 12742001.5798544);
	}

	//获取指定两点之间固定距离点
	public static LatLng getPointForDis(LatLng sPt, LatLng ePt, double dis) {
		double lSegLength = calculateDistance(sPt, ePt);
		double preResult = dis / lSegLength;
		return new LatLng((ePt.latitude - sPt.latitude) * preResult + sPt.latitude, (ePt.longitude - sPt.longitude) * preResult + sPt.longitude);
	}


	protected float getRouteWidth() {
		return 18f;
	}

	protected @ColorInt int getWalkColor() {
		return mContext.getResources().getColor(R.color.pasc_primary);
	}

	/**
	 * 自定义路线颜色。
	 * return 自定义路线颜色。
	 * @since V2.2.1
	 */
	protected @ColorInt int getBusColor() {
		return mContext.getResources().getColor(R.color.pasc_primary);
	}

	protected @ColorInt int getDriveColor() {
		return mContext.getResources().getColor(R.color.pasc_primary);
	}

	// protected int getShowRouteZoom() {
	// return 15;
	// }
}
