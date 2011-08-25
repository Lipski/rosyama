package ru.redsolution.rosyama;

import java.util.Collection;

import ru.redsolution.rosyama.data.Photo;
import ru.redsolution.rosyama.data.Rosyama;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class Edit extends Activity implements OnClickListener {
	/**
	 * Extra парамент с этим именем должен содержать номер редактируемой ямы.
	 */
	public static final String EXTRA_ID = "ru.redsolution.rosyama.EXTRA_ID";

	/**
	 * Приложение.
	 */
	Rosyama rosyama;

	/**
	 * Дефект.
	 */
	ru.redsolution.rosyama.data.Hole hole;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hole_edit);
		rosyama = (Rosyama) getApplication();
		Intent intent = getIntent();
		Integer id;
		if (intent.hasExtra(EXTRA_ID))
			id = intent.getIntExtra(EXTRA_ID, 0);
		else
			id = null;
		hole = rosyama.getHole(id);
		for (int i : new int[] { R.id.send, R.id.delete })
			findViewById(i).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Collection<Photo> photos = hole.getPhotos();
		if (photos.size() == 0)
			((ImageView) findViewById(R.id.image))
					.setImageResource(R.drawable.no_image);
		else
			((ImageView) findViewById(R.id.image)).setImageURI(photos
					.iterator().next().getUrl());
		((TextView) findViewById(R.id.count)).setText(String.valueOf(photos
				.size()));
		((TextView) findViewById(R.id.address)).setText(hole.getAddress());
		findViewById(R.id.delete).setVisibility(
				hole.getId() == null ? View.GONE : View.VISIBLE);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.send:
			finish();
			break;
		case R.id.delete:
			finish();
			break;
		}
	}
}
