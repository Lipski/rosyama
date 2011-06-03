package ru.redsolution.rosyama;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class Main extends Activity implements OnClickListener {
	private static final String SAVED_REQUESTED_URI = "SAVED_REQUESTED_URI";

	private static final int OPTION_MENU_PREFERENCE_ID = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;

	private Uri requestedUri;
	private String login;
	private String password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findViewById(R.id.make_photo).setOnClickListener(this);
		findViewById(R.id.send).setOnClickListener(this);

		requestedUri = Uri
				.fromFile(new File("/sdcard/2011-06-02-21-20-24.jpg"));
		if (savedInstanceState != null) {
			String stringUri = savedInstanceState
					.getString(SAVED_REQUESTED_URI);
			if (stringUri != null)
				requestedUri = Uri.parse(stringUri);
		}
		findViewById(R.id.send).setEnabled(requestedUri != null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		login = settings.getString(getString(R.string.login_key), "");
		password = settings.getString(getString(R.string.password_key), "");
		if ("".equals(login) && "".equals(password)) {
			Intent intent = new Intent(this, Login.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_REQUESTED_URI, requestedUri == null ? null
				: requestedUri.toString());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
			findViewById(R.id.send).setEnabled(resultCode == RESULT_OK);
			if (resultCode != RESULT_OK)
				requestedUri = null;
			else {
				Location gps = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
						.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
				Location net = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
						.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
				Location best = net;
				if (gps != null && net != null) {
					if (gps.getTime() > net.getTime())
						best = gps;
					else if (net.hasAccuracy()) {
						float[] results = new float[1];
						Location.distanceBetween(net.getLatitude(),
								net.getLongitude(), gps.getLatitude(),
								gps.getLongitude(), results);
						if (results[0] < net.getAccuracy())
							best = gps;
					}

				} else if (net == null)
					best = gps;
				if (best != null)
					((EditText) findViewById(R.id.coordinates))
							.setText(Location.convert(best.getLongitude(),
									Location.FORMAT_DEGREES).replace(',', '.')
									+ ","
									+ Location.convert(best.getLatitude(),
											Location.FORMAT_DEGREES).replace(
											',', '.'));
			}
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, OPTION_MENU_PREFERENCE_ID, 0, "Настройки").setIcon(
				android.R.drawable.ic_menu_preferences);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case OPTION_MENU_PREFERENCE_ID:
			Intent intent = new Intent(this, Preference.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.make_photo:
			requestedUri = getNextUri(".jpg");
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, requestedUri);
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			break;
		case R.id.send:
			if (!((Rosyama) getApplication()).login(login, password)) {
				Toast.makeText(this, getString(R.string.auth_fail),
						Toast.LENGTH_LONG).show();
				break;
			}
			if (!((Rosyama) getApplication()).add(requestedUri.getPath(),
					((EditText) findViewById(R.id.address)).getText()
							.toString(),
					((EditText) findViewById(R.id.comment)).getText()
							.toString(),
					((EditText) findViewById(R.id.coordinates)).getText()
							.toString())) {
				Toast.makeText(this, getString(R.string.add_fail),
						Toast.LENGTH_LONG).show();
				break;
			}
			break;
		}
	}

	private Uri getNextUri(String extention) {
		return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
				Rosyama.FILE_NAME_FORMAT.format(new Date()) + extention));
	}
}