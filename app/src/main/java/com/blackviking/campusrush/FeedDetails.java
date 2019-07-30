package com.blackviking.campusrush;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Common.GetTimeAgo;
import com.blackviking.campusrush.Model.CommentModel;
import com.blackviking.campusrush.Notification.APIService;
import com.blackviking.campusrush.Notification.DataMessage;
import com.blackviking.campusrush.Notification.MyResponse;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.Profile.OtherUserProfile;
import com.blackviking.campusrush.Settings.Help;
import com.blackviking.campusrush.ViewHolder.CommentViewHolder;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FeedDetails extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private ImageView postImage, likeBtn, sendComment; //options;
    private CircleImageView posterImage;
    private TextView posterName, postText, likeCount, commentCount, postTime;
    private EditText commentBox;
    private RelativeLayout rootLayout;
    private RecyclerView commentRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<CommentModel, CommentViewHolder> adapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, updateRef, likeRef, commentRef;
    public String currentFeedId;
    private String currentUid, privacyState, myUsername, userType;
    private String serverPosterId, serverRealPoster, serverPost, serverPostTime, serverPostImage, serverPostImageThumb;
    private String offenceString = "";
    private APIService mService;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*---   FONT MANAGEMENT   ---*/
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Wigrum-Regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_feed_details);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   INTENT DATA   ---*/
        currentFeedId = getIntent().getStringExtra("CurrentFeedId");


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();

        userRef = db.getReference("Users");
        updateRef = db.getReference("Feed");
        likeRef = db.getReference("Likes");
        commentRef = db.getReference("FeedComments");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        commentRecycler = (RecyclerView)findViewById(R.id.feedCommentRecycler);
        posterImage = (CircleImageView)findViewById(R.id.feedDetailPosterImage);
        postImage = (ImageView)findViewById(R.id.feedDetailPostImage);
        likeBtn = (ImageView)findViewById(R.id.feedDetailLikeBtn);
        sendComment = (ImageView)findViewById(R.id.sendCommentBtn);
        //options = (ImageView)findViewById(R.id.feedDetailOptions);
        posterName = (TextView)findViewById(R.id.feedDetailPosterUsername);
        postText = (TextView)findViewById(R.id.feedDetailPostText);
        likeCount = (TextView)findViewById(R.id.feedDetailLikesCount);
        commentCount = (TextView)findViewById(R.id.feedDetailCommentCount);
        postTime = (TextView)findViewById(R.id.feedDetailPostTime);
        commentBox = (EditText)findViewById(R.id.commentBox);



        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Feed");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(FeedDetails.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        /*---   SEND COMMENT   ---*/
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTheComment();
            }
        });


        /*---   CURRENT USER   ---*/
        userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                myUsername = dataSnapshot.child("username").getValue().toString();
                privacyState = dataSnapshot.child("privacy").getValue().toString();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        loadCurrentUpdate(currentFeedId);

    }

    private void sendTheComment() {

        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yy HH:mm");
        final String dateString = sdf.format(date);

        final String theComment = commentBox.getText().toString().trim();

        if (Common.isConnectedToInternet(getBaseContext())){

            if (!TextUtils.isEmpty(theComment)){

                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("comment", theComment);

                /*---   PRIVACY CHECK   ---*/
                if (privacyState.equalsIgnoreCase("private"))
                    commentMap.put("commenter", "");
                else
                    commentMap.put("commenter", currentUid);

                commentMap.put("realCommenter", currentUid);
                commentMap.put("commentTime", ServerValue.TIMESTAMP);



                commentRef.child(currentFeedId).push().setValue(commentMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            commentBox.setText("");
                            if (!serverRealPoster.equalsIgnoreCase(currentUid)){


                                DatabaseReference notificationRef = db.getReference("Notifications")
                                        .child(serverRealPoster);

                                final Map<String, Object> notificationMap = new HashMap<>();
                                notificationMap.put("title", "Campus Feed");
                                notificationMap.put("details", "Just commented your post");
                                notificationMap.put("comment", theComment);
                                notificationMap.put("type", "Comment");
                                notificationMap.put("status", "Unread");
                                notificationMap.put("intentPrimaryKey", currentFeedId);
                                notificationMap.put("intentSecondaryKey", "");
                                notificationMap.put("user", currentUid);
                                notificationMap.put("timestamp", ServerValue.TIMESTAMP);

                                notificationRef.push().setValue(notificationMap).addOnCompleteListener(
                                        new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isComplete()){
                                                    sendCommentNotification();
                                                }

                                            }
                                        }
                                );

                            }

                        }

                    }
                });


            }

        }

    }

    private void loadComments() {

        /*---   RECYCLER CONTROLLER   ---*/
        commentRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        commentRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<CommentModel, CommentViewHolder>(
                CommentModel.class,
                R.layout.comment_item,
                CommentViewHolder.class,
                commentRef.child(currentFeedId)
        ) {
            @Override
            protected void populateViewHolder(final CommentViewHolder viewHolder, final CommentModel model, int position) {

                /*---   GET TIME AGO ALGORITHM   ---*/
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = model.getCommentTime();
                final String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                viewHolder.time.setText(lastSeenTime);
                viewHolder.comment.setText(model.getComment());

                if (model.getCommenter().equalsIgnoreCase("")){

                    viewHolder.username.setText("PROTECTED");

                } else {

                    userRef.child(model.getCommenter()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String userName = dataSnapshot.child("username").getValue().toString();

                            viewHolder.username.setText("@" + userName);

                            viewHolder.username.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if (model.getCommenter().equals(currentUid)) {

                                        Intent posterProfile = new Intent(FeedDetails.this, MyProfile.class);
                                        startActivity(posterProfile);
                                        overridePendingTransition(R.anim.slide_left, R.anim.slide_out);

                                    } else {

                                        Intent posterProfile = new Intent(FeedDetails.this, OtherUserProfile.class);
                                        posterProfile.putExtra("UserId", model.getCommenter());
                                        startActivity(posterProfile);
                                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                                    }

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        };
        commentRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void loadCurrentUpdate(String feedUpdateId) {

        updateRef.child(feedUpdateId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                serverPosterId = dataSnapshot.child("sender").getValue().toString();
                serverRealPoster = dataSnapshot.child("realSender").getValue().toString();
                serverPost = dataSnapshot.child("update").getValue().toString();
                serverPostImage = dataSnapshot.child("imageUrl").getValue().toString();
                serverPostImageThumb = dataSnapshot.child("imageThumbUrl").getValue().toString();
                serverPostTime = dataSnapshot.child("timestamp").getValue().toString();


                /*---   POSTER DETAILS   ---*/
                if (serverPosterId.equalsIgnoreCase("")) {

                    posterName.setText("PROTECTED");
                    posterImage.setImageResource(R.drawable.profile);

                } else {

                    userRef.child(serverPosterId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final String imageThumbLink = dataSnapshot.child("profilePictureThumb").getValue().toString();
                            String username = dataSnapshot.child("username").getValue().toString();

                            if (!imageThumbLink.equals("")) {

                                Picasso.with(getBaseContext())
                                        .load(imageThumbLink)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.profile)
                                        .into(posterImage, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(getBaseContext())
                                                        .load(imageThumbLink)
                                                        .placeholder(R.drawable.profile)
                                                        .into(posterImage);
                                            }
                                        });

                            } else {

                                posterImage.setImageResource(R.drawable.profile);

                            }

                            posterName.setText("@" + username);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    if (serverPosterId.equals(currentUid)) {

                        /*---   POSTER NAME CLICK   ---*/
                        posterName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent posterProfile = new Intent(FeedDetails.this, MyProfile.class);
                                startActivity(posterProfile);
                                overridePendingTransition(R.anim.slide_left, R.anim.slide_out);

                            }
                        });


                        /*---   POSTER IMAGE CLICK   ---*/
                        posterImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent posterProfile = new Intent(FeedDetails.this, MyProfile.class);
                                startActivity(posterProfile);
                                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                            }
                        });

                    } else {

                        /*---   POSTER NAME CLICK   ---*/
                        posterName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (serverPosterId != null) {

                                    Intent posterProfile = new Intent(FeedDetails.this, OtherUserProfile.class);
                                    posterProfile.putExtra("UserId", serverPosterId);
                                    startActivity(posterProfile);
                                    overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                                }

                            }
                        });


                        /*---   POSTER IMAGE CLICK   ---*/
                        posterImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (serverPosterId != null) {

                                    Intent posterProfile = new Intent(FeedDetails.this, OtherUserProfile.class);
                                    posterProfile.putExtra("UserId", serverPosterId);
                                    startActivity(posterProfile);
                                    overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                                }

                            }
                        });

                    }


                }




                /*---   OPTIONS   ---*/
                /*if (serverRealPoster.equals(currentUid)) {

                    options.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            *//*---   POPUP MENU FOR UPDATE   ---*//*
                            PopupMenu popup = new PopupMenu(FeedDetails.this, options);
                            popup.inflate(R.menu.feed_item_menu);
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.action_feed_delete:

                                            AlertDialog alertDialog = new AlertDialog.Builder(FeedDetails.this)
                                                    .setTitle("Delete Update !")
                                                    .setIcon(R.drawable.ic_delete_feed)
                                                    .setMessage("Are You Sure You Want To Delete This Update From Your Timeline?")
                                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            updateRef.removeValue()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            likeRef.child(currentFeedId).removeValue();
                                                                            commentRef.child(currentFeedId).removeValue();
                                                                            finish();
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
                                            i.putExtra("FeedId", currentFeedId);
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

                    options.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            *//*---   POPUP MENU FOR UPDATE   ---*//*
                            PopupMenu popup = new PopupMenu(FeedDetails.this, options);
                            popup.inflate(R.menu.feed_item_menu_other);
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.action_feed_other_report:

                                            openReportDialog(serverRealPoster);

                                            return true;
                                        case R.id.action_feed_other_share:

                                            Intent i = new Intent(android.content.Intent.ACTION_SEND);
                                            i.setType("text/plain");
                                            i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Campus Rush Share");
                                            i.putExtra(android.content.Intent.EXTRA_TEXT, "Hey There, \n \nCheck Out My Latest Post On The CAMPUS RUSH App.\n\nYou can download for free on playstore via the link below \nhttps://play.google.com/store/apps/details?id=com.blackviking.campusrush");
                                            i.putExtra("FeedId", currentFeedId);
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

                }*/


                /*---   FEED DETAILS   ---*/
                /*---   POST IMAGE   ---*/
                if (!serverPostImageThumb.equals("")) {

                    Picasso.with(getBaseContext())
                            .load(serverPostImageThumb) // thumbnail url goes here
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(postImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Picasso.with(getBaseContext())
                                            .load(serverPostImage) // image url goes here
                                            .placeholder(posterImage.getDrawable())
                                            .into(posterImage);
                                }
                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(serverPostImageThumb) // thumbnail url goes here
                                            .into(postImage, new Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    Picasso.with(getBaseContext())
                                                            .load(serverPostImage) // image url goes here
                                                            .placeholder(postImage.getDrawable())
                                                            .into(postImage);
                                                }
                                                @Override
                                                public void onError() {

                                                }
                                            });
                                }
                            });

                } else {

                    postImage.setVisibility(View.GONE);

                }


                /*---   UPDATE   ---*/
                if (!serverPost.equals("")) {

                    postText.setText(serverPost);

                } else {

                    postText.setVisibility(View.GONE);

                }


                /*---  TIME   ---*/
                postTime.setText(serverPostTime);


                /*---   LIKES   ---*/
                likeRef.child(currentFeedId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        /*---   LIKES   ---*/
                        int countLike = (int) dataSnapshot.getChildrenCount();

                        likeCount.setText(String.valueOf(countLike));

                        if (dataSnapshot.child(currentUid).exists()) {

                            likeBtn.setImageResource(R.drawable.liked_icon);

                            likeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    likeRef.child(currentFeedId).child(currentUid).removeValue();
                                }
                            });

                        } else {

                            likeBtn.setImageResource(R.drawable.unliked_icon);

                            likeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    likeRef.child(currentFeedId).child(currentUid).setValue("liked").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                if (!serverRealPoster.equalsIgnoreCase(currentUid)) {

                                                    DatabaseReference notificationRef = db.getReference("Notifications")
                                                            .child(serverRealPoster);

                                                    final Map<String, Object> notificationMap = new HashMap<>();
                                                    notificationMap.put("title", "Campus Feed");
                                                    notificationMap.put("details", "Just liked your post");
                                                    notificationMap.put("comment", "");
                                                    notificationMap.put("type", "Like");
                                                    notificationMap.put("status", "Unread");
                                                    notificationMap.put("intentPrimaryKey", currentFeedId);
                                                    notificationMap.put("intentSecondaryKey", "");
                                                    notificationMap.put("user", currentUid);
                                                    notificationMap.put("timestamp", ServerValue.TIMESTAMP);

                                                    notificationRef.push().setValue(notificationMap).addOnCompleteListener(
                                                            new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isComplete()){
                                                                        sendLikeNotification();
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
                commentRef.child(currentFeedId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        int countComment = (int) dataSnapshot.getChildrenCount();

                        commentCount.setText(String.valueOf(countComment));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        loadComments();

    }

    private void sendLikeNotification() {

        Map<String, String> dataSend = new HashMap<>();
        dataSend.put("title", "Campus Feed");
        dataSend.put("message", "@"+myUsername+" Just Liked Your Post");
        dataSend.put("feed_id", currentFeedId);
        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(Common.FEED_NOTIFICATION_TOPIC+serverRealPoster).toString(), dataSend);

        mService.sendNotification(dataMessage)
                .enqueue(new retrofit2.Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {
                        Toast.makeText(FeedDetails.this, "Error Sending Notification", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void sendCommentNotification() {

        Map<String, String> dataSend = new HashMap<>();
        dataSend.put("title", "Campus Feed");
        dataSend.put("message", "@"+myUsername+" Just Commented On Your Post");
        dataSend.put("feed_id", currentFeedId);
        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(Common.FEED_NOTIFICATION_TOPIC+serverRealPoster).toString(), dataSend);

        mService.sendNotification(dataMessage)
                .enqueue(new retrofit2.Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {
                        Toast.makeText(FeedDetails.this, "Error Sending Notification", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void openReportDialog(final String sender) {

        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
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
        dataAdapterOffence = new ArrayAdapter(FeedDetails.this, android.R.layout.simple_spinner_item, offenceList);

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

                if (Common.isConnectedToInternet(getBaseContext())){

                    if (offenceString.equals("") || TextUtils.isEmpty(offenceDetails.getText().toString())){

                        Toast.makeText(FeedDetails.this, "Invalid Report !", Toast.LENGTH_SHORT).show();

                    } else {

                        final Map<String, Object> reportUserMap = new HashMap<>();
                        reportUserMap.put("reporter", currentUid);
                        reportUserMap.put("reported", sender);
                        reportUserMap.put("reportClass", offenceString);
                        reportUserMap.put("reportDetails", offenceDetails.getText().toString());

                        reportRef.push().setValue(reportUserMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(FeedDetails.this, "Report Logged, But Remember, Don't Be A Snitch", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                }else {

                    Common.showErrorDialog(FeedDetails.this, "No Internet Access !");
                }
                alertDialog.dismiss();

            }
        });

        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
