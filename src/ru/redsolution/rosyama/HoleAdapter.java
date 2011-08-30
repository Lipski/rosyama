package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.Hole;
import ru.redsolution.rosyama.data.Rosyama;
import ru.redsolution.rosyama.data.Status;
import ru.redsolution.rosyama.data.Type;
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
		return rosyama.getHoles(status).size();
	}

	@Override
	public Object getItem(int position) {
		return rosyama.getHoles(status).get(position);
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
		Hole hole = (Hole) getItem(position);

		InterfaceUtilities.setPreview(
				(ImageView) view.findViewById(R.id.image),
				hole.getVisiblePhotos());

		((TextView) view.findViewById(R.id.date))
				.setText(hole.getCreatedText());
		((TextView) view.findViewById(R.id.address)).setText(hole.getAddress());

		Type type = hole.getType();
		if (type != null) {
			((ImageView) view.findViewById(R.id.type_image)).setImageLevel(type
					.ordinal());
			((TextView) view.findViewById(R.id.type_text)).setText(type
					.getResourceID());
		}
		return view;
	}
}
