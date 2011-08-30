package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.Hole;
import ru.redsolution.rosyama.data.Rosyama;
import ru.redsolution.rosyama.data.UpdateListener;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class Map extends MapActivity implements UpdateListener, OnClickListener {
	/**
	 * Приложение.
	 */
	private Rosyama rosyama;

	/**
	 * Дефект.
	 */
	private Hole hole;

	/**
	 * Управление картой.
	 */
	private MapController mapController;

	/**
	 * Поле ввода адреса.
	 */
	private TextView addressView;

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.map);
		rosyama = (Rosyama) getApplication();

		hole = rosyama.getHole(getIntent().getStringExtra(HoleEdit.EXTRA_ID));

		findViewById(R.id.search).setOnClickListener(this);
		MapView mapView = (MapView) findViewById(R.id.map);
		mapController = mapView.getController();
		addressView = (TextView) findViewById(R.id.address);
		findViewById(R.id.done).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		((Rosyama) getApplication()).setUpdateListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		((Rosyama) getApplication()).setUpdateListener(null);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.search:
			rosyama.getLocationOperation().execute(
					new Rosyama.HoleAndAddress(hole,
							((TextView) findViewById(R.id.address)).getText()
									.toString()));
			break;
		case R.id.done:
			hole.setAddress(((TextView) findViewById(R.id.address)).getText()
					.toString());
			finish();
			break;
		}
	}

	@Override
	public void onUpdate() {
		addressView.setText(hole.getAddress());
		Location location = hole.getLocation();
		if (location != null) {
			GeoPoint geoPoint = new GeoPoint(
					(int) (location.getLatitude() * 1E6),
					(int) (location.getLongitude() * 1E6));
			mapController.animateTo(geoPoint);
		}
	}
}
