package com.srenner.billminder;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.Window;

public class ActPreferences extends ActionBarPreferenceActivity implements OnSharedPreferenceChangeListener {
	SharedPreferences mPreferences;
	private boolean mReloadAfterwards = false;
	//SharedPreferences.OnSharedPreferenceChangeListener mPrefChangedListener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	    mPreferences.registerOnSharedPreferenceChangeListener(this);
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
        		Intent intent = new Intent();
        		intent.putExtra("prefsReload", mReloadAfterwards);
        		setResult(RESULT_OK, intent);
            	finish();
                break;


        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		mReloadAfterwards = true;
	}
	
	@Override
	public void onBackPressed() {
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String loginURL = mPreferences.getString("prefLoginURL", "");
		String indexURL = mPreferences.getString("prefIndexURL", "");
        String username = mPreferences.getString("prefUsername", "");
        String password = mPreferences.getString("prefPassword", "");
        if(loginURL.length() == 0 || indexURL.length() == 0 || username.length() == 0 || password.length() == 0) {
        	mReloadAfterwards = false;
        }
		Intent intent = new Intent();
		intent.putExtra("prefsReload", mReloadAfterwards);
		setResult(RESULT_OK, intent);		
		super.onBackPressed();
	}
}
