package com.example.healthyvk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.TextView;

import com.adapters.NewsWallAdapter;
import com.adapters.TrackingAdapter;
import com.perm.kate.api.Api;
import com.perm.kate.api.KException;
import com.perm.kate.api.User;
import com.support.OnlineTracker;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TrackingActivity extends AppCompatActivity {
    Api api;
    Long uid;

    EditText editText;
    OnlineTracker mService;
    boolean mBound = false;
    Intent intent;
    ArrayList<Long> ids = new ArrayList<>();
    ArrayList<User> users = new ArrayList<>();
    ArrayList<String> times = new ArrayList<>();
    RecyclerView trackwall;
    TrackingAdapter trAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        intent = this.getIntent();
        String token = intent.getStringExtra("token");
        api = new Api(token , Constants.API_ID);
        uid = intent.getLongExtra("uid", 0);
        intent = new Intent(getBaseContext(), OnlineTracker.class);
        intent.putExtra("token", token);
        ids.add(uid);
        editText = findViewById(R.id.editTextNumber);
        initRecyclerView();
        Thread load = new Thread(loadItems);
        load.start();
    }

    private void initRecyclerView() {
        trackwall = findViewById(R.id.trackWall);
        trackwall.setLayoutManager(new LinearLayoutManager(this));
        trAdapter = new TrackingAdapter();
        trackwall.setAdapter(trAdapter);
    }

    public void onAddId(View view){
        ids.add(Long.valueOf(editText.getText().toString()));
    }

    Runnable loadItems = new Runnable() {
        @Override
        public void run() {
            try {
                users = api.getProfiles(ids, null, "online", null, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            trAdapter.clearItems();
            trAdapter.setItems(users, times);
        }
    };

    public void onClickNot(View view){
        startService(intent);
    }

    public void onClick(View view){
        stopService(intent);
    }
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            OnlineTracker.LocalBinder binder = (OnlineTracker.LocalBinder) service;
            mService = binder.getService();
            times.clear();
            for(int i = 0; i < users.size(); i++){
                times.add(mService.getNextOnlineForUser(ids.get(i)));
            }
            mService.setIds(ids);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

}