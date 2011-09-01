package ru.redsolution.rosyama;

import java.util.Collection;

import ru.redsolution.rosyama.data.AbstractPhoto;
import ru.redsolution.rosyama.data.Hole;
import ru.redsolution.rosyama.data.Rosyama;
import ru.redsolution.rosyama.data.Type;
import ru.redsolution.rosyama.data.UpdateListener;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class HoleEdit extends Activity implements OnClickListener,
		UpdateListener, DialogClickListener, OnItemSelectedListener,
		OnCancelListener {
	/**
	 * Extra парамент с этим именем должен содержать номер редактируемой ямы.
	 */
	public static final String EXTRA_ID = "ru.redsolution.rosyama.EXTRA_ID";

	/**
	 * Приложение.
	 */
	private Rosyama rosyama;

	/**
	 * Дефект.
	 */
	private Hole hole;

	/**
	 * Диалог выполнения задания.
	 */
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hole_edit);
		rosyama = (Rosyama) getApplication();

		hole = rosyama.getHole(getIntent().getStringExtra(EXTRA_ID));
		if (hole == null) {
			checkSendComplited();
			finish();
			return;
		}

		Spinner spinner = (Spinner) findViewById(R.id.type);
		spinner.setAdapter(new TypeAdapter(this));
		spinner.setOnItemSelectedListener(this);
		findViewById(R.id.photo_panel).setOnClickListener(this);
		findViewById(R.id.address_panel).setOnClickListener(this);
		findViewById(R.id.comment_panel).setOnClickListener(this);
		findViewById(R.id.send).setOnClickListener(this);
		findViewById(R.id.delete).setOnClickListener(this);

		progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(true);
		progressDialog.setOnCancelListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
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
			if (hole.getId() != null)
				intent.putExtra(EXTRA_ID, hole.getId());
			startActivity(intent);
			break;
		case R.id.address_panel:
			intent = new Intent(this, Map.class);
			if (hole.getId() != null)
				intent.putExtra(EXTRA_ID, hole.getId());
			startActivity(intent);
			break;
		case R.id.comment_panel:
			showDialog(R.id.comment_panel);
			break;
		case R.id.send:
			rosyama.getSendOperation().execute(hole);
			break;
		case R.id.delete:
			rosyama.getDeleteOperation().execute(hole);
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		switch (id) {
		case R.id.comment_panel:
			return new EditDialogBuilder(this, this, R.id.comment_panel,
					getString(R.string.comment_label), hole.getComment(),
					getString(R.string.comment_hint)).create();
		default:
			return null;
		}
	}

	@Override
	public void onAccept(DialogBuilder dialog) {
		switch (dialog.getDialogId()) {
		case R.id.comment_panel:
			hole.setComment(((EditDialogBuilder) dialog).getText());
			break;
		}
	}

	@Override
	public void onDecline(DialogBuilder dialog) {
	}

	@Override
	public void onCancel(DialogBuilder dialog) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.redsolution.rosyama.data.UpdateListener#onUpdate()
	 */
	@Override
	public void onUpdate() {
		Collection<AbstractPhoto> photos = hole.getVisiblePhotos();
		((TextView) findViewById(R.id.count)).setText(String.valueOf(photos
				.size()));
		InterfaceUtilities.setPreview((ImageView) findViewById(R.id.image),
				photos);

		InterfaceUtilities.setText((TextView) findViewById(R.id.address),
				hole.getAddress(), R.string.address_hint);
		InterfaceUtilities.setText((TextView) findViewById(R.id.comment),
				hole.getComment(), R.string.comment_hint);

		Type type = hole.getType();
		Spinner spinner = (Spinner) findViewById(R.id.type);
		int position = -1;
		if (type != null) {
			for (int index = 0; index < spinner.getCount(); index++)
				if (type == spinner.getItemAtPosition(index)) {
					position = index;
					break;
				}
		}
		spinner.setSelection(position);

		findViewById(R.id.delete).setVisibility(
				hole.getId() == null ? View.GONE : View.VISIBLE);

		findViewById(R.id.send).setEnabled(
				!hole.getAddress().equals("") && type != null);

		if (rosyama.getSendOperation().isInProgress()) {
			progressDialog.setMessage(getString(R.string.send_request));
			progressDialog.show();
		} else if (rosyama.getDeleteOperation().isInProgress()) {
			progressDialog.setMessage(getString(R.string.delete_request));
			progressDialog.show();
		} else
			progressDialog.dismiss();

		checkSendComplited();
		if (rosyama.getDeleteOperation().isComplited()) {
			finish();
			rosyama.getDeleteOperation().clear();
		}
	}

	/**
	 * Проверяет была ли завершена отправка и открывает предпросмотр дефекта.
	 */
	private void checkSendComplited() {
		if (rosyama.getSendOperation().isComplited()) {
			if (hole.getId() == null) {
				Intent intent = new Intent(this, HoleDetail.class);
				intent.putExtra(EXTRA_ID, rosyama.getSendOperation().getId());
				startActivity(intent);
			}
			finish();
			rosyama.getSendOperation().clear();
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		hole.setType((Type) parent.getItemAtPosition(position));
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		hole.setType(null);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		rosyama.getSendOperation().cancel();
		rosyama.getDeleteOperation().cancel();
	}
}
