package io.github.esgtproject.esgtapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

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
                SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                Map<String,?> map = mSharedPreferences.getAll();
                String username = (String) map.get("username");
                map.remove("username");
                JSONObject configJson = new JSONObject(map);
                JSONObject json = new JSONObject();
                try {
                    json.put("username", username);
                    json.put("config", configJson);
                    json.put("refresh_tokens", "NONE");
                } catch (JSONException e) {
                    Log.e("MainActivity", e.getMessage());
                }
                Log.d("MainActivity", json.toString());
                //TODO: Use string resource
                post(getString(R.string.url_config), json, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        showSnackback("Failed to send POST...");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        showSnackback("Sent POST successfully!");
                    }
                });
            }

        });
    }

    //TODO: Fix show snackbar
    private void showSnackback(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    OkHttpClient client = new OkHttpClient();
    private Call post(String url, JSONObject json, Callback callback) {
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
        }

        return super.onOptionsItemSelected(item);
    }
}
