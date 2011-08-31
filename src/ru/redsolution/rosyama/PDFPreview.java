package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.Hole;
import ru.redsolution.rosyama.data.Rosyama;
import ru.redsolution.rosyama.data.UpdateListener;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class PDFPreview extends Activity implements OnClickListener,
		UpdateListener, DialogClickListener, OnCancelListener {
	/**
	 * Extra парамент с этим именем должен содержать номер редактируемой ямы.
	 */
	public static final String EXTRA_ID = "ru.redsolution.rosyama.EXTRA_ID";

	/**
	 * Приложение.
	 */
	private Rosyama rosyama;

	/**
	 * Диалог выполнения задания.
	 */
	private ProgressDialog progressDialog;

	/**
	 * Дефект.
	 */
	private Hole hole;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pdf_preview);
		rosyama = (Rosyama) getApplication();
		hole = rosyama.getHole(getIntent().getStringExtra(EXTRA_ID));
		if (hole == null || hole.getId() == null) {
			finish();
			return;
		}

		if (savedInstanceState == null) {
			rosyama.getHeadOperation().execute(hole);
		}

		progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(true);
		progressDialog.setOnCancelListener(this);

		findViewById(R.id.to_panel).setOnClickListener(this);
		findViewById(R.id.from_panel).setOnClickListener(this);
		findViewById(R.id.postaddress_panel).setOnClickListener(this);
		findViewById(R.id.signature_panel).setOnClickListener(this);
		findViewById(R.id.request).setOnClickListener(this);
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
		switch (view.getId()) {
		default:
			showDialog(view.getId());
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		switch (id) {
		case R.id.to_panel:
			return new EditDialogBuilder(this, this, R.id.to_panel,
					getString(R.string.to_label), hole.getTo()).create();
		case R.id.from_panel:
			return new EditDialogBuilder(this, this, R.id.from_panel,
					getString(R.string.from_label), rosyama.getFrom()).create();
		case R.id.postaddress_panel:
			return new EditDialogBuilder(this, this, R.id.postaddress_panel,
					getString(R.string.postaddress_label),
					rosyama.getPostAddress()).create();
		case R.id.signature_panel:
			return new EditDialogBuilder(this, this, R.id.signature_panel,
					getString(R.string.signature_label), rosyama.getSignature())
					.create();
		case R.id.request:
			return new DialogBuilder(this, this, R.id.request,
					getString(R.string.pdf_dialog)).create();
		default:
			return null;
		}
	}

	@Override
	public void onAccept(DialogBuilder dialog) {
		String text;
		if (dialog instanceof EditDialogBuilder)
			text = ((EditDialogBuilder) dialog).getText();
		else
			text = null;
		switch (dialog.getDialogId()) {
		case R.id.to_panel:
			hole.setTo(text);
			break;
		case R.id.from_panel:
			rosyama.setFrom(text);
			break;
		case R.id.postaddress_panel:
			rosyama.setPostAddress(text);
			break;
		case R.id.signature_panel:
			rosyama.setSignature(text);
			break;
		case R.id.request:
			rosyama.getPDFOperation().execute(hole);
			break;
		}
	}

	@Override
	public void onDecline(DialogBuilder dialog) {
	}

	@Override
	public void onCancel(DialogBuilder dialog) {
	}

	@Override
	public void onUpdate() {
		InterfaceUtilities.setText((TextView) findViewById(R.id.to),
				hole.getTo(), R.string.to_prompt);
		InterfaceUtilities.setText((TextView) findViewById(R.id.from),
				rosyama.getFrom(), R.string.from_prompt);
		InterfaceUtilities.setText((TextView) findViewById(R.id.postaddress),
				rosyama.getPostAddress(), R.string.postaddress_prompt);
		InterfaceUtilities.setText((TextView) findViewById(R.id.signature),
				rosyama.getSignature(), R.string.signature_prompt);

		if (rosyama.getHeadOperation().isInProgress()) {
			progressDialog.setMessage(getString(R.string.head_request));
			progressDialog.show();
		} else if (rosyama.getPDFOperation().isInProgress()) {
			progressDialog.setMessage(getString(R.string.pdf_request));
			progressDialog.show();
		} else
			progressDialog.dismiss();

		if (rosyama.getPDFOperation().isComplited()) {
			Intent intent = new Intent(android.content.Intent.ACTION_SEND);
			intent.setType("text/plain");
			Uri uri = Uri.fromFile(rosyama.getPDFOperation().getFile());
			intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
			startActivity(Intent.createChooser(intent,
					getString(R.string.pdf_choose)));
			finish();
			rosyama.getPDFOperation().clear();
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		rosyama.getHeadOperation().cancel();
		rosyama.getPDFOperation().cancel();
	}
}
