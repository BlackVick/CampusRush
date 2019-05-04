package com.blackviking.campusrush.Plugins.GamersHub;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;

public class TrendingGamesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public ImageView gameImage;
    public TextView gameName, gameRating;

    public TrendingGamesViewHolder(@NonNull View itemView) {
        super(itemView);

        gameImage = (ImageView)itemView.findViewById(R.id.gameImage);
        gameName = (TextView)itemView.findViewById(R.id.gameName);
        gameRating = (TextView)itemView.findViewById(R.id.gameRating);

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
