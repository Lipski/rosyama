package ru.redsolution.rosyama;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class About extends Activity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		findViewById(R.id.about_source).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		InterfaceUtilities.setTiledBackground(this, R.id.background,
				R.drawable.background);
		InterfaceUtilities.setTiledBackground(this, R.id.dot, R.drawable.dot);
		InterfaceUtilities.setTiledBackground(this, R.id.dot2, R.drawable.dot);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.about_source:
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse(getString(R.string.about_source_address))));
			break;
		}
	}
}