package com.blackviking.campusrush.Messaging;

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

public class MessagingViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout yourMsgLayout, otherMsgLayout;
    public ImageView yourMsgImage, otherMsgImage;
    public TextView myText, myTextTimeStamp, otherText, otherTextTimeStamp;

    public LinearLayout myQuoteLayout, otherQuoteLayout;
    public TextView myQuoter, myQuoteText, otherQuoter, otherQuoteText;
    public ImageView myQuoteImage, otherQuoteImage;

    public MessagingViewHolder(View itemView) {
        super(itemView);

        yourMsgLayout = (LinearLayout)itemView.findViewById(R.id.yourMessageTextLayout);
        otherMsgLayout = (LinearLayout)itemView.findViewById(R.id.otherMessageTextLayout);
        yourMsgImage = (ImageView)itemView.findViewById(R.id.yourMessageImage);
        otherMsgImage = (ImageView)itemView.findViewById(R.id.otherMessageImage);
        myText = (TextView)itemView.findViewById(R.id.yourMessageText);
        myTextTimeStamp = (TextView)itemView.findViewById(R.id.yourMessageTimeStamp);
        otherText = (TextView)itemView.findViewById(R.id.otherMessageText);
        otherTextTimeStamp = (TextView)itemView.findViewById(R.id.otherMessageTimeStamp);

        myQuoteLayout = (LinearLayout)itemView.findViewById(R.id.myQuoteLayout);
        otherQuoteLayout = (LinearLayout)itemView.findViewById(R.id.otherQuoteLayout);
        myQuoter = (TextView)itemView.findViewById(R.id.myQuoter);
        myQuoteText = (TextView)itemView.findViewById(R.id.myQuoteText);
        otherQuoter = (TextView)itemView.findViewById(R.id.otherQuoter);
        otherQuoteText = (TextView)itemView.findViewById(R.id.otherQuoteText);
        myQuoteImage = (ImageView)itemView.findViewById(R.id.myQuoteImage);
        otherQuoteImage = (ImageView)itemView.findViewById(R.id.otherQuoteImage);

    }

}
