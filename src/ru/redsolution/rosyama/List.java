package ru.redsolution.rosyama;

import ru.redsolution.rosyama.data.Status;
import android.app.TabActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TabHost;

public class List extends TabActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hole_list);
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		spec = tabHost.newTabSpec("created");
		spec.setIndicator("Новые");
		spec.setContent(R.id.created);
		tabHost.addTab(spec);
		((ListView) findViewById(R.id.created)).setAdapter(new HoleAdapter(
				this, Status.fresh));

		spec = tabHost.newTabSpec("pending");
		spec.setIndicator("В ожидании");
		spec.setContent(R.id.pending);
		tabHost.addTab(spec);
		((ListView) findViewById(R.id.pending)).setAdapter(new HoleAdapter(
				this, Status.inprogress));

		spec = tabHost.newTabSpec("fixed");
		spec.setIndicator("Устраненные");
		spec.setContent(R.id.fixed);
		tabHost.addTab(spec);
		((ListView) findViewById(R.id.fixed)).setAdapter(new HoleAdapter(this,
				Status.fixed));

	}
}
