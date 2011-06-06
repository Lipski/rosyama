package ru.redsolution.rosyama;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class Form extends Activity implements OnClickListener {
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form);
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
			postaddress.setText(rosyama.getPostaddress());
			signature.setText(rosyama.getSignature());
		}
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
		Intent intent;
		switch (v.getId()) {
		case R.id.send:
			if (((Rosyama) getApplication()).pdf(to.getText().toString(), from
					.getText().toString(), postaddress.getText().toString(),
					address.getText().toString(), signature.getText()
							.toString())) {
				intent = new Intent(android.content.Intent.ACTION_SEND);
				intent.setType("text/plain");
				Uri uri = Uri.fromFile(((Rosyama) getApplication()).getPdf());
				System.out.println(uri);
				intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
				startActivity(Intent.createChooser(intent,
						getString(R.string.email)));
			} else {
				Toast.makeText(this, getString(R.string.pdf_fail),
						Toast.LENGTH_LONG).show();
			}
			finish();
			break;
		case R.id.cancel:
			finish();
			break;
		}
	}
}