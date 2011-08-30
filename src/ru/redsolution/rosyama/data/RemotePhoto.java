package ru.redsolution.rosyama.data;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import ru.redsolution.rosyama.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class RemotePhoto extends AbstractPhoto {
	/**
	 * Адрес изображения с оригинальным разрешением.
	 */
	private Uri original;

	/**
	 * Адрес изображения с низким разрешением.
	 */
	private Uri small;

	/**
	 * Нужно ли удалять изображение с сервера.
	 */
	private boolean toRemove;

	public RemotePhoto(Rosyama rosyama, Uri original, Uri small) {
		super(rosyama);
		this.original = original;
		this.small = small;
		toRemove = false;
	}

	/**
	 * Установить пометку для удаления.
	 */
	public void remove() {
		toRemove = true;
	}

	/**
	 * Подлежит ли изображение удалению.
	 * 
	 * @return
	 */
	public boolean isToRemove() {
		return toRemove;
	}

	@Override
	Bitmap process(Void... params) throws LocalizedException {
		Bitmap preview;
		try {
			URL url = new URL(small.toString());
			URLConnection connection = url.openConnection();
			connection.connect();
			InputStream inputStream = connection.getInputStream();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(
					inputStream);
			preview = BitmapFactory.decodeStream(bufferedInputStream);
			bufferedInputStream.close();
			inputStream.close();
		} catch (IOException e) {
			throw new LocalizedException(R.string.io_fail);
		}
		return preview;
	}

	@Override
	public Uri getUri() {
		return original;
	}
}
