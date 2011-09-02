package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.Status;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StatusAdapter extends BaseAdapter {
	/**
	 * Конструктор layout-а.
	 */
	private LayoutInflater layoutInflater;

	public StatusAdapter(Activity activity) {
		layoutInflater = activity.getLayoutInflater();
	}

	@Override
	public int getCount() {
		return Status.values().length;
	}

	@Override
	public Object getItem(int position) {
		return Status.values()[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			view = layoutInflater.inflate(R.layout.status_item, parent, false);
		} else {
			view = convertView;
		}
		Status status = (Status) getItem(position);
		((TextView) view).setText(status.getResourceID());
		return view;
	}
}
