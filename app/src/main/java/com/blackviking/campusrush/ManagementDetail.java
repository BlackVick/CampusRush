package com.blackviking.campusrush;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.Model.AdminManageModel;
import com.blackviking.campusrush.Plugins.SkitCenter.SkitDetails;
import com.blackviking.campusrush.Plugins.SkitCenter.SkitModel;
import com.blackviking.campusrush.Plugins.SkitCenter.SkitViewHolder;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.Profile.OtherUserProfile;
import com.blackviking.campusrush.Settings.Help;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
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

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ManagementDetail extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference skitRef, userRef, feedRef, adminRef, pushRef;
    private String currentUid, itemId, itemType;
    private Button approveBtn, denyBtn;
    private LinearLayout feedLayout, skitLayout;

    private CircleImageView posterImage;
    private TextView posterName, postText, postTime;
    private ImageView postImage;

    private TextView title, owner, description;
    private SimpleExoPlayerView videoPlayer;
    private SimpleExoPlayer exoPlayer;
    private String skitOwnerUid;


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

        setContentView(R.layout.activity_management_detail);


        /*---   LOCAL   ---*/
        itemId = getIntent().getStringExtra("ItemId");
        itemType = getIntent().getStringExtra("ItemType");


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        skitRef = db.getReference("Skits");
        userRef = db.getReference("Users");
        feedRef = db.getReference("Feed");
        adminRef = db.getReference("AdminManagement");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        approveBtn = (Button) findViewById(R.id.approveBtn);
        denyBtn = (Button) findViewById(R.id.denyBtn);

        feedLayout = (LinearLayout)findViewById(R.id.feedLayout);
        posterImage = (CircleImageView)findViewById(R.id.feedDetailPosterImage);
        postImage = (ImageView)findViewById(R.id.feedDetailPostImage);
        posterName = (TextView)findViewById(R.id.feedDetailPosterUsername);
        postText = (TextView)findViewById(R.id.feedDetailPostText);
        postTime = (TextView)findViewById(R.id.feedDetailPostTime);

        skitLayout = (LinearLayout)findViewById(R.id.skitLayout);
        title = (TextView)findViewById(R.id.currentSkitTitle);
        owner = (TextView)findViewById(R.id.currentSkitOwner);
        description = (TextView)findViewById(R.id.currentSkitDescription);
        videoPlayer = (SimpleExoPlayerView)findViewById(R.id.videoPlayer);





        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exoPlayer != null) {
                    exoPlayer.release();
                    exoPlayer.stop();
                }
                finish();
            }
        });

        activityName.setText("Management Detail");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(ManagementDetail.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        if (Common.isConnectedToInternet(getBaseContext())) {

            if (itemType.equalsIgnoreCase("Feed")) {
                loadFeedItem();

            } else if (itemType.equalsIgnoreCase("Skit")){
                loadSkitItem();

            }

        } else {
            showErrorDialog("No Internet Access !");
        }
    }

    private void loadSkitItem() {

        feedLayout.setVisibility(View.GONE);
        skitLayout.setVisibility(View.VISIBLE);

        adminRef.child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String theOwner = dataSnapshot.child("owner").getValue().toString();
                final String theTitle = dataSnapshot.child("title").getValue().toString();
                final String theDescription = dataSnapshot.child("description").getValue().toString();
                final String theMediaLink = dataSnapshot.child("mediaUrl").getValue().toString();
                final String theThumbnail = dataSnapshot.child("thumbnail").getValue().toString();

                /*---   CURRENT SKIT USER   ---*/
                userRef.orderByChild("username").equalTo(theOwner).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot child : dataSnapshot.getChildren()) {

                            skitOwnerUid = child.getKey();

                        }

                        owner.setText("@"+theOwner);
                        owner.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Common.isConnectedToInternet(getBaseContext())){

                                    if (skitOwnerUid.equals(currentUid)){

                                        Intent myProfileIntent = new Intent(ManagementDetail.this, MyProfile.class);
                                        startActivity(myProfileIntent);
                                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                                    } else {

                                        Intent otherUserProfileIntent = new Intent(ManagementDetail.this, OtherUserProfile.class);
                                        otherUserProfileIntent.putExtra("UserId", skitOwnerUid);
                                        startActivity(otherUserProfileIntent);
                                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                                    }

                                } else {

                                    showErrorDialog("No Internet Access !");

                                }

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                title.setText(theTitle);
                description.setText(theDescription);

                try {

                    BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                    TrackSelector selector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                    exoPlayer = ExoPlayerFactory.newSimpleInstance(ManagementDetail.this, selector);

                    Uri videoUri = Uri.parse(theMediaLink);

                    DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("Campus Rush");
                    ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                    MediaSource videoSource = new ExtractorMediaSource(videoUri, dataSourceFactory, extractorsFactory, null, null);

                    videoPlayer.setPlayer(exoPlayer);
                    exoPlayer.prepare(videoSource);

                    exoPlayer.setPlayWhenReady(true);

                } catch (Exception e){

                    Toast.makeText(ManagementDetail.this, "Video Parse Error", Toast.LENGTH_SHORT).show();

                }


                /*---   APPROVAL   ---*/
                approveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (Common.isConnectedToInternet(getBaseContext())) {

                            final Map<String, Object> newSkitMap = new HashMap<>();
                            newSkitMap.put("title", theTitle);
                            newSkitMap.put("owner", theOwner);
                            newSkitMap.put("mediaUrl", theMediaLink);
                            newSkitMap.put("thumbnail", theThumbnail);
                            newSkitMap.put("description", theDescription);

                            pushRef = skitRef.push();
                            final String pushId = pushRef.getKey();
                            skitRef.child(pushId).setValue(newSkitMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        adminRef.child(itemId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                if (exoPlayer != null) {
                                                    exoPlayer.release();
                                                    exoPlayer.stop();
                                                }
                                                finish();
                                            }
                                        });

                                    }
                                }
                            });

                        } else {

                            showErrorDialog("No Internet Access !");

                        }

                    }
                });


                /*---   DENIAL   ---*/
                denyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Common.isConnectedToInternet(getBaseContext())){

                            adminRef.child(itemId)
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (exoPlayer != null) {
                                        exoPlayer.release();
                                        exoPlayer.stop();
                                    }
                                    finish();
                                }
                            });

                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadFeedItem() {

        feedLayout.setVisibility(View.VISIBLE);
        skitLayout.setVisibility(View.GONE);

        adminRef.child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String theSourceType = dataSnapshot.child("sourceType").getValue().toString();
                final String theUpdate = dataSnapshot.child("update").getValue().toString();
                final String theSender = dataSnapshot.child("sender").getValue().toString();
                final String theRealSender = dataSnapshot.child("realSender").getValue().toString();
                final String theImageUrl = dataSnapshot.child("imageUrl").getValue().toString();
                final String theImageThumbUrl = dataSnapshot.child("imageThumbUrl").getValue().toString();
                final String theTimeStamp = dataSnapshot.child("timestamp").getValue().toString();
                final String theUpdateType = dataSnapshot.child("updateType").getValue().toString();


                /*---   POSTER DETAILS   ---*/
                if (theSender.equalsIgnoreCase("")) {

                    posterName.setText("PROTECTED");
                    posterImage.setImageResource(R.drawable.profile);

                } else {

                    userRef.child(theSender).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final String imageThumbLink = dataSnapshot.child("profilePictureThumb").getValue().toString();
                            String username = dataSnapshot.child("username").getValue().toString();

                            posterName.setText("@" + username);

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

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    if (theSender.equals(currentUid)) {

                        /*---   POSTER NAME CLICK   ---*/
                        posterName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent posterProfile = new Intent(ManagementDetail.this, MyProfile.class);
                                startActivity(posterProfile);
                                overridePendingTransition(R.anim.slide_left, R.anim.slide_out);

                            }
                        });


                        /*---   POSTER IMAGE CLICK   ---*/
                        posterImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent posterProfile = new Intent(ManagementDetail.this, MyProfile.class);
                                startActivity(posterProfile);
                                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                            }
                        });

                    } else {

                        /*---   POSTER NAME CLICK   ---*/
                        posterName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent posterProfile = new Intent(ManagementDetail.this, OtherUserProfile.class);
                                posterProfile.putExtra("UserId", theSender);
                                startActivity(posterProfile);
                                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                            }
                        });


                        /*---   POSTER IMAGE CLICK   ---*/
                        posterImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent posterProfile = new Intent(ManagementDetail.this, OtherUserProfile.class);
                                posterProfile.putExtra("UserId", theSender);
                                startActivity(posterProfile);
                                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                            }
                        });

                    }


                }


                /*---   FEED DETAILS   ---*/
                /*---   POST IMAGE   ---*/
                if (!theImageThumbUrl.equals("")) {

                    Picasso.with(getBaseContext())
                            .load(theImageThumbUrl) // thumbnail url goes here
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(postImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Picasso.with(getBaseContext())
                                            .load(theImageUrl) // image url goes here
                                            .placeholder(posterImage.getDrawable())
                                            .into(posterImage);
                                }
                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(theImageThumbUrl) // thumbnail url goes here
                                            .into(postImage, new Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    Picasso.with(getBaseContext())
                                                            .load(theImageUrl) // image url goes here
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
                if (!theUpdate.equals("")) {

                    postText.setText(theUpdate);

                } else {

                    postText.setVisibility(View.GONE);

                }


                /*---  TIME   ---*/
                postTime.setText(theTimeStamp);


                /*---   APPROVAL   ---*/
                approveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (Common.isConnectedToInternet(getBaseContext())) {

                            final Map<String, Object> newFeedMap = new HashMap<>();
                            newFeedMap.put("sourceType", theSourceType);
                            newFeedMap.put("update", theUpdate);
                            newFeedMap.put("sender", theSender);
                            newFeedMap.put("realSender", theRealSender);
                            newFeedMap.put("imageUrl", theImageUrl);
                            newFeedMap.put("imageThumbUrl", theImageThumbUrl);
                            newFeedMap.put("timestamp", theTimeStamp);
                            newFeedMap.put("updateType", theUpdateType);

                            pushRef = feedRef.push();
                            final String pushId = pushRef.getKey();
                            feedRef.child(pushId).setValue(newFeedMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        adminRef.child(itemId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                finish();
                                            }
                                        });

                                    }
                                }
                            });

                        } else {

                            showErrorDialog("No Internet Access !");

                        }

                    }
                });


                /*---   DENIAL   ---*/
                denyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Common.isConnectedToInternet(getBaseContext())){

                            adminRef.child(itemId)
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    finish();
                                }
                            });

                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer.stop();
        }
        finish();
    }

    /*---   WARNING DIALOG   ---*/
    public void showErrorDialog(String theWarning){

        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("Attention !")
                .setIcon(R.drawable.ic_attention_red)
                .setMessage(theWarning)
                .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.show();

    }
}
