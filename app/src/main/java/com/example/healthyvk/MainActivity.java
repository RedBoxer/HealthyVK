package com.example.healthyvk;


import androidx.appcompat.app.AppCompatActivity;

import com.perm.kate.api.Api;
import com.perm.kate.api.User;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_LOGIN=1;

    Button authorizeButton;
    Button logoutButton;
    Button postButton;
    Button newsButton;
    Button friendsButton;
    TextView name;
    ImageView pic;
    EditText messageEditText;
    Drawable picIm;
    Account account = new Account();
    Api api;
    User user = new User();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();

        account.restore(this);

        if(account.access_token!=null) {
            api = new Api(account.access_token, Constants.API_ID);
            new loadProfile().execute();
        }

        showButtons();
    }

    private void setupUI() {
        authorizeButton=(Button)findViewById(R.id.authorize);
        logoutButton=(Button)findViewById(R.id.logout);
        postButton=(Button)findViewById(R.id.post);
        newsButton = findViewById(R.id.News);
        friendsButton = findViewById(R.id.FriendssBtn);
        messageEditText=(EditText)findViewById(R.id.message);
        name = findViewById(R.id.prName);
        pic = findViewById(R.id.prPic);
        authorizeButton.setOnClickListener(authorizeClick);
        logoutButton.setOnClickListener(logoutClick);
        postButton.setOnClickListener(postClick);
    }

    private View.OnClickListener authorizeClick=new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            startLoginActivity();
        }
    };

    private View.OnClickListener logoutClick=new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            logOut();
        }
    };

    private View.OnClickListener postClick=new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            postToWall();
        }
    };

    private void startLoginActivity() {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);

        startActivityForResult(intent, REQUEST_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                //авторизовались успешно
                account.access_token=data.getStringExtra("token");
                account.user_id=data.getLongExtra("user_id", 0);
                account.save(MainActivity.this);
                api=new Api(account.access_token, Constants.API_ID);
                new loadProfile().execute();
                showButtons();

            }
        }
    }

    class loadProfile extends AsyncTask<Integer, Integer, Integer>{
        @Override
        protected Integer doInBackground(Integer...integers){
            try {
                user = api.getProfiles(null, String.valueOf(account.user_id), "photo_200", null, null, null).get(0);
                picIm = LoadImageFromURL(user.photo_200);
            }catch(Exception e){

            }
            return 0;
        }
        @Override
        protected void onPostExecute(Integer i){
            super.onPostExecute(i);
            pic.setImageDrawable(picIm);
            name.setText(user.first_name + " " + user.last_name);
        }
    }

    private void postToWall() {
        //Общение с сервером в отдельном потоке чтобы не блокировать UI поток
        new Thread(){
            @Override
            public void run(){
                try {
                    String text=messageEditText.getText().toString();
                    api.createWallPost(account.user_id, text, null, null, false, false, false, null, null, null, 0L, null, null);
                    //Показать сообщение в UI потоке
                    runOnUiThread(successRunnable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    Runnable successRunnable=new Runnable(){
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), "Запись успешно добавлена", Toast.LENGTH_LONG).show();
        }
    };

    private void logOut() {
        api=null;
        account.access_token=null;
        account.user_id=0;
        account.save(MainActivity.this);
        showButtons();
    }

    void showButtons(){
        if(api!=null){
            authorizeButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
            postButton.setVisibility(View.VISIBLE);
            messageEditText.setVisibility(View.VISIBLE);
            newsButton.setVisibility(View.VISIBLE);
            friendsButton.setVisibility(View.VISIBLE);
            name.setVisibility(View.VISIBLE);
            pic.setVisibility(View.VISIBLE);
        }else{
            authorizeButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
            postButton.setVisibility(View.GONE);
            messageEditText.setVisibility(View.GONE);
            newsButton.setVisibility(View.GONE);
            friendsButton.setVisibility(View.GONE);
            name.setVisibility(View.GONE);
            pic.setVisibility(View.GONE);
        }
    }

    public void onNewsClick(View view){
        Intent intent = new Intent();
        intent.setClass(this, NewsWallActivity.class);
        intent.putExtra("token", account.access_token);
        startActivity(intent);
    }

    public void onFriendsClick(View view){
        Intent intent = new Intent();
        intent.setClass(this, FriendsActivity.class);
        intent.putExtra("token", account.access_token);
        intent.putExtra("uid", user.uid);
        startActivity(intent);
    }

    public void onTrackingClick(View view){
        Intent intent = new Intent();
        intent.setClass(this, TrackingActivity.class);
        intent.putExtra("token", account.access_token);
        intent.putExtra("uid", user.uid);
        startActivity(intent);
    }

    Drawable LoadImageFromURL(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }
}
