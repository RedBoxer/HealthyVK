package com.example.healthyvk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.adapters.FriendsAdapter;
import com.adapters.NewsWallAdapter;
import com.perm.kate.api.Api;
import com.perm.kate.api.User;
import com.support.FriendsAnalyzer;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    Api api;
    long uid;
    ArrayList<User> friends = new ArrayList<>();
    ArrayList<User> recFriends =new ArrayList<>();
    RecyclerView friendsWall;
    FriendsAdapter frAdapter;
    Button chgFrList;
    FriendsAnalyzer frAnal;
    boolean bRecFriends = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        chgFrList = findViewById(R.id.chgFrList);
        Intent intent = this.getIntent();
        api = new Api(intent.getStringExtra("token"), Constants.API_ID);
        uid = intent.getLongExtra("uid", 0);
        new getFriends().execute();

        initRecyclerView();
    }

    class getFriends extends AsyncTask<Integer, Integer, Integer>{
        @Override
        protected Integer doInBackground(Integer...integers){
            try{
                friends.addAll(api.getFriends(uid, "online, last_seen", null, null, null));
            }catch(Exception e){}
            return 0;
        }

        @Override
        protected void onPostExecute(Integer s){
            loadItems(friends);
            frAnal = new FriendsAnalyzer(friends, api, uid);
        }
    }

    private void initRecyclerView() {
        friendsWall = findViewById(R.id.friendsList);
        friendsWall.setLayoutManager(new LinearLayoutManager(this));
        frAdapter = new FriendsAdapter();
        friendsWall.setAdapter(frAdapter);
    }

    private void loadItems(ArrayList<User> friends){
        if(friends.size() == 0){
        }else {
            frAdapter.setItems(friends);
        }
    }

    public void onChgFriendsList(View view){
        frAdapter.clearItems();
        if(bRecFriends){
            bRecFriends = false;
            chgFrList.setText("Рекомендуемые друзья");
            loadItems(friends);
        } else {
            bRecFriends = true;
            chgFrList.setText("Мои друзья");
            if (recFriends.size() == 0) {
                Thread load = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        recFriends.addAll(frAnal.getRecommendedFriends());
                    }
                });
                load.start();
                try {
                    load.join();
                }catch(Exception e){}
                loadItems(recFriends);
            } else {
                loadItems(recFriends);
            }
        }
    }
}
