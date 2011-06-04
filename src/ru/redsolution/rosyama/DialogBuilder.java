package ru.redsolution.rosyama;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class DialogBuilder extends AlertDialog.Builder implements
		DialogInterface.OnDismissListener, DialogInterface.OnClickListener {
	protected final Activity activity;
	protected final int dialogId;
	private final DialogClickListener listener;
	private State state;

	private static enum State {
		/**
		 * Dialog was canceled.
		 */
		canceled,

		/**
		 * Request was accepted.
		 */
		accepted,

		/**
		 * Request was declined.
		 */
		declined,
	}

	public DialogBuilder(Activity activity, DialogClickListener listener,
			int dialogId, String message) {
		super(activity);
		this.activity = activity;
		this.dialogId = dialogId;
		this.listener = listener;
		state = State.canceled;
		setMessage(message);
		setPositiveButton(activity.getString(android.R.string.yes), this);
		setNegativeButton(activity.getString(android.R.string.no), this);
	}

	@Override
	public AlertDialog create() {
		AlertDialog alertDialog = super.create();
		alertDialog.setOnDismissListener(this);
		return alertDialog;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		activity.removeDialog(dialogId);
		if (state == State.accepted) {
			listener.onAccept(this);
		} else if (state == State.declined) {
			listener.onDecline(this);
		} else { // State.canceled
			listener.onCancel(this);
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int id) {
		switch (id) {
		case DialogInterface.BUTTON_POSITIVE:
			state = State.accepted;
			dialog.dismiss();
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			state = State.declined;
			dialog.dismiss();
			break;
		}
	}

	public int getDialogId() {
		return dialogId;
	}
}
