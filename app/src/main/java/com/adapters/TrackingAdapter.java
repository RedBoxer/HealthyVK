package com.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.healthyvk.R;
import com.perm.kate.api.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class TrackingAdapter extends RecyclerView.Adapter<TrackingAdapter.FriendsViewHolder>  {
    ArrayList<User> items = new ArrayList<>();
    ArrayList<String> times = new ArrayList<>();

    public void setItems(ArrayList<User> items, ArrayList<String> times) {
        this.items.addAll(items);
        this.times.addAll(times);
        notifyDataSetChanged();
    }

    public void clearItems() {
        items.clear();
        times.clear();
        notifyDataSetChanged();
    }

    @Override
    public TrackingAdapter.FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_item, parent, false);
        return new TrackingAdapter.FriendsViewHolder(view);
    }


    @Override
    public void onBindViewHolder(TrackingAdapter.FriendsViewHolder holder, int position) {
        holder.bind(items.get(position), position);
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

        public void bind(User item, int pos) {
            name.setText(item.first_name + " " + item.last_name);
            id_text = item.uid;
            if (item.online){
                online.setText("В сети");
            }else{
                online.setText("Ожидаемый онлайн: " + times.get(pos));
            }
        }
    }
}
