package com.blackviking.campusrush.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.R;

/**
 * Created by Scarecrow on 1/19/2019.
 */

public class RantViewHolder extends RecyclerView.ViewHolder {

    public ImageView rantImage;
    public TextView rantUsername, rantText, rantTimestamp;

    public RantViewHolder(View itemView) {
        super(itemView);

        rantImage = (ImageView)itemView.findViewById(R.id.rantImage);
        rantText = (TextView)itemView.findViewById(R.id.rantText);
        rantTimestamp = (TextView)itemView.findViewById(R.id.rantTimeStamp);
        rantUsername = (TextView)itemView.findViewById(R.id.rantUsername);

    }

}
