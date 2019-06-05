package com.blackviking.campusrush.Messaging;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public CircleImageView userImage;
    public TextView username, unreadCount;

    public MessagesViewHolder(@NonNull View itemView) {
        super(itemView);

        userImage = (CircleImageView)itemView.findViewById(R.id.msgListImage);
        username = (TextView)itemView.findViewById(R.id.msgListUsername);
        unreadCount = (TextView)itemView.findViewById(R.id.msgUnreadCount);

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
