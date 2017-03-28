package io.github.esgtproject.esgtapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    // Update database in realtime
    private DatabaseReference mDatabase;
    private SharedPreferences mSharedPreferences;
    // Used to prevent sending updates to Firebase before pulling data from Firebase
    private boolean refreshedFromFirebase;
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (refreshedFromFirebase) {
                        pushPrefsToFirebase(SettingsActivity.this, mDatabase);
                    }
                }
            };
    private static String TAG = "SettingsActivity";

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_notification_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Not updated from Firebase yet
        refreshedFromFirebase = false; //TODO: Do not push preferences until update from server

        // Read from the database (Only once)
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                updatePrefsFromFirebase(SettingsActivity.this, dataSnapshot);
                ((SettingsFragment)getFragmentManager().findFragmentById(android.R.id.content)).updatePreferenceSummaries();
                refreshedFromFirebase = true; //TODO: Do not push preferences until update from server
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        // Shared preference listener attached update changes to Firebase
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mPrefListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pushPrefsToFirebase(this, mDatabase); //TODO: Used to send default preference if user does not change anything
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mPrefListener);
    }


    // Update preferences from Firebase database
    private static void updatePrefsFromFirebase(Context mContext, DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() != null) {

            Log.d(TAG, dataSnapshot.getValue().toString());

            // Get shared preferences
            SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor mEditor = mSharedPreferences.edit();

            Map<String, String> preferenceMap = (Map<String,String>)dataSnapshot.getValue();

            // Restore from Firebase preference
            Iterator it = preferenceMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                if (pair.getKey().equals(mContext.getString(R.string.pref_use_imperial_key))) {
                    mEditor.putBoolean(pair.getKey().toString(), Boolean.parseBoolean(pair.getValue().toString()));
                } else {
                    mEditor.putString(pair.getKey().toString(), pair.getValue().toString());
                }
                it.remove();
            }
            mEditor.apply();
        }
    }

    // Push preferences to Firebase servers as Json
    private static void pushPrefsToFirebase(Context mContext, DatabaseReference mDatabase) {
        // Get preferences
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Map<String,?> preferenceMap = mSharedPreferences.getAll();

        // Update firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            mDatabase.setValue(preferenceMap);
        }
    }
    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_username_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_display_name_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_time_zone_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_weather_location_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_news_source_key)));
            // TODO: Bind imperial temperature
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), MainActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        public void updatePreferenceSummaries() {
            updatePreferenceSummary(findPreference(getString(R.string.pref_username_key)));
            updatePreferenceSummary(findPreference(getString(R.string.pref_display_name_key)));
            updatePreferenceSummary(findPreference(getString(R.string.pref_time_zone_key)));
            updatePreferenceSummary(findPreference(getString(R.string.pref_weather_location_key)));
            updatePreferenceSummary(findPreference(getString(R.string.pref_news_source_key)));
        }

        private void updatePreferenceSummary(Preference preference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }
}
