package ru.redsolution.rosyama;

import java.util.Collection;

import ru.redsolution.rosyama.data.Photo;
import ru.redsolution.rosyama.data.Rosyama;
import ru.redsolution.rosyama.data.Status;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HoleAdapter extends BaseAdapter {
	/**
	 * Приложение.
	 */
	private final Rosyama rosyama;

	/**
	 * Конструктор layout-а.
	 */
	private LayoutInflater layoutInflater;

	/**
	 * Статус дефектов для отображения.
	 */
	private final Status status;

	public HoleAdapter(Activity activity, Status status) {
		rosyama = (Rosyama) activity.getApplication();
		layoutInflater = activity.getLayoutInflater();
		this.status = status;
	}

	@Override
	public int getCount() {
		int count = 0;
		for (ru.redsolution.rosyama.data.Hole hole : rosyama.getHoles())
			if (hole.getStatus() == status)
				count++;
		return count;
	}

	@Override
	public Object getItem(int position) {
		int count = 0;
		for (ru.redsolution.rosyama.data.Hole hole : rosyama.getHoles())
			if (hole.getStatus() == status)
				if (count == position)
					return hole;
		count++;
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			view = layoutInflater.inflate(R.layout.hole_item, parent, false);
		} else {
			view = convertView;
		}
		ru.redsolution.rosyama.data.Hole hole = (ru.redsolution.rosyama.data.Hole) getItem(position);
		Collection<Photo> photos = hole.getPhotos();
		if (photos.size() == 0)
			((ImageView) view.findViewById(R.id.image))
					.setImageResource(R.drawable.no_image);
		else
			((ImageView) view.findViewById(R.id.image)).setImageURI(photos
					.iterator().next().getUrl());
		((TextView) view.findViewById(R.id.address)).setText(hole.getAddress());
		return view;
	}

}
