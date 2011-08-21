package ru.redsolution.rosyama;

import java.io.File;
import java.util.Date;

import ru.redsolution.rosyama.Rosyama.LocalizedException;
import ru.redsolution.rosyama.Rosyama.State;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class Main extends Activity implements OnClickListener,
		DialogClickListener, StateListener {
	private static final String SAVED_REQUESTED_URI = "SAVED_REQUESTED_URI";

	private static final int OPTION_MENU_PREFERENCE_ID = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;

	private static final int DIALOG_PHOTO_ID = 0;
	private static final int DIALOG_SEND_ID = 1;
	private static final int DIALOG_GET_ID = 2;

	private Uri requestedUri;

	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		for (int id : new int[] { R.id.photo_image, R.id.hole_image,
				R.id.pdf_image })
			findViewById(id).setOnClickListener(this);
		requestedUri = null;
		if (savedInstanceState != null) {
			String stringUri = savedInstanceState
					.getString(SAVED_REQUESTED_URI);
			if (stringUri != null)
				requestedUri = Uri.parse(stringUri);
		}
		// new PhotoTask().execute("/sdcard/2011-06-02-21-20-24.jpg");

		progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!((Rosyama) getApplication()).getState().isAuthorized()) {
			Intent intent = new Intent(this, Auth.class);
			startActivity(intent);
			finish();
			return;
		}
		((Rosyama) getApplication()).setStateListener(this);
		enables();
	}

	@Override
	protected void onPause() {
		super.onPause();
		((Rosyama) getApplication()).setStateListener(null);
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
				new PhotoTask().execute(requestedUri.getPath());
			enables();
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, OPTION_MENU_PREFERENCE_ID, 0, getString(R.string.login))
				.setIcon(android.R.drawable.ic_menu_preferences);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case OPTION_MENU_PREFERENCE_ID:
			Intent intent = new Intent(this, Auth.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.photo_image:
			showDialog(DIALOG_PHOTO_ID);
			break;
		case R.id.hole_image:
			showDialog(DIALOG_SEND_ID);
			break;
		case R.id.pdf_image:
			showDialog(DIALOG_GET_ID);
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		switch (id) {
		case DIALOG_PHOTO_ID:
			return new DialogBuilder(this, this, DIALOG_PHOTO_ID,
					getString(R.string.photo_help)).create();
		case DIALOG_SEND_ID:
			return new DialogBuilder(this, this, DIALOG_SEND_ID,
					getString(R.string.hole_help)).create();
		case DIALOG_GET_ID:
			return new DialogBuilder(this, this, DIALOG_GET_ID,
					getString(R.string.pdf_help)).create();
		default:
			return null;
		}
	}

	private Uri getNextUri(String extention) {
		return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
				Rosyama.FILE_NAME_FORMAT.format(new Date()) + extention));
	}

	private void enables() {
		State state = ((Rosyama) getApplication()).getState();
		findViewById(R.id.hole_image).setEnabled(state.canHole());
		findViewById(R.id.hole_label).setEnabled(state.canHole());
		findViewById(R.id.pdf_image).setEnabled(state.canPDF());
		findViewById(R.id.pdf_label).setEnabled(state.canPDF());
	}

	@Override
	public void onAccept(DialogBuilder dialog) {
		Intent intent;
		switch (dialog.getDialogId()) {
		case DIALOG_PHOTO_ID:
			requestedUri = getNextUri(".jpg");
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, requestedUri);
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			break;
		case DIALOG_SEND_ID:
			intent = new Intent(this, Hole.class);
			startActivity(intent);
			break;
		case DIALOG_GET_ID:
			intent = new Intent(this, PDF.class);
			startActivity(intent);
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
	public void onStateChange() {
		State state = ((Rosyama) getApplication()).getState();
		Integer action = state.getAction();
		if (action == null) {
			try {
				progressDialog.dismiss();
			} catch (IllegalArgumentException e) {
			}
		} else {
			progressDialog.setMessage(getString(action));
			progressDialog.show();
		}
		enables();
	}

	private class PhotoTask extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			try {
				((Rosyama) getApplication()).photo(params[0]);
				((Rosyama) getApplication()).geo();
			} catch (LocalizedException e) {
				return getString(e.getResourceID());
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null)
				Toast.makeText(Main.this, result, Toast.LENGTH_LONG).show();
		}
	}
}