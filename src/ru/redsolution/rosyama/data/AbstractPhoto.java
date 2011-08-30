package ru.redsolution.rosyama.data;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Фотография дефекта.
 * 
 * @author alexander.ivanov
 * 
 */
public abstract class AbstractPhoto {
	/**
	 * Приложение.
	 */
	private final Rosyama rosyama;

	/**
	 * Предпросмотр изображения.
	 */
	private Bitmap preview;

	/**
	 * Задача для создания предпросмотра.
	 */
	private AbstractTask<Void, Bitmap> makePreview;

	public AbstractPhoto(Rosyama rosyama) {
		this.rosyama = rosyama;
		preview = null;
		makePreview = null;
	}

	/**
	 * Возвращает предпросмотр фотографии. Значение кешируется, его размеры при
	 * необходимости подгоняются.
	 * 
	 * @return
	 */
	public Bitmap getPreview() {
		if (makePreview != null)
			return preview;
		makePreview = new AbstractTask<Void, Bitmap>(getRosyama()) {
			@Override
			Bitmap process(Void... params) throws LocalizedException {
				return AbstractPhoto.this.process(params);
			}

			@Override
			void onSuccess(Bitmap result) {
				setPreview(result);
			}

			@Override
			void onFinish() {
			}
		};
		makePreview.execute();
		return null;
	}

	/**
	 * Устанавливает загруженный предпросмотр.
	 * 
	 * @param bitmap
	 */
	void setPreview(Bitmap preview) {
		this.preview = preview;
	}

	/**
	 * Возвражает приложение.
	 * 
	 * @return
	 */
	Rosyama getRosyama() {
		return rosyama;
	}

	/**
	 * Формирует предпросмотра в фоне.
	 * 
	 * @return
	 */
	abstract Bitmap process(Void... params) throws LocalizedException;

	/**
	 * Возвращает адрес для полного просмотра изображения в стороннем
	 * приложении.
	 * 
	 * @return
	 */
	public abstract Uri getUri();
}
