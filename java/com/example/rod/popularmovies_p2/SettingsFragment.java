package com.example.rod.popularmovies_p2;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import java.util.prefs.PreferenceChangeEvent;

/**
 * Created by rodrigo on 10/15/2017.
 */

public class SettingsFragment extends PreferenceFragmentCompat{


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }


}
