package com.blackviking.campusrush.Plugins.SkitCenter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;
import com.bumptech.glide.RequestManager;

public class SkitViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView skitTitle, skitOwner, skitLike;
    public ImageView likeButton, thumbnail;
    private ItemClickListener itemClickListener;

    public SkitViewHolder(@NonNull View itemView) {
        super(itemView);

        skitTitle = (TextView)itemView.findViewById(R.id.skitTitle);
        skitOwner = (TextView)itemView.findViewById(R.id.skitOwner);
        skitLike = (TextView)itemView.findViewById(R.id.likeCount);
        likeButton = (ImageView)itemView.findViewById(R.id.likeBtn);
        thumbnail = (ImageView)itemView.findViewById(R.id.skitThumbnail);

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
