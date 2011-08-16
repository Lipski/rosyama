package ru.redsolution.rosyama;

import ru.redsolution.rosyama.Rosyama.ExceptionWithResource;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class Hole extends Activity implements OnClickListener {
	private static final String SAVED_ADDRESS = "SAVED_ADDRESS";
	private static final String SAVED_COMMENT = "SAVED_COMMENT";

	Rosyama rosyama;

	EditText address;
	EditText comment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hole);
		rosyama = (Rosyama) getApplication();
		findViewById(R.id.send).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
		address = (EditText) findViewById(R.id.address);
		comment = (EditText) findViewById(R.id.comment);
		if (savedInstanceState != null) {
			address.setText(savedInstanceState.getString(SAVED_ADDRESS));
			comment.setText(savedInstanceState.getString(SAVED_COMMENT));
		} else {
			address.setText(rosyama.getAddress());
			comment.setText(rosyama.getComment());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_ADDRESS, address.getText().toString());
		outState.putString(SAVED_COMMENT, comment.getText().toString());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.send:
			new AddTask().execute(address.getText().toString(), comment
					.getText().toString());
			break;
		case R.id.cancel:
			finish();
			break;
		}
	}

	private class AddTask extends AsyncTask<String, String, String> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(Hole.this);
			progressDialog.setMessage(getString(R.string.hole_request));
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				((Rosyama) getApplication()).hole(params[0], params[1]);
				((Rosyama) getApplication()).head();
			} catch (ExceptionWithResource e) {
				return getString(e.getResourceID());
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				progressDialog.dismiss();
			} catch (IllegalArgumentException e) {
				return;
			}
			if (result != null)
				Toast.makeText(Hole.this, result, Toast.LENGTH_LONG).show();
			if (((Rosyama) getApplication()).getState().canPDF())
				finish();
		}
	}
}