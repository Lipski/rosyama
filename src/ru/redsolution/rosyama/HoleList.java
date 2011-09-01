package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.Hole;
import ru.redsolution.rosyama.data.Rosyama;
import ru.redsolution.rosyama.data.Status;
import ru.redsolution.rosyama.data.UpdateListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TabHost;

public class HoleList extends TabActivity implements OnItemClickListener,
		UpdateListener, OnCancelListener, DialogClickListener {
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
	 * Добавить дефект.
	 */
	private static final int OPTION_MENU_ADD_ID = 0;

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

	/**
	 * Диалог выполнения задания.
	 */
	private ProgressDialog progressDialog;

	private ListView freshListView;
	private ListView inprogressListView;
	private ListView fixedListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hole_list);
		rosyama = (Rosyama) getApplication();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		requestedUri = null;
		if (savedInstanceState != null) {
			String stringUri = savedInstanceState
					.getString(SAVED_REQUESTED_URI);
			if (stringUri != null)
				requestedUri = Uri.parse(stringUri);
		}

		addTabSpec("fresh", R.string.status_fresh,
				android.R.drawable.ic_menu_directions, R.id.fresh);
		freshListView = (ListView) findViewById(R.id.fresh);
		freshListView.setAdapter(new HoleAdapter(this, Status.fresh));
		freshListView.setOnItemClickListener(this);

		addTabSpec("inprogress", R.string.status_inprogress,
				android.R.drawable.ic_menu_today, R.id.inprogress);
		inprogressListView = (ListView) findViewById(R.id.inprogress);
		inprogressListView.setAdapter(new HoleAdapter(this, Status.inprogress));
		inprogressListView.setOnItemClickListener(this);

		addTabSpec("fixed", R.string.status_fixed,
				android.R.drawable.ic_menu_agenda, R.id.fixed);
		fixedListView = (ListView) findViewById(R.id.fixed);
		fixedListView.setAdapter(new HoleAdapter(this, Status.fixed));
		fixedListView.setOnItemClickListener(this);

		if (savedInstanceState == null) {
			rosyama.getListOperation().execute();
		}

		progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(true);
		progressDialog.setOnCancelListener(this);
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

	/**
	 * Добавляет новый таб.
	 * 
	 * @param tag
	 *            Тег.
	 * @param name
	 *            Имя.
	 * @param content
	 *            Идентификатор закладки.
	 */
	private void addTabSpec(String tag, int name, int drawable, int content) {
		TabHost tabHost = getTabHost();
		TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
		tabSpec.setIndicator(getString(name));
		// TODO: , getResources().getDrawable(drawable));
		tabSpec.setContent(content);
		tabHost.addTab(tabSpec);
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
		menu.add(0, OPTION_MENU_ADD_ID, 0,
				getString(R.string.hole_create_label)).setIcon(
				android.R.drawable.ic_menu_add);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case OPTION_MENU_ADD_ID:
			showDialog(DIALOG_CREATE_ID);
			return true;
		}
		return false;
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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Hole hole = (Hole) parent.getAdapter().getItem(position);
		if (hole == null)
			// Footer
			return;
		Intent intent = new Intent(this, HoleDetail.class);
		intent.putExtra(HoleEdit.EXTRA_ID, hole.getId());
		startActivity(intent);
	}

	@Override
	public void onUpdate() {
		((HoleAdapter) freshListView.getAdapter()).notifyDataSetChanged();
		((HoleAdapter) inprogressListView.getAdapter()).notifyDataSetChanged();
		((HoleAdapter) fixedListView.getAdapter()).notifyDataSetChanged();

		if (rosyama.getListOperation().isInProgress()) {
			progressDialog.setMessage(getString(R.string.list_request));
			progressDialog.show();
		} else
			progressDialog.dismiss();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		finish();
	}
}
