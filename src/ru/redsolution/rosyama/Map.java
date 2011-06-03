package ru.redsolution.rosyama;

import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class Map extends MapActivity {
	public static final String LAT = "LAT";
	public static final String LON = "LON";

	double lat;
	double lon;
	private MapView mapView;

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		mapView = (MapView) findViewById(0);
		if (icicle != null) {
			lat = icicle.getDouble(LAT, 0);
			lat = icicle.getDouble(LON, 0);

		} else {
			lat = getIntent().getDoubleExtra(LAT, 0);
			lon = getIntent().getDoubleExtra(LON, 0);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putDouble(LAT, lat);
		outState.putDouble(LON, lon);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (lat != 0 && lon != 0) {
			mapView.getController().animateTo(
					new GeoPoint((int) lat * 1000000, (int) lon * 1000000));
			lat = 0;
			lon = 0;
		}
	}

}
