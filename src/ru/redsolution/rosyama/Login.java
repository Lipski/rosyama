package ru.redsolution.rosyama;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
	public void onClick(View view) {
		Intent intent;
		switch (view.getId()) {
		case R.id.enter:
			if (((Rosyama) getApplication()).login(
					((EditText) findViewById(R.id.login)).getText().toString(),
					((EditText) findViewById(R.id.password)).getText()
							.toString())) {
				intent = new Intent(this, Main.class);
				startActivity(intent);
				finish();
			} else {
				Toast.makeText(this, getString(R.string.auth_fail),
						Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.register:
			intent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("http://rosyama.ru/personal/holes.php?register=yes"));
			startActivity(intent);
			break;
		}
	}
}