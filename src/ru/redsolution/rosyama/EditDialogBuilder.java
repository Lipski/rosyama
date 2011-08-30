package ru.redsolution.rosyama;

import android.app.Activity;
import android.widget.EditText;

public class EditDialogBuilder extends DialogBuilder {
	private final EditText editText;

	public EditDialogBuilder(Activity activity, DialogClickListener listener,
			int dialogId, String message, String value) {
		super(activity, listener, dialogId, message);
		editText = (EditText) activity.getLayoutInflater().inflate(
				R.layout.edit_dialog, null);
		editText.setText(value);
		setView(editText);
	}

	public String getText() {
		return editText.getText().toString();
	}

}
