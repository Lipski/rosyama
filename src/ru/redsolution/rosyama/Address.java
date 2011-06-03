package ru.redsolution.rosyama;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class Address extends Activity implements OnClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.address);
		findViewById(R.id.send).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.send:
			if (!((Rosyama) getApplication()).login()) {
				Toast.makeText(this, getString(R.string.auth_fail),
						Toast.LENGTH_LONG).show();
				break;
			}
			if (!((Rosyama) getApplication()).add(
					((EditText) findViewById(R.id.address)).getText()
							.toString(),
					((EditText) findViewById(R.id.comment)).getText()
							.toString())) {
				Toast.makeText(this, getString(R.string.add_fail),
						Toast.LENGTH_LONG).show();
				break;
			}
			break;
		case R.id.cancel:
			finish();
			break;
		}
	}
}