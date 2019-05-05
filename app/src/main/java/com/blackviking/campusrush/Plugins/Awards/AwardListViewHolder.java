package com.blackviking.campusrush.Plugins.Awards;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;

public class AwardListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public TextView awardName, awardFaculty, awardDepartment;

    public AwardListViewHolder(@NonNull View itemView) {
        super(itemView);

        awardName = (TextView)itemView.findViewById(R.id.awardName);
        awardFaculty = (TextView)itemView.findViewById(R.id.awardFaculty);
        awardDepartment = (TextView)itemView.findViewById(R.id.awardDepartment);

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
