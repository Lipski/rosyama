package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.Rosyama;
import ru.redsolution.rosyama.data.UpdateListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class Auth extends Activity implements OnClickListener, UpdateListener,
		OnCancelListener {
	private static final String SAVED_LOGIN = "SAVED_LOGIN";
	private static final String SAVED_PASSWORD = "SAVED_PASSWORD";

	/**
	 * Приложениею.
	 */
	private Rosyama rosyama;

	/**
	 * Диалог выполнения задания.
	 */
	private ProgressDialog progressDialog;

	/**
	 * Поле ввода логина.
	 */
	private EditText loginView;

	/**
	 * Поле ввода логина.
	 */
	private EditText passwordView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth);
		rosyama = (Rosyama) getApplication();

		loginView = (EditText) findViewById(R.id.login);
		passwordView = (EditText) findViewById(R.id.password);
		if (savedInstanceState != null) {
			loginView.setText(savedInstanceState.getString(SAVED_LOGIN));
			passwordView.setText(savedInstanceState.getString(SAVED_PASSWORD));
		} else {
			loginView.setText(rosyama.getLogin());
			passwordView.setText(rosyama.getPassword());
		}

		findViewById(R.id.enter).setOnClickListener(this);
		findViewById(R.id.register).setOnClickListener(this);

		progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(true);
		progressDialog.setOnCancelListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		((Rosyama) getApplication()).setUpdateListener(this);
		InterfaceUtilities.setTiledBackground(this, R.id.background,
				R.drawable.background);
		InterfaceUtilities.setTiledBackground(this, R.id.dot, R.drawable.dot);
	}

	@Override
	protected void onPause() {
		super.onPause();
		((Rosyama) getApplication()).setUpdateListener(null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_LOGIN, loginView.getText().toString());
		outState.putString(SAVED_PASSWORD, passwordView.getText().toString());
	}

	@Override
	public void onClick(View view) {
		Intent intent;
		switch (view.getId()) {
		case R.id.enter:
			rosyama.getAuthorizeOperation().execute(
					loginView.getText().toString(),
					passwordView.getText().toString());
			break;
		case R.id.register:
			intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(Rosyama.REGISTER_URL));
			startActivity(intent);
			break;
		}
	}

	@Override
	public void onUpdate() {
		if (rosyama.getAuthorizeOperation().isComplited()) {
			Intent intent = new Intent(Auth.this, Main.class);
			startActivity(intent);
			finish();
		} else {
			if (rosyama.getAuthorizeOperation().isInProgress()) {
				progressDialog
						.setMessage(getString(R.string.authorize_request));
				progressDialog.show();
			} else
				progressDialog.dismiss();
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		rosyama.getAuthorizeOperation().cancel();
	}
}