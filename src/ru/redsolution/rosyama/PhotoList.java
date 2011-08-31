package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.AbstractPhoto;
import ru.redsolution.rosyama.data.Hole;
import ru.redsolution.rosyama.data.Rosyama;
import ru.redsolution.rosyama.data.UpdateListener;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PhotoList extends ListActivity implements UpdateListener,
		OnItemClickListener, OnClickListener {
	/**
	 * Передается если необходимо отключить возможность редактирования
	 * загруженных дефектов.
	 */
	public static final String EXRTA_READ_ONLY = "ru.redsolution.rosyama.EXTRA_READ_ONLY";

	/**
	 * Запрошенный URL.
	 */
	private static final String SAVED_REQUESTED_URI = "SAVED_REQUESTED_URI";

	/**
	 * Код запроса изображения.
	 */
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;

	/**
	 * Контекстное меню для удаления фотографии.
	 */
	private static final int CONTEXT_MENU_DELETE_ID = 0;

	/**
	 * Приложение.
	 */
	Rosyama rosyama;

	/**
	 * Дефект.
	 */
	Hole hole;

	/**
	 * Адаптер для фотографий.
	 */
	PhotoAdapter photoAdapter;

	/**
	 * URL изображения.
	 */
	private Uri requestedUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_list);
		rosyama = (Rosyama) getApplication();
		hole = rosyama.getHole(getIntent().getStringExtra(HoleEdit.EXTRA_ID));
		if (hole == null) {
			finish();
			return;
		}
		boolean readOnly = getIntent().getBooleanExtra(EXRTA_READ_ONLY, false);

		ListView listView = getListView();
		if (!readOnly) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			View footerView = layoutInflater.inflate(R.layout.photo_add,
					listView, false);
			footerView.findViewById(R.id.add).setOnClickListener(this);
			listView.addFooterView(footerView, null, false);
			registerForContextMenu(listView);
		}

		requestedUri = null;
		if (savedInstanceState != null) {
			String stringUri = savedInstanceState
					.getString(SAVED_REQUESTED_URI);
			if (stringUri != null)
				requestedUri = Uri.parse(stringUri);
		}

		photoAdapter = new PhotoAdapter(this, hole);
		listView.setAdapter(photoAdapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		rosyama.setUpdateListener(this);
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
			if (resultCode != RESULT_OK)
				requestedUri = null;
			else
				hole.createPhoto(requestedUri);
			break;
		}
	}

	@Override
	public void onUpdate() {
		photoAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		AbstractPhoto photo = (AbstractPhoto) parent.getAdapter().getItem(
				position);
		if (photo == null)
			// Footer
			return;
		Intent intent;
		intent = new Intent(Intent.ACTION_VIEW);
		try {
			// Для интернет ресурсов
			intent.setData(photo.getUri());
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			try {
				// Для локальных файлов
				intent.setDataAndType(photo.getUri(), "image/*");
				startActivity(intent);
			} catch (ActivityNotFoundException e2) {
			}
		}
	}

	@Override
	public void onClick(View view) {
		Intent intent;
		switch (view.getId()) {
		case R.id.add:
			// hole.createPhoto(Uri.fromFile(new File("/sdcard/dsc04193.jpg")));
			requestedUri = Rosyama.getNextJpegUri();
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, requestedUri);
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			break;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		AbstractPhoto photo = (AbstractPhoto) getListView().getItemAtPosition(
				info.position);
		if (photo == null)
			// Footer
			return;
		menu.setHeaderTitle(getString(R.string.photo_hint, info.position + 1));
		menu.add(0, CONTEXT_MENU_DELETE_ID, 0,
				getResources().getText(R.string.photo_delete));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		AbstractPhoto photo = (AbstractPhoto) getListView().getItemAtPosition(
				info.position);
		if (photo == null)
			// Footer
			return false;
		switch (item.getItemId()) {
		case CONTEXT_MENU_DELETE_ID:
			hole.removePhoto(photo);
			return true;
		}
		return false;
	}
}
