package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.Hole;
import ru.redsolution.rosyama.data.Rosyama;
import ru.redsolution.rosyama.data.Status;
import ru.redsolution.rosyama.data.UpdateListener;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TabHost;

public class HoleList extends TabActivity implements OnItemClickListener,
		UpdateListener, OnCancelListener {
	/**
	 * Приложение.
	 */
	private Rosyama rosyama;

	/**
	 * Диалог выполнения задания.
	 */
	private ProgressDialog progressDialog;

	private ListView freshListView;
	private ListView inprogressListView;
	private ListView fixedListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hole_list);
		rosyama = (Rosyama) getApplication();

		addTabSpec("fresh", R.string.status_fresh, R.id.fresh);
		freshListView = (ListView) findViewById(R.id.fresh);
		freshListView.setAdapter(new HoleAdapter(this, Status.fresh));
		freshListView.setOnItemClickListener(this);

		addTabSpec("inprogress", R.string.status_inprogress, R.id.inprogress);
		inprogressListView = (ListView) findViewById(R.id.inprogress);
		inprogressListView.setAdapter(new HoleAdapter(this, Status.inprogress));
		inprogressListView.setOnItemClickListener(this);

		addTabSpec("fixed", R.string.status_fixed, R.id.fixed);
		fixedListView = (ListView) findViewById(R.id.fixed);
		fixedListView.setAdapter(new HoleAdapter(this, Status.fixed));
		fixedListView.setOnItemClickListener(this);

		if (savedInstanceState == null) {
			rosyama.getListOperation().execute();
		}

		progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(true);
		progressDialog.setOnCancelListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		((Rosyama) getApplication()).setUpdateListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		((Rosyama) getApplication()).setUpdateListener(null);
	}

	/**
	 * Добавляет новый таб.
	 * 
	 * @param tag
	 *            Тег.
	 * @param name
	 *            Имя.
	 * @param content
	 *            Идентификатор закладки.
	 */
	private void addTabSpec(String tag, int name, int content) {
		TabHost tabHost = getTabHost();
		TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
		tabSpec.setIndicator(getString(name));
		tabSpec.setContent(content);
		tabHost.addTab(tabSpec);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Hole hole = (Hole) parent.getAdapter().getItem(position);
		if (hole == null)
			// Footer
			return;
		Intent intent = new Intent(this, HoleDetail.class);
		intent.putExtra(HoleEdit.EXTRA_ID, hole.getId());
		startActivity(intent);
	}

	@Override
	public void onUpdate() {
		((HoleAdapter) freshListView.getAdapter()).notifyDataSetChanged();
		((HoleAdapter) inprogressListView.getAdapter()).notifyDataSetChanged();
		((HoleAdapter) fixedListView.getAdapter()).notifyDataSetChanged();

		if (rosyama.getListOperation().isInProgress()) {
			progressDialog.setMessage(getString(R.string.list_request));
			progressDialog.show();
		} else
			progressDialog.dismiss();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		finish();
	}
}
