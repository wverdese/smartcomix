package com.shockdom.model;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
	
	private Context c;
	private String prefs;
	
	public SharedPrefsManager(Context c, String prefs) {
		this.c = c;
		setPrefsName(prefs);
	}
	
	public void setPrefsName(String prefs) {
		if (prefs != null) {
			this.prefs = prefs;
		}
	}
	
	public void saveString(String name, String value) {
		SharedPreferences.Editor editor = c.getSharedPreferences(prefs, Context.MODE_PRIVATE).edit();
		if (value == null) {
			editor.remove(name);
		} else {
			editor.putString(name, value);
		}
		editor.commit();
	}

	public String loadString(String name, String defaultValue) {
		SharedPreferences p = c.getSharedPreferences(prefs, Context.MODE_PRIVATE); 
		return p.getString(name, defaultValue);
	}

	public void saveLong(String name, long value) {
		SharedPreferences.Editor editor = c.getSharedPreferences(prefs, Context.MODE_PRIVATE).edit();
		editor.putLong(name, value);
		editor.commit();
	}

	public long loadLong(String name, long defaultValue) {
		SharedPreferences p = c.getSharedPreferences(prefs, Context.MODE_PRIVATE); 
		return p.getLong(name, defaultValue);
	}

    public void saveInt(String name, int value) {
        SharedPreferences.Editor editor = c.getSharedPreferences(prefs, Context.MODE_PRIVATE).edit();
        editor.putInt(name, value);
        editor.commit();
    }

    public int loadInt(String name, int defaultValue) {
        SharedPreferences p = c.getSharedPreferences(prefs, Context.MODE_PRIVATE);
        return p.getInt(name, defaultValue);
    }
	
	public void saveBoolean(String name, boolean value) {
		SharedPreferences.Editor editor = c.getSharedPreferences(prefs, Context.MODE_PRIVATE).edit();
		editor.putBoolean(name, value);
		editor.commit();
	}

	public boolean loadBoolean(String name, boolean defaultValue) {
		SharedPreferences p = c.getSharedPreferences(prefs, Context.MODE_PRIVATE); 
		return p.getBoolean(name, defaultValue);
	}

	//TODO: altri tipi di dato supportati
	
	public void clearPreference(String name) {
		SharedPreferences.Editor editor = c.getSharedPreferences(prefs, Context.MODE_PRIVATE).edit();
		editor.remove(name);
		editor.commit();
	}

	public void clearAllPreferences() {
		SharedPreferences.Editor editor = c.getSharedPreferences(prefs, Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
	}


}
