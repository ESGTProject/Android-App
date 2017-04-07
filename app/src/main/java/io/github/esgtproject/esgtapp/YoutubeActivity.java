package io.github.esgtproject.esgtapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class YoutubeActivity extends AppCompatActivity {

    final String TAG = "YouTubeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        Bundle extras = getIntent().getExtras();
        String url = extras.getString(Intent.EXTRA_TEXT);
        Toast.makeText(this, "Shared: " + url, Toast.LENGTH_SHORT).show();
        pushToFireBase(url);
        finish();
    }

    private void pushToFireBase(String url) {
        // Initialize Firebase database reference
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d(TAG, "User:" + uid);
            DatabaseReference configRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("config");
            configRef.child("yt_url").setValue(url);
        }

    }
}
