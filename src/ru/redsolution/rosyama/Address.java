package ru.redsolution.rosyama;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class Address extends Activity implements OnClickListener {
	private static final String SAVED_AREA = "SAVED_AREA";
	private static final String SAVED_LOCALITY = "SAVED_LOCALITY";
	private static final String SAVED_ADDRESS = "SAVED_ADDRESS";
	private static final String SAVED_COMMENT = "SAVED_COMMENT";

	Rosyama rosyama;

	EditText area;
	EditText locality;
	EditText address;
	EditText comment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.address);
		rosyama = (Rosyama) getApplication();
		findViewById(R.id.send).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
		area = (EditText) findViewById(R.id.area);
		locality = (EditText) findViewById(R.id.locality);
		address = (EditText) findViewById(R.id.address);
		comment = (EditText) findViewById(R.id.comment);
		if (savedInstanceState != null) {
			area.setText(savedInstanceState.getString(SAVED_AREA));
			locality.setText(savedInstanceState.getString(SAVED_LOCALITY));
			address.setText(savedInstanceState.getString(SAVED_ADDRESS));
			comment.setText(savedInstanceState.getString(SAVED_COMMENT));
		} else {
			area.setText(rosyama.getArea());
			locality.setText(rosyama.getLocality());
			address.setText(rosyama.getAddress());
			comment.setText(rosyama.getComment());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_AREA, area.getText().toString());
		outState.putString(SAVED_LOCALITY, locality.getText().toString());
		outState.putString(SAVED_ADDRESS, address.getText().toString());
		outState.putString(SAVED_COMMENT, comment.getText().toString());
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
			if (!((Rosyama) getApplication()).add(area.getText().toString(),
					locality.getText().toString(),
					address.getText().toString(), comment.getText().toString())) {
				Toast.makeText(this, getString(R.string.add_fail),
						Toast.LENGTH_LONG).show();
				break;
			}
			finish();
			break;
		case R.id.cancel:
			finish();
			break;
		}
	}
}