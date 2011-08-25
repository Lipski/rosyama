package ru.redsolution.rosyama.data;

import android.net.Uri;

/**
 * Фотография дефекта.
 * 
 * @author alexander.ivanov
 * 
 */
public class Photo {
	/**
	 * Путь до фотографии на устройстве.
	 */
	private Uri path;

	/**
	 * Адрес фотографии в сети.
	 */
	private Uri url;

	/**
	 * Идет процесс загрузки фотографии.
	 */
	private boolean requested;

	public Photo(Uri path, Uri url) {
		this.path = path;
		this.url = url;
		requested = false;
	}

	public Uri getPath() {
		return path;
	}

	public Uri getUrl() {
		return url;
	}

	public boolean isRequested() {
		return requested;
	}

	public void download() {
		if (requested || path != null)
			return;
		// TODO:
	}
}
