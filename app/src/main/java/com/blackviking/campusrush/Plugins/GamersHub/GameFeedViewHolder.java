package com.blackviking.campusrush.Plugins.GamersHub;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;

public class GameFeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public ImageView gameFeedImage;
    public TextView gameFeedTitle, gameFeedTimeStamp;

    public GameFeedViewHolder(@NonNull View itemView) {
        super(itemView);

        gameFeedImage = (ImageView)itemView.findViewById(R.id.gameFeedImage);
        gameFeedTitle = (TextView) itemView.findViewById(R.id.gameFeedTitle);
        gameFeedTimeStamp = (TextView) itemView.findViewById(R.id.gameFeedTimeStamp);

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
