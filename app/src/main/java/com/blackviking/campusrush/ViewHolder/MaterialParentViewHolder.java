package com.blackviking.campusrush.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;

public class MaterialParentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView name;
    private ItemClickListener itemClickListener;

    public MaterialParentViewHolder(@NonNull View itemView) {
        super(itemView);

        name = (TextView)itemView.findViewById(R.id.courseTitle);

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
