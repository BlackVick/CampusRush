package com.blackviking.campusrush.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationViewHolder extends RecyclerView.ViewHolder{

    public CircleImageView userImage;
    public TextView title, details, comment, time, username;
    public ImageView type, status;

    public NotificationViewHolder(@NonNull View itemView) {
        super(itemView);

        userImage = (CircleImageView)itemView.findViewById(R.id.notificationImage);
        title = (TextView)itemView.findViewById(R.id.notificationTitle);
        details = (TextView)itemView.findViewById(R.id.notificationDescription);
        comment = (TextView)itemView.findViewById(R.id.notificationComment);
        username = (TextView)itemView.findViewById(R.id.notificationUser);
        time = (TextView)itemView.findViewById(R.id.notificationTimeStamp);
        type = (ImageView) itemView.findViewById(R.id.notificationType);
        status = (ImageView) itemView.findViewById(R.id.notificationStatus);

    }

}
