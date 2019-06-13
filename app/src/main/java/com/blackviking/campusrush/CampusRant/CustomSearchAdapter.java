package com.blackviking.campusrush.CampusRant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;

import java.util.ArrayList;

public class CustomSearchAdapter extends RecyclerView.Adapter<CustomSearchAdapter.CustomSearchAdapterViewHolder> {

    public Context c;
    public ArrayList<RantTopicModel> arrayList;
    public Activity activity;


    public CustomSearchAdapter(Context c, ArrayList<RantTopicModel> arrayList, Activity activity){
        this.c = c;
        this.arrayList = arrayList;
        this.activity = activity;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public CustomSearchAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.rant_topic_item, viewGroup, false);

        return new CustomSearchAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomSearchAdapterViewHolder customSearchAdapterViewHolder, int i) {

        final RantTopicModel rantTopicItem = arrayList.get(i);

        customSearchAdapterViewHolder.topicName.setText("#"+rantTopicItem.getName());

        customSearchAdapterViewHolder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                Intent rantRoomIntent = new Intent(c, RantRoom.class);
                rantRoomIntent.putExtra("RantTopic", rantTopicItem.getName());
                c.startActivity(rantRoomIntent);
                activity.overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


    }

    public class CustomSearchAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ItemClickListener itemClickListener;
        public TextView topicName;

        public CustomSearchAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            topicName = (TextView)itemView.findViewById(R.id.topicName);

            itemView.setOnClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(), false);
        }

    }


}
