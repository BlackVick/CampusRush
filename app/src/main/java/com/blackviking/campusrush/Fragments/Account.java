package com.blackviking.campusrush.Fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.CampusBusiness.AdDetails;
import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.FeedDetails;
import com.blackviking.campusrush.Model.FeedModel;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.AccountSettings;
import com.blackviking.campusrush.Settings.Settings;
import com.blackviking.campusrush.ViewHolder.FeedViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class Account extends Fragment {

    private CircleImageView myProfileImage;
    private TextView accountUsername, accountFullName, profileSetting, accountSetting;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef;
    private String currentUid, theUsername, theFullName, theImage;
    private RecyclerView myTimelineRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<FeedModel, FeedViewHolder> adapter;
    private DatabaseReference timelineRef, likeRef, commentRef;


    public Account() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users").child(currentUid);
        timelineRef = db.getReference("Feed");
        likeRef = db.getReference("Likes");
        commentRef = db.getReference("FeedComments");


        /*---   WIDGETS   ---*/
        myProfileImage = (CircleImageView)v.findViewById(R.id.profileImage);
        accountUsername = (TextView)v.findViewById(R.id.accountUsername);
        accountFullName = (TextView)v.findViewById(R.id.accountFullName);
        accountSetting = (TextView)v.findViewById(R.id.accountSetting);
        profileSetting = (TextView)v.findViewById(R.id.profileSetting);
        myTimelineRecycler = (RecyclerView)v.findViewById(R.id.timelineRecycler);


        /*---   CURRENT USER   ---*/
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                theUsername = dataSnapshot.child("username").getValue().toString();
                theFullName = dataSnapshot.child("lastName").getValue().toString()
                        + " " + dataSnapshot.child("firstName").getValue().toString();
                theImage = dataSnapshot.child("profilePictureThumb").getValue().toString();


                accountUsername.setText("@"+theUsername);
                accountFullName.setText(theFullName);

                if (!theImage.equalsIgnoreCase("")) {

                    Picasso.with(getContext()).load(theImage).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.profile).into(myProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(getContext()).load(theImage)
                                    .placeholder(R.drawable.profile).into(myProfileImage);
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   PROFILE SETTING   ---*/
        profileSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent posterProfile = new Intent(getContext(), MyProfile.class);
                startActivity(posterProfile);
                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

            }
        });


        /*---   ACCOUNT SETTING   ---*/
        accountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accountSettingIntent = new Intent(getContext(), AccountSettings.class);
                startActivity(accountSettingIntent);
                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        loadMyTimeline();

        return v;
    }

    private void loadMyTimeline() {

        /*---   TIMELINE   ---*/
        myTimelineRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        myTimelineRecycler.setLayoutManager(layoutManager);

        Query myTimeline = timelineRef.orderByChild("sender").equalTo(currentUid);

        adapter = new FirebaseRecyclerAdapter<FeedModel, FeedViewHolder>(
                FeedModel.class,
                R.layout.feed_item,
                FeedViewHolder.class,
                myTimeline.limitToLast(50)
        ) {
            @Override
            protected void populateViewHolder(final FeedViewHolder viewHolder, final FeedModel model, final int position) {

                if (model.getUpdateType().equalsIgnoreCase("Ad")){

                    viewHolder.posterName.setVisibility(View.GONE);
                    viewHolder.posterImage.setVisibility(View.GONE);
                    viewHolder.options.setVisibility(View.GONE);
                    viewHolder.commentBtn.setVisibility(View.GONE);
                    viewHolder.commentCount.setVisibility(View.GONE);
                    viewHolder.likeBtn.setVisibility(View.GONE);
                    viewHolder.likeCount.setVisibility(View.GONE);
                    viewHolder.postTime.setVisibility(View.GONE);


                    if (!model.getImageThumbUrl().equalsIgnoreCase("")){

                        viewHolder.postImage.setVisibility(View.VISIBLE);

                        Picasso.with(getContext())
                                .load(model.getImageThumbUrl())
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.campus_rush_feed_placeholder)
                                .into(viewHolder.postImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getContext())
                                                .load(model.getImageThumbUrl())
                                                .placeholder(R.drawable.campus_rush_feed_placeholder)
                                                .into(viewHolder.postImage);
                                    }
                                });

                        viewHolder.postImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent adDetail = new Intent(getContext(), AdDetails.class);
                                adDetail.putExtra("AdId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                                adDetail.putExtra("AdSenderId", model.getSender());
                                startActivity(adDetail);
                                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                            }
                        });

                    } else {

                        viewHolder.postImage.setVisibility(View.GONE);
                        viewHolder.postText.setMaxLines(7);

                    }


                    if (!model.getUpdate().equalsIgnoreCase("")){

                        viewHolder.postText.setVisibility(View.VISIBLE);
                        viewHolder.postText.setText(model.getUpdate());
                        viewHolder.postText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent adDetail = new Intent(getContext(), AdDetails.class);
                                adDetail.putExtra("AdId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                                adDetail.putExtra("AdSenderId", model.getSender());
                                startActivity(adDetail);
                                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                            }
                        });

                    } else {

                        viewHolder.postText.setVisibility(View.GONE);

                    }

                } else {
                    /*---   OPTIONS   ---*/
                    viewHolder.options.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            /*---   POPUP MENU FOR UPDATE   ---*/
                            PopupMenu popup = new PopupMenu(getContext(), viewHolder.options);
                            popup.inflate(R.menu.feed_item_menu);
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.action_feed_delete:

                                            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                                    .setTitle("Delete Update !")
                                                    .setIcon(R.drawable.ic_delete_feed)
                                                    .setMessage("Are You Sure You Want To Delete This Update From Your Timeline?")
                                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            timelineRef.child(adapter.getRef(viewHolder.getAdapterPosition()).getKey()).removeValue()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });

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

                                            return true;
                                        case R.id.action_feed_share:

                                            Intent i = new Intent(android.content.Intent.ACTION_SEND);
                                            i.setType("text/plain");
                                            i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Campus Rush Share");
                                            i.putExtra(android.content.Intent.EXTRA_TEXT, "Hey There, \n \nCheck Out My Latest Post On The CAMPUS RUSH App.\n\nYou can download for free on playstore via the link below \nhttps://play.google.com/store/apps/details?id=com.blackviking.campusrush");
                                            i.putExtra("FeedId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                                            startActivity(Intent.createChooser(i, "Share via"));

                                            return true;

                                        default:
                                            return false;
                                    }
                                }
                            });

                            popup.show();
                        }
                    });


                    /*---   POSTER DETAILS   ---*/
                    if (!theImage.equals("")) {

                        Picasso.with(getContext())
                                .load(theImage)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.profile)
                                .into(viewHolder.posterImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getContext())
                                                .load(theImage)
                                                .placeholder(R.drawable.ic_loading_animation)
                                                .into(viewHolder.posterImage);
                                    }
                                });

                    } else {

                        viewHolder.posterImage.setImageResource(R.drawable.profile);

                    }
                    viewHolder.posterName.setText("@" + theUsername);


                    /*---   POST IMAGE   ---*/
                    if (!model.getImageThumbUrl().equals("")) {

                        viewHolder.postImage.setVisibility(View.VISIBLE);

                        Picasso.with(getContext())
                                .load(model.getImageThumbUrl())
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.campus_rush_feed_placeholder)
                                .into(viewHolder.postImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getContext())
                                                .load(model.getImageThumbUrl())
                                                .placeholder(R.drawable.campus_rush_feed_placeholder)
                                                .into(viewHolder.postImage);
                                    }
                                });

                    } else {

                        viewHolder.postImage.setVisibility(View.GONE);

                    }


                    /*---   UPDATE   ---*/
                    if (!model.getUpdate().equals("")) {

                        viewHolder.postText.setVisibility(View.VISIBLE);
                        viewHolder.postText.setText(model.getUpdate());

                    } else {

                        viewHolder.postText.setVisibility(View.GONE);

                    }


                    /*---  TIME   ---*/
                    viewHolder.postTime.setText(model.getTimestamp());


                    /*---   LIKES   ---*/
                    final String feedId = adapter.getRef(viewHolder.getAdapterPosition()).getKey();
                    likeRef.child(feedId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            /*---   LIKES   ---*/
                            int countLike = (int) dataSnapshot.getChildrenCount();

                            viewHolder.likeCount.setText(String.valueOf(countLike));

                            if (dataSnapshot.child(currentUid).exists()) {

                                viewHolder.likeBtn.setImageResource(R.drawable.liked_icon);

                                viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        likeRef.child(feedId).child(currentUid).removeValue();
                                    }
                                });

                            } else {

                                viewHolder.likeBtn.setImageResource(R.drawable.unliked_icon);

                                viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        likeRef.child(feedId).child(currentUid).setValue("liked");
                                    }
                                });

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    /*---   COMMENTS   ---*/
                    commentRef.child(feedId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            int countComment = (int) dataSnapshot.getChildrenCount();

                            viewHolder.commentCount.setText(String.valueOf(countComment));

                            viewHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent feedDetail = new Intent(getContext(), FeedDetails.class);
                                    feedDetail.putExtra("CurrentFeedId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                                    startActivity(feedDetail);
                                    getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    /*---   FEED IMAGE CLICK   ---*/
                    viewHolder.postImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent feedDetail = new Intent(getContext(), FeedDetails.class);
                            feedDetail.putExtra("CurrentFeedId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                            startActivity(feedDetail);
                            getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                        }
                    });


                    /*---   FEED TEXT CLICK   ---*/
                    viewHolder.postText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent feedDetail = new Intent(getContext(), FeedDetails.class);
                            feedDetail.putExtra("CurrentFeedId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                            startActivity(feedDetail);
                            getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                        }
                    });
                }


            }
        };
        myTimelineRecycler.setAdapter(adapter);

    }

    private void openNotificationDialog(final String key) {

        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.feed_notification_layout,null);

        final RadioGroup notificationGroup = (RadioGroup) viewOptions.findViewById(R.id.notificationGroup);
        final RadioButton notificationOn = (RadioButton) viewOptions.findViewById(R.id.notificationOn);
        final RadioButton notificationOff = (RadioButton) viewOptions.findViewById(R.id.notificationOff);
        String notiState = Paper.book().read(Common.FEED_NOTIFICATION_TOPIC+key);

        /*---   NOTIFICATION SWITCH SETTINGS HANDLER   ---*/
        if (notiState == null || TextUtils.isEmpty(notiState) || notiState.equals("false")) {
            notificationOff.setChecked(true);
            notificationOn.setChecked(false);
        } else {
            notificationOff.setChecked(false);
            notificationOn.setChecked(true);
        }

        notificationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){

                    case R.id.notificationOn:
                        FirebaseMessaging.getInstance().subscribeToTopic(Common.FEED_NOTIFICATION_TOPIC+key);
                        Paper.book().write(Common.FEED_NOTIFICATION_TOPIC+key, "true");

                    case R.id.notificationOff:
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC+key);
                        Paper.book().write(Common.FEED_NOTIFICATION_TOPIC+key, "false");
                }
            }
        });

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 200; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);


        alertDialog.show();

    }

}
