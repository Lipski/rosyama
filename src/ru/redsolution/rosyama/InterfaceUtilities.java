package ru.redsolution.rosyama;

import java.util.Collection;

import ru.redsolution.rosyama.data.AbstractPhoto;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
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

	/**
	 * Принудительно позволяет замостить фон view.
	 * 
	 * @param activity
	 *            Родительское активити.
	 * @param view
	 *            Ресурс объекта для размещения фона.
	 * @param drawable
	 *            Ресурс фона, подлежащего повторению.
	 */
	public static void setTiledBackground(Activity activity, int view,
			int drawable) {
		Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(),
				drawable);
		BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
		bitmapDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		activity.findViewById(view).setBackgroundDrawable(bitmapDrawable);
	}
}
