package com.support;

import android.os.AsyncTask;

import com.perm.kate.api.Api;
import com.perm.kate.api.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendsAnalyzer {
    ArrayList<Long> rootFriends = new ArrayList<>();
    ArrayList<ArrayList<Long>> friendsFriends = new ArrayList<>();
    Api api;
    HashMap<Long, User> usersInAnalyze = new HashMap<>();
    long rootId;
    int ready = 0;
    ArrayList<User> recUsers = new ArrayList<>();

    public FriendsAnalyzer(ArrayList<User> rootFriends, Api api, long rootId){
        for(int i = 0; i < rootFriends.size(); i++) {
            this.rootFriends.add(rootFriends.get(i).uid);
        }
        this.api = api;
        this.rootId = rootId;
    }

    private void prepareForRecommendation(){
        friendsFriends.clear();
        ready = 1;
        Thread prepThread = new Thread(getFriends);
        prepThread.start();
        try {
            prepThread.join();
        }catch (Exception e){};
        ArrayList<Long> recFriends = new ArrayList<>();
        HashMap<Long, Integer> conCount  = new HashMap<>();
        for(int i = 0; i < 20 && i < friendsFriends.size() ; i++){
            for(int o = 0; o < 20 && o < friendsFriends.get(i).size(); o++){
                properAddToMap(conCount, friendsFriends.get(i).get(o));
            }
        }
        removeBulkFriendsFromMap(conCount);
        recFriends.addAll(sortedFriendsFromMap(conCount));
        for(int i = 0; i < recFriends.size(); i++) {
            recUsers.add(usersInAnalyze.get(recFriends.get(i)));
        }
    }

    public ArrayList<User> getRecommendedFriends(){
        prepareForRecommendation();
        return recUsers;
    }

    private void properAddToMap(HashMap<Long, Integer> map, long uid){
        if(map.containsKey(uid)){
            map.put(uid, map.get(uid) + 1);
        }else{
            map.put(uid, 1);
        }
    }

    private void removeBulkFriendsFromMap(HashMap<Long, Integer> map){
        for(int i = 0; i < rootFriends.size(); i++){
            if(map.containsKey(rootFriends.get(i))){
                map.remove(rootFriends.get(i));
            }
        }
        if(map.containsKey(rootId)){
            map.remove(rootId);
        }
        /*for (Map.Entry<Long, Integer> entry : map.entrySet()) {
            if(entry.getValue() == 1){
                map.remove(entry.getKey());
            }
        }*/
    }

    private ArrayList<Long> sortedFriendsFromMap(HashMap<Long, Integer> map){
        ArrayList<Long> result = new ArrayList<>();
        Map.Entry<Long, Integer> entry;
        long maxId;
        int maxValue;
        for(int i = 0; i < 20; i++) {
            entry = map.entrySet().iterator().next();
            maxId = entry.getKey();
            maxValue = entry.getValue();
            for (Map.Entry<Long, Integer> entrys : map.entrySet()) {
                if(maxValue < entrys.getValue()){
                    maxId = entrys.getKey();
                    maxValue = entrys.getValue();
                }
            }
            result.add(maxId);
            map.remove(maxId);
            if(maxValue == 1){
                break;
            }
        }
        return result;
    }

    Runnable getFriends = new Runnable() {
        @Override
        public void run() {
            ArrayList<User> temp = new ArrayList<>();
            long curId;
            try{
                for(int i = 0; i < 20 && i < rootFriends.size(); i++) {

                    temp.addAll(api.getFriends(rootFriends.get(i), "online, last_seen", null, null, null));
                    friendsFriends.add(new ArrayList<Long>());
                    for(int o = 0; o < 20 && o < temp.size(); o++) {
                        curId = temp.get(o).uid;
                        friendsFriends.get(i).add(curId);
                        if(!usersInAnalyze.containsKey(curId)) {
                            usersInAnalyze.put(curId, temp.get(o));
                        }
                    }
                    temp.clear();
                    Thread.sleep(200);
                }
            }catch(Exception e){}
        }
    };
}
