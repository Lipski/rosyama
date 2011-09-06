package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.Rosyama;
import ru.redsolution.rosyama.data.UpdateListener;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class Main extends Activity implements OnClickListener,
		DialogClickListener, UpdateListener {
	/**
	 * Запрошенный URL.
	 */
	private static final String SAVED_REQUESTED_URI = "SAVED_REQUESTED_URI";

	/**
	 * Код запроса изображения.
	 */
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;

	/**
	 * Диалог с сообщением перед созданием дефекта.
	 */
	private static final int DIALOG_CREATE_ID = 0;

	/**
	 * Выход из текущей сессии.
	 */
	private static final int OPTION_MENU_LOGOUT_ID = 0;

	/**
	 * Выход из текущей сессии.
	 */
	private static final int OPTION_MENU_ABOUT_ID = 1;

	/**
	 * Приложение.
	 */
	private Rosyama rosyama;

	/**
	 * URL изображения.
	 */
	private Uri requestedUri;

	/**
	 * Менеджер местоположения.
	 */
	private LocationManager locationManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		rosyama = (Rosyama) getApplication();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		findViewById(R.id.create).setOnClickListener(this);
		findViewById(R.id.list).setOnClickListener(this);

		requestedUri = null;
		if (savedInstanceState != null) {
			String stringUri = savedInstanceState
					.getString(SAVED_REQUESTED_URI);
			if (stringUri != null)
				requestedUri = Uri.parse(stringUri);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		rosyama.setUpdateListener(this);
		InterfaceUtilities.setTiledBackground(this, R.id.background,
				R.drawable.background);
		InterfaceUtilities.setTiledBackground(this, R.id.dot, R.drawable.dot);
		InterfaceUtilities.setTiledBackground(this, R.id.dot2, R.drawable.dot);
	}

	@Override
	protected void onPause() {
		super.onPause();
		rosyama.setUpdateListener(null);
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
			locationManager.removeUpdates(rosyama);
			if (resultCode != RESULT_OK)
				requestedUri = null;
			else {
				rosyama.createHole(requestedUri);
				Intent intent = new Intent(this, HoleEdit.class);
				startActivity(intent);
			}
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, OPTION_MENU_LOGOUT_ID, 0, getString(R.string.logout))
				.setIcon(R.drawable.ic_menu_login);
		menu.add(0, OPTION_MENU_ABOUT_ID, 0, getString(R.string.about_title))
				.setIcon(android.R.drawable.ic_menu_info_details);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case OPTION_MENU_LOGOUT_ID:
			rosyama.logout();
			return true;
		case OPTION_MENU_ABOUT_ID:
			Intent intent = new Intent(this, About.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.create:
			showDialog(DIALOG_CREATE_ID);
			// rosyama.createHole(Uri.fromFile(new File("/sdcard/test.jpg")));
			// intent = new Intent(this, HoleEdit.class);
			// startActivity(intent);
			break;
		case R.id.list:
			intent = new Intent(this, HoleList.class);
			startActivity(intent);
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		switch (id) {
		case DIALOG_CREATE_ID:
			return new DialogBuilder(this, this, DIALOG_CREATE_ID,
					getString(R.string.hole_create_dialog)).create();
		default:
			return null;
		}
	}

	@Override
	public void onAccept(DialogBuilder dialog) {
		Intent intent;
		switch (dialog.getDialogId()) {
		case DIALOG_CREATE_ID:
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, rosyama);
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, rosyama);
			requestedUri = Rosyama.getNextJpegUri();
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, requestedUri);
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			break;
		}
	}

	@Override
	public void onDecline(DialogBuilder dialog) {
	}

	@Override
	public void onCancel(DialogBuilder dialog) {
	}

	@Override
	public void onUpdate() {
		if (!rosyama.getAuthorizeOperation().isComplited()) {
			Intent intent = new Intent(this, Auth.class);
			startActivity(intent);
			finish();
			return;
		}
	}
}