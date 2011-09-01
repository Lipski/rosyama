package ru.redsolution.rosyama;

import java.util.Collection;

import ru.redsolution.rosyama.data.AbstractPhoto;
import ru.redsolution.rosyama.data.Hole;
import ru.redsolution.rosyama.data.Rosyama;
import ru.redsolution.rosyama.data.Status;
import ru.redsolution.rosyama.data.Type;
import ru.redsolution.rosyama.data.UpdateListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class HoleDetail extends Activity implements OnClickListener,
		UpdateListener {
	/**
	 * Приложение.
	 */
	private Rosyama rosyama;

	/**
	 * Дефект.
	 */
	private Hole hole;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hole_detail);
		rosyama = (Rosyama) getApplication();

		hole = rosyama.getHole(getIntent().getStringExtra(HoleEdit.EXTRA_ID));
		if (hole == null || hole.getId() == null) {
			finish();
			return;
		}

		findViewById(R.id.photo_panel).setOnClickListener(this);
		findViewById(R.id.pdf_preview).setOnClickListener(this);

		if (hole.getStatus() == Status.fresh)
			findViewById(R.id.hole_edit).setVisibility(View.VISIBLE);
		else
			findViewById(R.id.hole_edit).setVisibility(View.GONE);
		findViewById(R.id.hole_edit).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Hole newHole = rosyama.getHole(hole.getId());
		if (newHole != hole) {
			if (newHole != null) {
				Intent intent = new Intent(this, HoleDetail.class);
				intent.putExtra(HoleEdit.EXTRA_ID, hole.getId());
				startActivity(intent);
			}
			finish();
			return;
		}
		((Rosyama) getApplication()).setUpdateListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		((Rosyama) getApplication()).setUpdateListener(null);
	}

	@Override
	public void onClick(View view) {
		Intent intent;
		switch (view.getId()) {
		case R.id.photo_panel:
			intent = new Intent(this, PhotoList.class);
			intent.putExtra(HoleEdit.EXTRA_ID, hole.getId());
			intent.putExtra(PhotoList.EXRTA_READ_ONLY, true);
			startActivity(intent);
			break;
		case R.id.pdf_preview:
			intent = new Intent(this, PDFPreview.class);
			intent.putExtra(HoleEdit.EXTRA_ID, hole.getId());
			startActivity(intent);
			break;
		case R.id.hole_edit:
			intent = new Intent(this, HoleEdit.class);
			intent.putExtra(HoleEdit.EXTRA_ID, hole.getId());
			startActivity(intent);
			break;
		}
	}

	@Override
	public void onUpdate() {
		Collection<AbstractPhoto> photos = hole.getVisiblePhotos();
		((TextView) findViewById(R.id.count)).setText(String.valueOf(photos
				.size()));
		InterfaceUtilities.setPreview((ImageView) findViewById(R.id.image),
				photos);

		((TextView) findViewById(R.id.date)).setText(hole.getCreatedText());
		((TextView) findViewById(R.id.address)).setText(hole.getAddress());
		((TextView) findViewById(R.id.comment)).setText(hole.getComment());

		Type type = hole.getType();
		if (type != null) {
			((ImageView) findViewById(R.id.type_image)).setImageLevel(type
					.ordinal());
			((TextView) findViewById(R.id.type_text)).setText(type
					.getResourceID());
		}
	}
}
