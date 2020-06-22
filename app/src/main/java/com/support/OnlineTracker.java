package com.support;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.example.healthyvk.Constants;
import com.perm.kate.api.Api;
import com.perm.kate.api.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class OnlineTracker extends Service {
   private final IBinder mBinder = new LocalBinder();
   ArrayList<Long> trackPeople = new ArrayList<>();
   HashMap<Long, ArrayList<Integer>> timetable;
    Thread check;
    boolean idsSet = true;
    Api api;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        check = new Thread(stuff);
        api = new Api(intent.getStringExtra("token"), Constants.API_ID);
        check.start();
        return Service.START_STICKY;
    }

    public class LocalBinder extends Binder {
        public OnlineTracker getService() {
            // Return this instance of LocalService so clients can call public methods
            return OnlineTracker.this;
        }
    }

    public void setIds(ArrayList<Long> ids){
        idsSet = false;
        trackPeople = ids;
        idsSet = true;
        check.start();
    }

    public String getNextOnlineForUser(Long id){
        ArrayList<Integer> temp = timetable.get(id);
        int maxValue = 0;
        int maxPos = 0;
        for(int i = countPosInTable(); i < temp.size(); i++){
            if(temp.get(i) > maxValue){
                maxValue = temp.get(i);
                maxPos = i;
            }
        }
        String result = "NO:";
        if(maxPos % 2 == 0){
            result += maxPos / 2 + ":00";
        }else{
            result += maxPos / 2 + ":30";
        }
        return result;
    }

    private int countPosInTable(){
        int posInTable = ((int) curT.charAt(curT.length() - 4) +
                (int) curT.charAt(curT.length() - 5) * 10) * 2;
        if((int) curT.charAt(curT.length() - 2) > 2){
            posInTable++;
        }
        return posInTable;
    }

    private void admitUserActivity(User user){
        int posInTable = countPosInTable();
        if(timetable.get(user.uid).get(posInTable) != 0){
            timetable.get(user.uid).add(timetable.get(user.uid).get(posInTable) + 1);
        }else{
            timetable.get(user.uid).add(posInTable, 1);
        }
    }

    String curT;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    Runnable stuff = new Runnable() {
        @Override
        public void run() {
            ArrayList<User> temp;
            while(idsSet){
                try {
                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy,HH:mm");
                    f.setTimeZone(TimeZone.getDefault());
                    curT = f.format(currentTime);
                    if((int) curT.charAt(curT.length() - 1) % 5 == 0) {
                        temp = api.getProfiles(trackPeople, null, "online"
                                        , null, null, null);
                        for(int i = 0; i < temp.size(); i++){
                            if(temp.get(i).online){
                                admitUserActivity(temp.get(i));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {

    }
}
