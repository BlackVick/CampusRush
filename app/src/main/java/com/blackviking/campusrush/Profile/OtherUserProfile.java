package com.blackviking.campusrush.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.blackviking.campusrush.FeedDetails;
import com.blackviking.campusrush.ImageController.BlurImage;
import com.blackviking.campusrush.ImageController.ImageViewer;
import com.blackviking.campusrush.Model.FeedModel;
import com.blackviking.campusrush.Notification.APIService;
import com.blackviking.campusrush.Notification.DataMessage;
import com.blackviking.campusrush.Notification.MyResponse;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OtherUserProfile extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private String userId, currentUid;
    private ImageView userProfileImage, coverPhoto;
    private TextView username, fullName, status, department, gender, bio;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, timelineRef, likeRef, commentRef, businessProfileRef;
    private CoordinatorLayout rootLayout;
    private int BLUR_PRECENTAGE = 50;
    private RecyclerView timelineRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<FeedModel, FeedViewHolder> adapter;
    private Target target;
    private String offenceString = "";
    private APIService mService;
    private String serverUsername, serverFullName, serverGender, serverStatus,
            serverDepartment, serverBio, serverProfilePictureThumb,
            serverProfilePicture, serverUserType;

    private RelativeLayout businessLayout;
    private TextView busName, busAddress, busCategory, busDescription, busPhone, busFacebook, busInstagram, busTwitter,
            accountType;
    private FloatingActionButton sendUserMessage;

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

        setContentView(R.layout.activity_other_user_profile);


        /*---   TOOLBAR   ---*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   INTENT DATA   ---*/
        userId = getIntent().getStringExtra("UserId");


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users");
        timelineRef = db.getReference("Feed");
        likeRef = db.getReference("Likes");
        commentRef = db.getReference("FeedComments");
        businessProfileRef = db.getReference("BusinessProfile");


        /*---   WIDGETS   ---*/
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        coverPhoto = (ImageView)findViewById(R.id.userProfilePictureBlur);
        userProfileImage = (ImageView)findViewById(R.id.userProfilePicture);
        username = (TextView)findViewById(R.id.userUsername);
        fullName = (TextView)findViewById(R.id.userFullName);
        status = (TextView)findViewById(R.id.userStatus);
        gender = (TextView)findViewById(R.id.userGender);
        department = (TextView)findViewById(R.id.userDepartment);
        bio = (TextView)findViewById(R.id.userBio);
        timelineRecycler = (RecyclerView)findViewById(R.id.otherUserTimelineRecycler);


        sendUserMessage = (FloatingActionButton)findViewById(R.id.messageUser);
        businessLayout = (RelativeLayout)findViewById(R.id.userProfileBusinessLayout);
        busName = (TextView)findViewById(R.id.userBusinessNameTxt);
        busAddress = (TextView)findViewById(R.id.userBusinessAddressTxt);
        busCategory = (TextView)findViewById(R.id.userBusinessCategoryTxt);
        busDescription = (TextView)findViewById(R.id.userBusinessDescriptionTxt);
        busPhone = (TextView)findViewById(R.id.userBusinessPhoneTxt);
        busFacebook = (TextView)findViewById(R.id.userBusinessFacebookTxt);
        busInstagram = (TextView)findViewById(R.id.userBusinessInstagramTxt);
        busTwitter = (TextView)findViewById(R.id.userBusinessTwitterTxt);
        accountType = (TextView)findViewById(R.id.userAccountType);


        /*---   TOOLBAR   ---*/
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        /*---   FAB   ---*/
        sendUserMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userId.equalsIgnoreCase("")){
                    /*Intent messageIntent = new Intent(OtherUserProfile.this, Messaging.class);
                    messageIntent.putExtra("UserId", userId);
                    startActivity(messageIntent);
                    overridePendingTransition(R.anim.slide_left, R.anim.slide_left);*/
                }
            }
        });


        /*---   LOAD PROFILE   ---*/
        if (Common.isConnectedToInternet(getBaseContext())){

            loadUserProfile(userId);
            loadUserTimeline(userId);

        } else {
            Common.showErrorDialog(OtherUserProfile.this, "Could Not Load User Profile Because There Is No Internet Access !");
        }
    }

    private void loadUserProfile(final String userId) {

        /*---   BLUR COVER   ---*/
        target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                coverPhoto.setImageBitmap(BlurImage.fastblur(bitmap, 1f,
                        BLUR_PRECENTAGE));
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                coverPhoto.setImageResource(R.drawable.profile);
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        userRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                serverUsername = dataSnapshot.child("username").getValue().toString();
                serverFullName = dataSnapshot.child("lastName").getValue().toString() + " " + dataSnapshot.child("firstName").getValue().toString();
                serverStatus = dataSnapshot.child("status").getValue().toString();
                serverGender = dataSnapshot.child("gender").getValue().toString();
                serverDepartment = dataSnapshot.child("department").getValue().toString();
                serverBio = dataSnapshot.child("bio").getValue().toString();
                serverProfilePictureThumb = dataSnapshot.child("profilePictureThumb").getValue().toString();
                serverProfilePicture = dataSnapshot.child("profilePicture").getValue().toString();
                serverUserType = dataSnapshot.child("userType").getValue().toString();

                /*---   DETAILS   ---*/
                collapsingToolbarLayout.setTitle("@"+serverUsername);
                username.setText("@"+serverUsername);
                fullName.setText(serverFullName);
                status.setText(serverStatus);
                department.setText(serverDepartment);
                gender.setText(serverGender);
                bio.setText(serverBio);

                if (serverUserType.equalsIgnoreCase("Business") || serverUserType.equalsIgnoreCase("Admin")){
                    accountType.setText("Business Account");
                } else {
                    accountType.setText("Regular Account");
                }


                /*---   ACCOUNT TYPE   ---*/
                businessProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(userId).exists()) {

                            businessLayout.setVisibility(View.VISIBLE);

                            String theBusName = dataSnapshot.child(userId).child("businessName").getValue().toString();
                            String theBusAddress = dataSnapshot.child(userId).child("businessAddress").getValue().toString();
                            String theBusCategory = dataSnapshot.child(userId).child("businessCategory").getValue().toString();
                            String theBusDescription = dataSnapshot.child(userId).child("businessDescription").getValue().toString();
                            String theBusPhone = dataSnapshot.child(userId).child("businessPhone").getValue().toString();
                            String theBusFacebook = dataSnapshot.child(userId).child("businessFacebook").getValue().toString();
                            String theBusInstagram = dataSnapshot.child(userId).child("businessInstagram").getValue().toString();
                            String theBusTwitter = dataSnapshot.child(userId).child("businessTwitter").getValue().toString();


                            busName.setText(theBusName);
                            busAddress.setText(theBusAddress);
                            busCategory.setText(theBusCategory);
                            busDescription.setText(theBusDescription);
                            busPhone.setText(theBusPhone);
                            busFacebook.setText(theBusFacebook);
                            busInstagram.setText(theBusInstagram);
                            busTwitter.setText(theBusTwitter);

                        } else {

                            businessLayout.setVisibility(View.GONE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                /*---   IMAGE   ---*/
                if (!serverProfilePictureThumb.equals("")){

                    /*---   PROFILE IMAGE   ---*/
                    Picasso.with(getBaseContext())
                            .load(serverProfilePictureThumb)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(userProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(serverProfilePictureThumb)
                                            .placeholder(R.drawable.ic_loading_animation)
                                            .into(userProfileImage);
                                }
                            });


                    /*---   COVER PHOTO   ---*/
                    Picasso.with(getBaseContext())
                            .load(serverProfilePictureThumb)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(target);


                    userProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent profileImgIntent = new Intent(OtherUserProfile.this, ImageViewer.class);
                            profileImgIntent.putExtra("ImageLink", serverProfilePicture);
                            profileImgIntent.putExtra("ImageThumbLink", serverProfilePictureThumb);
                            startActivity(profileImgIntent);
                            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadUserTimeline(final String userId) {

        /*---   TIMELINE RECYCLER   ---*/
        timelineRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        timelineRecycler.setLayoutManager(layoutManager);

        Query myTimeline = timelineRef.orderByChild("sender").equalTo(userId);

        adapter = new FirebaseRecyclerAdapter<FeedModel, FeedViewHolder>(
                FeedModel.class,
                R.layout.feed_item,
                FeedViewHolder.class,
                myTimeline.limitToLast(50)
        ) {
            @Override
            protected void populateViewHolder(final FeedViewHolder viewHolder, final FeedModel model, final int position) {

                /*---   OPTIONS   ---*/
                viewHolder.options.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        /*---   POPUP MENU FOR UPDATE   ---*/
                        PopupMenu popup = new PopupMenu(OtherUserProfile.this, viewHolder.options);
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
                                        i.putExtra("FeedId", adapter.getRef(position).getKey());
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


                /*---   POSTER DETAILS   ---*/
                viewHolder.posterImage.setVisibility(View.GONE);


                /*---   POST IMAGE   ---*/
                if (!model.getImageThumbUrl().equals("")){

                    viewHolder.postImage.setVisibility(View.VISIBLE);

                    Picasso.with(getBaseContext())
                            .load(model.getImageThumbUrl())
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(viewHolder.postImage);

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
                final String feedId = adapter.getRef(position).getKey();
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

                                                DatabaseReference notificationRef = db.getReference("Notifications")
                                                        .child(userId);

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
                                                                    sendLikeNotification(feedId);
                                                                }

                                                            }
                                                        }
                                                );



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
                                Intent feedDetail = new Intent(OtherUserProfile.this, FeedDetails.class);
                                feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
                                startActivity(feedDetail);
                                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
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

                        Intent feedDetail = new Intent(OtherUserProfile.this, FeedDetails.class);
                        feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
                        startActivity(feedDetail);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                    }
                });


                /*---   FEED TEXT CLICK   ---*/
                viewHolder.postText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent feedDetail = new Intent(OtherUserProfile.this, FeedDetails.class);
                        feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
                        startActivity(feedDetail);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                    }
                });


            }
        };
        timelineRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void sendLikeNotification(final String feedId) {

        userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.child("username").getValue().toString();

                Map<String, String> dataSend = new HashMap<>();
                dataSend.put("title", "Campus Feed");
                dataSend.put("message", "@"+userName+" Just Liked Your Post");
                dataSend.put("feed_id", feedId);
                DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(Common.FEED_NOTIFICATION_TOPIC+userId).toString(), dataSend);

                mService.sendNotification(dataMessage)
                        .enqueue(new retrofit2.Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Toast.makeText(OtherUserProfile.this, "Error Sending Notification", Toast.LENGTH_SHORT).show();
                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void openReportDialog(final String sender) {

        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(OtherUserProfile.this).create();
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
        dataAdapterOffence = new ArrayAdapter(this, android.R.layout.simple_spinner_item, offenceList);

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

                        Toast.makeText(OtherUserProfile.this, "Invalid Report", Toast.LENGTH_SHORT).show();

                    } else {

                        final Map<String, Object> reportUserMap = new HashMap<>();
                        reportUserMap.put("reporter", currentUid);
                        reportUserMap.put("reported", sender);
                        reportUserMap.put("reportClass", offenceString);
                        reportUserMap.put("reportDetails", offenceDetails.getText().toString());

                        reportRef.push().setValue(reportUserMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(OtherUserProfile.this, "B***h Snitch", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                }else {

                    Common.showErrorDialog(OtherUserProfile.this, "No Internet Access !");
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
