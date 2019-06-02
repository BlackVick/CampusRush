package com.blackviking.campusrush.Fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.AddFeed;
import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.FeedDetails;
import com.blackviking.campusrush.Home;
import com.blackviking.campusrush.Model.FeedModel;
import com.blackviking.campusrush.Notification.APIService;
import com.blackviking.campusrush.Notification.DataMessage;
import com.blackviking.campusrush.Notification.MyResponse;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.Profile.OtherUserProfile;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.ViewHolder.FeedViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class Feed extends Fragment {

    private RecyclerView feedRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<FeedModel, FeedViewHolder> adapter;
    private String currentUid;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference feedRef, userRef, likeRef, commentRef;
    private String offenceString = "";
    private APIService mService;
    private FloatingActionButton addFeedButton;

    public Feed() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feed, container, false);


        /*---   PAPER DB   ---*/
        Paper.init(getContext());


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();

        feedRef = db.getReference("Feed");
        userRef = db.getReference("Users");
        likeRef = db.getReference("Likes");
        commentRef = db.getReference("FeedComments");


        /*---   WIDGETS   ---*/
        feedRecycler = (RecyclerView)v.findViewById(R.id.feedRecycler);
        addFeedButton = (FloatingActionButton)v.findViewById(R.id.addFeed);

        addFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addFeedIntent = new Intent(getContext(), AddFeed.class);
                startActivity(addFeedIntent);
                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        loadFeed();

        return v;
    }

    private void loadFeed() {

        /*---   RECYCLER CONTROLLER   ---*/
        feedRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        feedRecycler.setLayoutManager(layoutManager);

        final Home theHome = new Home();
        final RelativeLayout theNavLayout = (RelativeLayout)getActivity().findViewById(R.id.navLayout);


        feedRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy < 0 && theNavLayout.getVisibility() == View.GONE)
                    theNavLayout.setVisibility(View.VISIBLE);
                else if(dy > 0 && theNavLayout.getVisibility() == View.VISIBLE)
                    theNavLayout.setVisibility(View.GONE);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                /*if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    theNavLayout.setVisibility(View.VISIBLE);*/
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        adapter = new FirebaseRecyclerAdapter<FeedModel, FeedViewHolder>(
                FeedModel.class,
                R.layout.feed_item,
                FeedViewHolder.class,
                feedRef.limitToLast(75)
        ) {
            @Override
            protected void populateViewHolder(final FeedViewHolder viewHolder, final FeedModel model, final int position) {

                /*---   OPTIONS   ---*/

                final String feedId = adapter.getRef(position).getKey();
                if (model.getRealSender().equals(currentUid)){

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

                                                            feedRef.child(feedId).removeValue()
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
                                            i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Campus Rush Share");
                                            i.putExtra(android.content.Intent.EXTRA_TEXT, "Hey There, \n \nCheck Out My Latest Post On The CAMPUS RUSH App.\n\nYou can download for free on playstore via the link below \nhttps://play.google.com/store/apps/details?id=com.blackviking.campusrush");
                                            i.putExtra("FeedId", feedId);
                                            startActivity(Intent.createChooser(i,"Share via"));

                                            return true;

                                        default:
                                            return false;
                                    }
                                }
                            });

                            popup.show();
                        }
                    });

                } else {

                    viewHolder.options.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            /*---   POPUP MENU FOR UPDATE   ---*/
                            PopupMenu popup = new PopupMenu(getContext(), viewHolder.options);
                            popup.inflate(R.menu.feed_item_menu_other);
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.action_feed_other_report:

                                            openReportDialog(model.getSender());

                                            return true;
                                        case R.id.action_feed_other_share:

                                            Intent i = new Intent(android.content.Intent.ACTION_SEND);
                                            i.setType("text/plain");
                                            i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Campus Rush Share");
                                            i.putExtra(android.content.Intent.EXTRA_TEXT, "Hey There, \n \nCheck Out My Latest Post On The CAMPUS RUSH App.\n\nYou can download for free on playstore via the link below \nhttps://play.google.com/store/apps/details?id=com.blackviking.campusrush");
                                            i.putExtra("FeedId", feedId);
                                            startActivity(Intent.createChooser(i,"Share via"));

                                            return true;
                                        default:
                                            return false;
                                    }
                                }
                            });

                            popup.show();
                        }
                    });

                }




                /*---   POSTER DETAILS   ---*/
                if (model.getSender().equalsIgnoreCase("")) {

                    viewHolder.posterName.setText("PROTECTED");
                    viewHolder.posterImage.setImageResource(R.drawable.profile);

                } else {

                    userRef.child(model.getSender()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String imageLink = dataSnapshot.child("profilePicture").getValue().toString();
                            final String imageThumbLink = dataSnapshot.child("profilePictureThumb").getValue().toString();
                            String username = dataSnapshot.child("username").getValue().toString();

                            if (!imageThumbLink.equals("")) {

                                Picasso.with(getContext())
                                        .load(imageThumbLink)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.ic_loading_animation)
                                        .into(viewHolder.posterImage, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(getContext())
                                                        .load(imageThumbLink)
                                                        .placeholder(R.drawable.ic_loading_animation)
                                                        .into(viewHolder.posterImage);
                                            }
                                        });

                            } else {

                                viewHolder.posterImage.setImageResource(R.drawable.profile);

                            }

                            viewHolder.posterName.setText("@" + username);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    if (model.getSender().equals(currentUid)) {

                        /*---   POSTER NAME CLICK   ---*/
                        viewHolder.posterName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent posterProfile = new Intent(getContext(), MyProfile.class);
                                startActivity(posterProfile);
                                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                            }
                        });


                        /*---   POSTER IMAGE CLICK   ---*/
                        viewHolder.posterImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent posterProfile = new Intent(getContext(), MyProfile.class);
                                startActivity(posterProfile);
                                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                            }
                        });

                    } else {

                        /*---   POSTER NAME CLICK   ---*/
                        viewHolder.posterName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent posterProfile = new Intent(getContext(), OtherUserProfile.class);
                                posterProfile.putExtra("UserId", model.getSender());
                                startActivity(posterProfile);
                                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                            }
                        });


                        /*---   POSTER IMAGE CLICK   ---*/
                        viewHolder.posterImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent posterProfile = new Intent(getContext(), OtherUserProfile.class);
                                posterProfile.putExtra("UserId", model.getSender());
                                startActivity(posterProfile);
                                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                            }
                        });

                    }

                }


                /*---   POST IMAGE   ---*/
                if (!model.getImageThumbUrl().equals("")){

                    viewHolder.postImage.setVisibility(View.VISIBLE);

                    Picasso.with(getContext())
                            .load(model.getImageThumbUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(viewHolder.postImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getContext())
                                            .load(model.getImageThumbUrl())
                                            .placeholder(R.drawable.ic_loading_animation)
                                            .into(viewHolder.postImage);
                                }
                            });

                } else {

                    viewHolder.postImage.setVisibility(View.GONE);

                }


                /*---   UPDATE   ---*/
                if (!model.getUpdate().equals("")){

                    viewHolder.postText.setVisibility(View.VISIBLE);
                    viewHolder.postText.setText(model.getUpdate());

                } else {

                    viewHolder.postText.setVisibility(View.GONE);

                }


                /*---  TIME   ---*/
                viewHolder.postTime.setText(model.getTimestamp());


                /*---   LIKES   ---*/
                likeRef.child(feedId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        /*---   LIKES   ---*/
                        int countLike = (int) dataSnapshot.getChildrenCount();

                        viewHolder.likeCount.setText(String.valueOf(countLike));

                        if (dataSnapshot.child(currentUid).exists()){

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
                                    likeRef.child(feedId).child(currentUid).setValue("liked").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                if (!model.getRealSender().equalsIgnoreCase(currentUid)){

                                                    DatabaseReference notificationRef = db.getReference("Notifications")
                                                            .child(model.getRealSender());

                                                    final Map<String, Object> notificationMap = new HashMap<>();
                                                    notificationMap.put("title", "Campus Feed");
                                                    notificationMap.put("details", "Just liked your post");
                                                    notificationMap.put("comment", "");
                                                    notificationMap.put("type", "Like");
                                                    notificationMap.put("status", "Unread");
                                                    notificationMap.put("intentPrimaryKey", feedId);
                                                    notificationMap.put("intentSecondaryKey", "");
                                                    notificationMap.put("user", currentUid);
                                                    notificationMap.put("timestamp", ServerValue.TIMESTAMP);

                                                    notificationRef.push().setValue(notificationMap).addOnCompleteListener(
                                                            new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isComplete()){
                                                                        sendLikeNotification(feedId, model.getRealSender());
                                                                    }

                                                                }
                                                            }
                                                    );

                                                }

                                            }

                                        }
                                    });


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
                                feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
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
                        feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
                        startActivity(feedDetail);
                        getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                    }
                });


                /*---   FEED TEXT CLICK   ---*/
                viewHolder.postText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent feedDetail = new Intent(getContext(), FeedDetails.class);
                        feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
                        startActivity(feedDetail);
                        getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                    }
                });


            }
        };
        feedRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void openReportDialog(final String sender) {

        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.report_form,null);

        final Spinner offenceClass = (Spinner) viewOptions.findViewById(R.id.reportTypeSpinner);
        final EditText offenceDetails = (EditText) viewOptions.findViewById(R.id.reportDetails);
        final Button submitReport = (Button) viewOptions.findViewById(R.id.submitReportBtn);
        final DatabaseReference reportRef = db.getReference("Reports");


        /*---   SETUP SPINNER   ---*/
        /*---   FILL GENDER SPINNER   ---*/
        List<String> offenceList = new ArrayList<>();
        offenceList.add(0, "Report Type");
        offenceList.add("Inappropriate Content");
        offenceList.add("Offensive Acts");
        offenceList.add("Bullying");

        ArrayAdapter<String> dataAdapterOffence;
        dataAdapterOffence = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, offenceList);

        dataAdapterOffence.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        offenceClass.setAdapter(dataAdapterOffence);


        /*---    GENDER SPINNER   ---*/
        offenceClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Report Type")){

                    offenceString = parent.getItemAtPosition(position).toString();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

        submitReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getContext())){

                    if (offenceString.equals("") || TextUtils.isEmpty(offenceDetails.getText().toString())){

                        Toast.makeText(getContext(), "Invalid Report", Toast.LENGTH_SHORT).show();

                    } else {

                        final Map<String, Object> reportUserMap = new HashMap<>();
                        reportUserMap.put("reporter", currentUid);
                        reportUserMap.put("reported", sender);
                        reportUserMap.put("reportClass", offenceString);
                        reportUserMap.put("reportDetails", offenceDetails.getText().toString());

                        reportRef.push().setValue(reportUserMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "B***h Snitch", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                }else {

                    Common.showErrorDialog(getContext(), "No Internet Access !");
                }
                alertDialog.dismiss();

            }
        });

        alertDialog.show();

    }

    private void sendLikeNotification(final String feedId, final String feederId) {

        userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.child("username").getValue().toString();

                Map<String, String> dataSend = new HashMap<>();
                dataSend.put("title", "Campus Feed");
                dataSend.put("message", "@"+userName+" Just Liked Your Post");
                dataSend.put("feed_id", feedId);
                DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(Common.FEED_NOTIFICATION_TOPIC+feederId).toString(), dataSend);

                mService.sendNotification(dataMessage)
                        .enqueue(new retrofit2.Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Toast.makeText(getContext(), "Error Sending Notification", Toast.LENGTH_SHORT).show();
                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
