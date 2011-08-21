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

public class Auth extends Activity implements OnClickListener, StateListener {
	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth);
		findViewById(R.id.enter).setOnClickListener(this);
		findViewById(R.id.register).setOnClickListener(this);

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
	public void onClick(View view) {
		Intent intent;
		switch (view.getId()) {
		case R.id.enter:
			new LoginTask().execute(((EditText) findViewById(R.id.login))
					.getText().toString(),
					((EditText) findViewById(R.id.password)).getText()
							.toString());
			break;
		case R.id.register:
			intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(Rosyama.REGISTER_URL));
			startActivity(intent);
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

	private class LoginTask extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			Rosyama rosyama = (Rosyama) getApplication();
			try {
				rosyama.authorize(params[0], params[1]);
			} catch (LocalizedException e) {
				return e.getString(Auth.this);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null)
				Toast.makeText(Auth.this, result, Toast.LENGTH_LONG).show();
			if (((Rosyama) getApplication()).getState().isAuthorized()) {
				Intent intent = new Intent(Auth.this, Main.class);
				startActivity(intent);
				finish();
			}
		}
	}
}