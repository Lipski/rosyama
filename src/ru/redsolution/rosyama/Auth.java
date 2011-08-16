package ru.redsolution.rosyama;

import ru.redsolution.rosyama.Rosyama.ExceptionWithResource;
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

public class Auth extends Activity implements OnClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth);
		findViewById(R.id.enter).setOnClickListener(this);
		findViewById(R.id.register).setOnClickListener(this);
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

	private class LoginTask extends AsyncTask<String, String, String> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(Auth.this);
			progressDialog.setMessage(getString(R.string.auth_request));
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			Rosyama rosyama = (Rosyama) getApplication();
			try {
				rosyama.authorize(params[0], params[1]);
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
			if (result == null) {
				Intent intent = new Intent(Auth.this, Main.class);
				startActivity(intent);
				finish();
			} else
				Toast.makeText(Auth.this, result, Toast.LENGTH_LONG).show();
		}
	}
}