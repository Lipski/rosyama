package ru.redsolution.rosyama;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class Preference extends PreferenceActivity implements
		OnPreferenceChangeListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		EditTextPreference preference = (EditTextPreference) findPreference(getString(R.string.login_key));
		preference.setOnPreferenceChangeListener(this);
		preference.setSummary(preference.getText());
	}

	@Override
	public boolean onPreferenceChange(android.preference.Preference preference,
			Object newValue) {
		preference.setSummary((String) newValue);
		return true;
	}
}
