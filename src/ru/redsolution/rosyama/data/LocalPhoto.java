package ru.redsolution.rosyama.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import ru.redsolution.rosyama.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class LocalPhoto extends AbstractPhoto {
	/**
	 * Путь до изображения.
	 */
	Uri path;

	public LocalPhoto(Rosyama rosyama, Uri path) {
		super(rosyama);
		this.path = path;
	}

	@Override
	Bitmap process(Void... params) throws LocalizedException {
		try {
			// Определяем размеры изображения
			BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
			sizeOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(path.getPath()),
					null, sizeOptions);

			// Находим оптимальный множитель
			int width_tmp = sizeOptions.outWidth;
			int height_tmp = sizeOptions.outHeight;
			int scale = 1;
			while (width_tmp / 2 >= Rosyama.IMAGE_PREVIEW_SIZE
					&& height_tmp / 2 >= Rosyama.IMAGE_PREVIEW_SIZE) {
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// Загружаем изображение
			BitmapFactory.Options resultOptions = new BitmapFactory.Options();
			resultOptions.inSampleSize = scale;
			return BitmapFactory.decodeStream(
					new FileInputStream(path.getPath()), null, resultOptions);
		} catch (FileNotFoundException e) {
			throw new LocalizedException(R.string.io_fail);
		}
	}

	@Override
	public Uri getUri() {
		return path;
	}
}
