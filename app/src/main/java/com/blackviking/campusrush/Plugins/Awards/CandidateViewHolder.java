package com.blackviking.campusrush.Plugins.Awards;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;

public class CandidateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public ImageView candidateImage;
    public TextView candidateName;

    public CandidateViewHolder(@NonNull View itemView) {
        super(itemView);

        candidateImage = (ImageView)itemView.findViewById(R.id.candidateImage);
        candidateName = (TextView)itemView.findViewById(R.id.candidateName);

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
