package ru.redsolution.rosyama;

import java.util.Collection;

import ru.redsolution.rosyama.data.AbstractPhoto;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

public class InterfaceUtilities {
	/**
	 * Устанавливает изображение фотографии.
	 * 
	 * @param imageView
	 *            Элемент для размещения изображения.
	 * @param photo
	 *            Фотография для размещения. Если фотография не имеет
	 *            загруженного изображения или фотография равна
	 *            <code>null</code> будет отображаться изображение "нет фото".
	 */
	public static void setPreview(ImageView imageView, AbstractPhoto photo) {
		Bitmap bitmap;
		if (photo == null)
			bitmap = null;
		else
			bitmap = photo.getPreview();
		if (bitmap == null)
			imageView.setImageResource(R.drawable.ic_no_photo);
		else
			imageView.setImageBitmap(bitmap);
	}

	/**
	 * Устанавливает первое изображение из списка.
	 * 
	 * @param imageView
	 *            Элемент для размещения изображения.
	 * @param photos
	 *            Список фотографий.
	 */
	public static void setPreview(ImageView imageView,
			Collection<AbstractPhoto> photos) {
		AbstractPhoto photo;
		if (photos.isEmpty())
			photo = null;
		else
			photo = photos.iterator().next();
		setPreview(imageView, photo);
	}

	/**
	 * Устанавливает текст.
	 * 
	 * @param textView
	 *            Элемент для размещения текста.
	 * @param value
	 *            Значение.
	 * @param prompt
	 *            Ресурс с приглашением для рамещения контента. Отображается
	 *            если значение пустая строка.
	 */
	public static void setText(TextView textView, String value, int prompt) {
		if (value.equals(""))
			textView.setText(prompt);
		else
			textView.setText(value);
	}
}
