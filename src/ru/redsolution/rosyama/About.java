package ru.redsolution.rosyama;

import android.app.Activity;
import android.os.Bundle;

public class About extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
	}

	@Override
	protected void onResume() {
		super.onResume();
		InterfaceUtilities.setTiledBackground(this, R.id.background,
				R.drawable.background);
		InterfaceUtilities.setTiledBackground(this, R.id.dot, R.drawable.dot);
		InterfaceUtilities.setTiledBackground(this, R.id.dot2, R.drawable.dot);
	}

}