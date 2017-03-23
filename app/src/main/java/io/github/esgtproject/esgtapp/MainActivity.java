package io.github.esgtproject.esgtapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackback("Does nothing!");
            }
        });
    }

    private void showSnackback(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void pushConfig() {
        // Get preferences
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final Map<String,?> preferenceMap = mSharedPreferences.getAll();
        final String username = (String) preferenceMap.get("username");
        preferenceMap.remove("username");

        // Send POST request
        JSONObject configJson = new JSONObject(preferenceMap);
        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("config", configJson);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        Log.d(TAG, json.toString());
        post(getString(R.string.url_config), json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showSnackback("Could not send POST ...");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    Log.d(TAG, responseStr);
                    showSnackback("POST sent successfully!");
                } else {
                    Log.d(TAG, "POST FAILED");
                    showSnackback("Failed to send POST...");
                }
            }
        });
    }

    private Call get(String url, Map<String,String> params, Callback callback) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        HttpUrl.Builder builder = httpUrl.newBuilder();
        for (Map.Entry<String,String> entry : params.entrySet()) {
            builder.setQueryParameter(entry.getKey(), entry.getValue());
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(builder.toString())
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    private Call post(String url, JSONObject json, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(settingsIntent);
            return true;
        } else if (id == R.id.action_signout) {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences settings = getSharedPreferences(SignInActivity.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("signed_in", false);
            editor.apply();
            Intent mIntent = new Intent(this, SplashActivity.class);
            startActivity(mIntent);
            MainActivity.this.finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
