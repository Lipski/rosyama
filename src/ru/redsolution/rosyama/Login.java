package ru.redsolution.rosyama;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity implements OnClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		findViewById(R.id.enter).setOnClickListener(this);
		findViewById(R.id.register).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.enter:
			String login = ((EditText) findViewById(R.id.login)).getText()
					.toString();
			String password = ((EditText) findViewById(R.id.password))
					.getText().toString();
			if (((Rosyama) getApplication()).login(login, password)) {
				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(getBaseContext());
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(getString(R.string.login_key), login);
				editor.putString(getString(R.string.password_key), password);
				editor.commit();
				Intent intent = new Intent(this, Main.class);
				startActivity(intent);
				finish();
			} else {
				Toast.makeText(this, getString(R.string.auth_fail),
						Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.register:
			Intent browserIntent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("http://rosyama.ru/personal/holes.php?register=yes"));
			startActivity(browserIntent);
			break;
		}
	}
}