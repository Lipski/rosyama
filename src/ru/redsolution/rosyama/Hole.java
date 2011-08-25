package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.Rosyama;
import ru.redsolution.rosyama.data.Rosyama.LocalizedException;
import ru.redsolution.rosyama.data.Rosyama.State;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class Hole extends Activity implements OnClickListener, StateListener {
	private static final String SAVED_ADDRESS = "SAVED_ADDRESS";
	private static final String SAVED_COMMENT = "SAVED_COMMENT";

	Rosyama rosyama;

	EditText address;
	EditText comment;

	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.hole);
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

		progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		((Rosyama) getApplication()).setStateListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		((Rosyama) getApplication()).setStateListener(null);
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
	}

	private class AddTask extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			try {
				((Rosyama) getApplication()).hole(params[0], params[1]);
				((Rosyama) getApplication()).head();
			} catch (LocalizedException e) {
				return e.getString(Hole.this);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null)
				Toast.makeText(Hole.this, result, Toast.LENGTH_LONG).show();
			if (((Rosyama) getApplication()).getState().canPDF())
				finish();
		}
	}
}