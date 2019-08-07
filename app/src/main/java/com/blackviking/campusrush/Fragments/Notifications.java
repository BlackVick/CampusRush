package com.blackviking.campusrush.Fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Common.GetTimeAgo;
import com.blackviking.campusrush.FeedDetails;
import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.Model.FeedModel;
import com.blackviking.campusrush.Model.NotificationModel;
import com.blackviking.campusrush.Notification.APIService;
import com.blackviking.campusrush.Profile.OtherUserProfile;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.ViewHolder.FeedViewHolder;
import com.blackviking.campusrush.ViewHolder.NotificationViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class Notifications extends Fragment {

    private RecyclerView notificationRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<NotificationModel, NotificationViewHolder> adapter;
    private String currentUid;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference notificationRef, userRef, feedRef;
    private FloatingActionButton deleteAllNotification;

    public Notifications() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notifications, container, false);

        /*---   PAPER DB   ---*/
        Paper.init(getContext());


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();

        userRef = db.getReference("Users");
        notificationRef = db.getReference("Notifications");
        feedRef = db.getReference("Feed");


        /*---   WIDGETS   ---*/
        notificationRecycler = (RecyclerView)v.findViewById(R.id.notificationRecycler);
        deleteAllNotification = (FloatingActionButton)v.findViewById(R.id.deleteAllNotification);

        deleteAllNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getContext()))
                    showConfirmDialog();
            }
        });

        clearNotifications();

        loadNotifications();

        return v;
    }

    private void clearNotifications() {

        notificationRef.child(currentUid)
                .orderByChild("status")
                .equalTo("Unread")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snap : dataSnapshot.getChildren()){

                            notificationRef.child(currentUid)
                                    .child(snap.getKey())
                                    .child("status")
                                    .setValue("Read");

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void showConfirmDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Attention !")
                .setIcon(R.drawable.ic_delete_feed)
                .setMessage("Are you sure you want to clear all notifications?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        notificationRef.child(currentUid)
                                .removeValue().addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dialog.dismiss();
                                    }
                                }
                        );

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.show();
    }

    private void loadNotifications() {

        notificationRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        notificationRecycler.setLayoutManager(layoutManager);

        final RelativeLayout theNavLayout = (RelativeLayout)getActivity().findViewById(R.id.navLayout);


        notificationRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy < 0 && theNavLayout.getVisibility() == View.GONE) {

                    theNavLayout.setVisibility(View.VISIBLE);
                    deleteAllNotification.show();
                }
                else if(dy > 0 && theNavLayout.getVisibility() == View.VISIBLE) {
                    theNavLayout.setVisibility(View.GONE);
                    deleteAllNotification.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                /*if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    theNavLayout.setVisibility(View.VISIBLE);*/
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        adapter = new FirebaseRecyclerAdapter<NotificationModel, NotificationViewHolder>(
                NotificationModel.class,
                R.layout.notification_item,
                NotificationViewHolder.class,
                notificationRef.child(currentUid).limitToLast(20)
        ) {
            @Override
            protected void populateViewHolder(final NotificationViewHolder viewHolder, final NotificationModel model, int position) {


                /*---   GET TIME AGO ALGORITHM   ---*/
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = model.getTimestamp();
                final String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getActivity());

                if (model.getTitle() != null) {

                    if (model.getTitle().equalsIgnoreCase("Account")) {

                        viewHolder.userImage.setImageResource(R.drawable.campus_rush_colored_logo);
                        viewHolder.title.setText(model.getTitle());
                        viewHolder.comment.setVisibility(View.VISIBLE);
                        viewHolder.comment.setText(model.getDetails());
                        viewHolder.username.setVisibility(View.GONE);
                        viewHolder.details.setVisibility(View.GONE);
                        viewHolder.time.setText(lastSeenTime);
                        viewHolder.type.setImageResource(R.drawable.ic_campus_business);


                        if (model.getStatus().equalsIgnoreCase("Unread")) {
                            viewHolder.status.setImageResource(R.drawable.ic_notification_unread);
                        } else {
                            viewHolder.status.setImageResource(R.drawable.ic_notification_read);
                        }


                    } else {

                        /*---   USER   ---*/
                        userRef.child(model.getUser()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot != null) {
                                    String username = dataSnapshot.child("username").getValue().toString();
                                    String image = dataSnapshot.child("profilePictureThumb").getValue().toString();

                                    viewHolder.username.setText("@" + username);

                                    if (!image.equalsIgnoreCase("")) {

                                        Picasso.with(getContext())
                                                .load(image)
                                                .placeholder(R.drawable.profile)
                                                .into(viewHolder.userImage);


                                    }

                                    viewHolder.userImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent posterProfile = new Intent(getContext(), OtherUserProfile.class);
                                            posterProfile.putExtra("UserId", model.getUser());
                                            startActivity(posterProfile);
                                            getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                                        }
                                    });

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                        /*---   NOTIFICATION STATE   ---*/
                        if (model.getStatus().equalsIgnoreCase("Unread")) {
                            viewHolder.status.setImageResource(R.drawable.ic_notification_unread);
                        } else {
                            viewHolder.status.setImageResource(R.drawable.ic_notification_read);
                        }

                        if (model.getType().equalsIgnoreCase("Comment")) {
                            viewHolder.type.setImageResource(R.drawable.ic_comment_notification);
                        } else if (model.getType().equalsIgnoreCase("Like")) {
                            viewHolder.type.setImageResource(R.drawable.ic_like_notification);
                        }


                        /*---   NOTIFICATION DETAILS   ---*/
                        viewHolder.time.setText(lastSeenTime);
                        viewHolder.title.setText(model.getTitle());
                        viewHolder.details.setText(model.getDetails());

                        if (!model.getComment().equalsIgnoreCase("")) {
                            viewHolder.comment.setVisibility(View.VISIBLE);
                            viewHolder.comment.setText(model.getComment());
                        } else {
                            viewHolder.comment.setVisibility(View.GONE);
                        }


                    }

                }

            }
        };
        notificationRecycler.setAdapter(adapter);

    }

}
