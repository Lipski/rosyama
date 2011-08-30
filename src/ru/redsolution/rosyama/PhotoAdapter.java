package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.AbstractPhoto;
import ru.redsolution.rosyama.data.Hole;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Адаптер для списка фотографий.
 * 
 * @author alexander.ivanov
 * 
 */
public class PhotoAdapter extends BaseAdapter {
	/**
	 * Конструктор layout-а.
	 */
	private LayoutInflater layoutInflater;

	/**
	 * Дефект.
	 */
	private final Hole hole;

	public PhotoAdapter(Activity activity, Hole hole) {
		layoutInflater = activity.getLayoutInflater();
		this.hole = hole;
	}

	@Override
	public int getCount() {
		return hole.getVisiblePhotos().size();
	}

	@Override
	public Object getItem(int position) {
		return hole.getVisiblePhotos().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			view = layoutInflater.inflate(R.layout.photo_item, parent, false);
		} else {
			view = convertView;
		}
		InterfaceUtilities.setPreview(
				(ImageView) view.findViewById(R.id.image),
				(AbstractPhoto) getItem(position));
		((TextView) view.findViewById(R.id.hint)).setText(layoutInflater
				.getContext().getString(R.string.photo_hint, position + 1));
		return view;
	}
}
