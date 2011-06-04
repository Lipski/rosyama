package ru.redsolution.rosyama;

/**
 * Listener for choice in {@link RequestDialog}.
 */
public interface DialogClickListener {
	/**
	 * Request was accepted.
	 */
	void onAccept(DialogBuilder dialog);

	/**
	 * Request was declined.
	 */
	void onDecline(DialogBuilder dialog);

	/**
	 * Request was canceled.
	 */
	void onCancel(DialogBuilder dialog);
}
