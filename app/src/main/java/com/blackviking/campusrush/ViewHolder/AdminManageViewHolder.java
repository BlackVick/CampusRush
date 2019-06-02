package com.blackviking.campusrush.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;

public class AdminManageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public ImageView adminItemImage;
    public TextView adminItemTitle, adminItemDescription;

    public AdminManageViewHolder(@NonNull View itemView) {
        super(itemView);

        adminItemImage = (ImageView)itemView.findViewById(R.id.adminItemImage);
        adminItemTitle = (TextView)itemView.findViewById(R.id.adminItemTitle);
        adminItemDescription = (TextView)itemView.findViewById(R.id.adminItemDescription);

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
