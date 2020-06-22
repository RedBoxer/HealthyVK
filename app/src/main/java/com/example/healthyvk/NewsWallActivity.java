package com.example.healthyvk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.adapters.NewsWallAdapter;
import com.perm.kate.api.Api;
import com.perm.kate.api.Group;
import com.perm.kate.api.Newsfeed;

import java.util.ArrayList;
import java.util.Collection;

public class NewsWallActivity extends AppCompatActivity {

    RecyclerView newswall;
    NewsWallAdapter nwAdapter;
    Newsfeed nfeed;
    Api api;
    ArrayList<Group> groups = new ArrayList<>();
    TextView err;
    ArrayList<Long> groupIds = new ArrayList<>();
    int newsCount = 20;
    boolean reqInProgress = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_wall);

        Intent intent = getIntent();
        api = new Api(intent.getStringExtra("token"), Constants.API_ID);
       // new AsyncRequest().execute();

        initRecyclerView();
    }

   /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.newswall_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.news10:
                newsCount = 10;
                new AsyncRequest().execute();
                return true;
            case R.id.news25:
                newsCount = 25;
                new AsyncRequest().execute();
                return true;
            case R.id.news50:
                 newsCount = 50;
                new AsyncRequest().execute();
                return true;
            case R.id.news75:
                 newsCount = 75;
                new AsyncRequest().execute();
                 return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    private void initRecyclerView() {
        newswall = findViewById(R.id.NewsWall);
        newswall.setLayoutManager(new LinearLayoutManager(this));
        nwAdapter = new NewsWallAdapter();
        newswall.setAdapter(nwAdapter);
    }

    private void loadNews(){
        if(groups.size() == 0){
            //err.setText("Groups are empty!" + groupIds.size());
        }else {
            nwAdapter.setItems(nfeed.items, groups);
        }
    }

    public void onRefreshClick(View view){
        if(!reqInProgress){
            reqInProgress = true;
            new AsyncRequest().execute();
        }
    }

    class AsyncRequest extends AsyncTask<Integer, Double, String> {
        StringBuilder str = new StringBuilder();
        @Override
        protected String doInBackground(Integer ...inte){
            try {
                nfeed = api.getNews(newsCount, "post");
                groupIds.clear();
                for(int i = 0; i < nfeed.items.size(); i++){
                    if (nfeed.items.get(i).source_id >= 0) {
                        groupIds.add(nfeed.items.get(i).source_id);
                    } else {
                        groupIds.add(-1 * nfeed.items.get(i).source_id);
                    }
                }
                groups.clear();
                groups.addAll(api.getGroups(groupIds, null, "description"));
            }catch(Exception e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            nwAdapter.clearItems();
            loadNews();
            reqInProgress = false;
        }
    }
}
