package com.blackviking.campusrush.CampusBusiness;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;

public class AdViewHolder extends RecyclerView.ViewHolder {

    public ImageView adImage;
    public TextView adText;
    public Button adPromote;

    public AdViewHolder(@NonNull View itemView) {
        super(itemView);

        adImage = (ImageView)itemView.findViewById(R.id.adListImage);
        adText = (TextView)itemView.findViewById(R.id.adListText);
        adPromote = (Button)itemView.findViewById(R.id.adListPromote);

    }

}
