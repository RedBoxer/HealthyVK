package com.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.healthyvk.R;
import com.perm.kate.api.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>  {
    ArrayList<User> items = new ArrayList<>();

    public void setItems(ArrayList<User> items) {
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void clearItems() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public FriendsAdapter.FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_item, parent, false);
        return new FriendsAdapter.FriendsViewHolder(view);
    }


    @Override
    public void onBindViewHolder(FriendsAdapter.FriendsViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class FriendsViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView online;
        long id_text;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.friendName);
            online = itemView.findViewById(R.id.onlineInd);
        }

        public void bind(User item) {
            name.setText(item.first_name + " " + item.last_name);
            id_text = item.uid;
            if (item.online){
                online.setText("В сети");
            }else{
                Date temp = new Date(item.last_seen * 1000L);
                SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy,HH:mm");
                f.setTimeZone(TimeZone.getDefault());
                online.setText("последний онлайн: " +  f.format(temp));
            }
        }
    }
}
