package nanodegree.gemma.alexandria;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by saj on 27/01/15.
 * Updated by Gemma Lara 02/2016 for Udacity Android Developer Nanodegree
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);


    }
}
