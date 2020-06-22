package com.adapters;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.healthyvk.R;
import com.perm.kate.api.Api;
import com.perm.kate.api.Group;
import com.perm.kate.api.NewsItem;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public class NewsWallAdapter extends RecyclerView.Adapter<NewsWallAdapter.NewsViewHolder> {
    private ArrayList<NewsItem> items = new ArrayList<>();
    private ArrayList<Group> groups = new ArrayList<>();

    public void setItems(ArrayList<NewsItem> items, ArrayList<Group> groups) {
        this.items.addAll(items);
        this.groups.addAll(groups);
        notifyDataSetChanged();
    }

    public void clearItems() {
        items.clear();
        groups.clear();
        notifyDataSetChanged();
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_item, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        int pos = position;
        if (pos >= items.size()){
            pos--;
        }
        holder.bind(items.get(pos), groups.get(pos));
        final NewsViewHolder hol = holder;
        holder.nextPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hol.setNextPic();
            }
        });
        holder.prevPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               hol.setPrevPic();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {
        private ImageView newsPic;
        private ImageView grPic;
        private TextView grName;
        private TextView newsText;
        private ArrayList<Drawable> tempNPic = new ArrayList<>();
        private Drawable tempGPic;
        private Button like;
        private Button comments;
        private Button prevPic;
        private Button nextPic;
        private TextView picNumb;
        NewsItem itemH;
        Group groupH;
        int picNum = 0;
        int noPhoto = 0;
        
        public NewsViewHolder(View itemView) {
            super(itemView);
            grPic = itemView.findViewById(R.id.groupPic);
            grName = itemView.findViewById(R.id.groupName);
            newsPic = itemView.findViewById(R.id.newsPic);
            newsText = itemView.findViewById(R.id.newsText);
            like = itemView.findViewById(R.id.likes);
            comments = itemView.findViewById(R.id.comments);
            prevPic = itemView.findViewById(R.id.prevPic);
            nextPic = itemView.findViewById(R.id.nextPic);
            picNumb = itemView.findViewById(R.id.picNum);
        }

        public void bind(NewsItem item, Group group) {
            itemH = item;
            groupH = group;

            new AsyncRequest().execute();
            like.setText("Лайки: " + item.like_count);
            comments.setText(item.comment_count + " коммент.");
            newsText.setText(item.text);
            grName.setText(group.name);
            if(item.attachments != null && item.attachments.size() != 0) {
                picNumb.setText(item.attachments.size() + item.attachments.get(0).type);
            }

        }

        class AsyncRequest extends AsyncTask<Integer, Integer, Integer>{
            @Override
            protected Integer doInBackground(Integer ...inte){
                try {
                    noPhoto = 0;
                    if(itemH.attachments.size() != 0) {
                        tempNPic.clear();
                        for(int i = 0; i < itemH.attachments.size(); i++) {
                            if(itemH.attachments.get(i).photo != null) {
                                tempNPic.add(LoadImageFromURL(itemH.attachments.get(i).photo.src_big));
                            }
                        }
                    }
                    if(tempNPic.size() == 0){
                        noPhoto = 1;
                    }
                    tempGPic = LoadImageFromURL(groupH.photo);
                }catch(Exception e){

                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer s) {
                super.onPostExecute(s);
                picNum = 0;
                if(noPhoto == 0) {
                    newsPic.setImageDrawable(tempNPic.get(picNum));
                }
                grPic.setImageDrawable(tempGPic);
            }
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
        void setNextPic(){
            picNum++;
            if(picNum < tempNPic.size()) {
                newsPic.setImageDrawable(tempNPic.get(picNum));
            }else{
                picNum--;
            }
        }

        void setPrevPic(){
            picNum--;
            if(picNum >= 0) {
                newsPic.setImageDrawable(tempNPic.get(picNum));
            }else{
                picNum++;
            }
        }
    }
}

