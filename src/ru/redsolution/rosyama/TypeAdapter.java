package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.Type;
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
public class TypeAdapter extends BaseAdapter {
	/**
	 * Поддерживаемые типы.
	 */
	private static Type[] TYPES = new Type[] { Type.badroad, Type.holeonroad,
			Type.hatch, Type.rails, Type.holeinyard, };

	/**
	 * Конструктор layout-а.
	 */
	private LayoutInflater layoutInflater;

	public TypeAdapter(Activity activity) {
		layoutInflater = activity.getLayoutInflater();
	}

	@Override
	public int getCount() {
		return TYPES.length;
	}

	@Override
	public Object getItem(int position) {
		return TYPES[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent, R.layout.type_item);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent, R.layout.type_dropdown);
	}

	/**
	 * Наполняет view контентом. При необходимости создает его из указанного
	 * layout-а.
	 * 
	 * @param position
	 * @param convertView
	 * @param parent
	 * @param layout
	 * @return
	 */
	private View getView(int position, View convertView, ViewGroup parent,
			int layout) {
		View view;
		if (convertView == null) {
			view = layoutInflater.inflate(layout, parent, false);
		} else {
			view = convertView;
		}
		Type type = (Type) getItem(position);
		((ImageView) view.findViewById(R.id.icon))
				.setImageLevel(type.ordinal());
		((TextView) view.findViewById(R.id.name)).setText(type.getResourceID());
		return view;
	}
}
