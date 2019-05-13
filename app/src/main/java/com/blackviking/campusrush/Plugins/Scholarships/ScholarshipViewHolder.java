package com.blackviking.campusrush.Plugins.Scholarships;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;

public class ScholarshipViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    TextView title, location, deadline;

    public ScholarshipViewHolder(@NonNull View itemView) {
        super(itemView);

        title = (TextView)itemView.findViewById(R.id.scholarshipTitle);
        location = (TextView)itemView.findViewById(R.id.scholarshipLocation);
        deadline = (TextView)itemView.findViewById(R.id.scholarshipDeadline);

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
