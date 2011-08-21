package ru.redsolution.rosyama;

import ru.redsolution.rosyama.Rosyama.LocalizedException;
import ru.redsolution.rosyama.Rosyama.State;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class PDF extends Activity implements OnClickListener, StateListener {
	private static final String SAVED_TO = "SAVED_TO";
	private static final String SAVED_FROM = "SAVED_FROM";
	private static final String SAVED_POSTADDRESS = "SAVED_POSTADDRESS";
	private static final String SAVED_ADDRESS = "SAVED_ADDRESS";
	private static final String SAVED_SIGNATURE = "SAVED_SIGNATURE";

	Rosyama rosyama;

	EditText to;
	EditText from;
	EditText postaddress;
	EditText address;
	EditText signature;

	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pdf);
		rosyama = (Rosyama) getApplication();
		findViewById(R.id.send).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
		to = (EditText) findViewById(R.id.to);
		from = (EditText) findViewById(R.id.from);
		postaddress = (EditText) findViewById(R.id.postaddress);
		address = (EditText) findViewById(R.id.address);
		signature = (EditText) findViewById(R.id.signature);
		if (savedInstanceState != null) {
			to.setText(savedInstanceState.getString(SAVED_TO));
			from.setText(savedInstanceState.getString(SAVED_FROM));
			postaddress
					.setText(savedInstanceState.getString(SAVED_POSTADDRESS));
			address.setText(savedInstanceState.getString(SAVED_ADDRESS));
			signature.setText(savedInstanceState.getString(SAVED_SIGNATURE));
		} else {
			to.setText(rosyama.getTo());
			from.setText(rosyama.getFrom());
			address.setText(rosyama.getAddress());
			postaddress.setText(rosyama.getPostAddress());
			signature.setText(rosyama.getSignature());
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
		outState.putString(SAVED_TO, to.getText().toString());
		outState.putString(SAVED_FROM, from.getText().toString());
		outState.putString(SAVED_POSTADDRESS, postaddress.getText().toString());
		outState.putString(SAVED_ADDRESS, address.getText().toString());
		outState.putString(SAVED_SIGNATURE, signature.getText().toString());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.send:
			new GetTask().execute(to.getText().toString(), from.getText()
					.toString(), postaddress.getText().toString(), address
					.getText().toString(), signature.getText().toString());
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

	private class GetTask extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			try {
				((Rosyama) getApplication()).pdf(params[0], params[1],
						params[2], params[3], params[4]);
			} catch (LocalizedException e) {
				return getString(e.getResourceID());
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null)
				Toast.makeText(PDF.this, result, Toast.LENGTH_LONG).show();
			if (((Rosyama) getApplication()).getState() == State.pdfComplited) {
				Intent intent = new Intent(android.content.Intent.ACTION_SEND);
				intent.setType("text/plain");
				Uri uri = Uri.fromFile(((Rosyama) getApplication())
						.getPdfFile());
				intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
				startActivity(Intent.createChooser(intent,
						getString(R.string.email)));
				finish();
			}
		}
	}
}