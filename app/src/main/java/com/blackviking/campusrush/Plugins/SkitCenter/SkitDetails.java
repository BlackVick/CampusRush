package com.blackviking.campusrush.Plugins.SkitCenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.Profile.OtherUserProfile;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SkitDetails extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference skitRef, skitLikesRef, userRef;
    private String currentUid, currentSkitId, currentSkitOwner, skitOwnerUid;
    private TextView title, owner, description, likes;
    private SimpleExoPlayerView videoPlayer;
    private ProgressBar progressBar;
    private ImageView likeBtn;
    private SimpleExoPlayer exoPlayer;

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

        setContentView(R.layout.activity_skit_details);


        /*---   LOCAL   ---*/
        currentSkitId = getIntent().getStringExtra("SkitId");
        currentSkitOwner = getIntent().getStringExtra("SkitOwner");


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        skitRef = db.getReference("Skits");
        skitLikesRef = db.getReference("SkitLikes");
        userRef = db.getReference("Users");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        title = (TextView)findViewById(R.id.currentSkitTitle);
        owner = (TextView)findViewById(R.id.currentSkitOwner);
        description = (TextView)findViewById(R.id.currentSkitDescription);
        likes = (TextView)findViewById(R.id.currentLikeCount);
        videoPlayer = (SimpleExoPlayerView)findViewById(R.id.videoPlayer);
        progressBar = (ProgressBar)findViewById(R.id.loadingProgress);
        likeBtn = (ImageView)findViewById(R.id.currentLikeBtn);


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

        activityName.setText("Skit Detail");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(SkitDetails.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   CURRENT USER   ---*/
        userRef.orderByChild("username").equalTo(currentSkitOwner).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    skitOwnerUid = child.getKey();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        owner.setText("@"+currentSkitOwner);
        owner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())){

                    if (skitOwnerUid.equals(currentUid)){

                        Intent myProfileIntent = new Intent(SkitDetails.this, MyProfile.class);
                        startActivity(myProfileIntent);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                    } else {

                        Intent otherUserProfileIntent = new Intent(SkitDetails.this, OtherUserProfile.class);
                        otherUserProfileIntent.putExtra("UserId", skitOwnerUid);
                        startActivity(otherUserProfileIntent);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                    }

                } else {

                    Common.showErrorDialog(SkitDetails.this, "No Internet Access !");

                }

            }
        });


        loadCurrentSkit();
    }

    private void loadCurrentSkit() {

        skitRef.child(currentSkitId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String theTitle = dataSnapshot.child("title").getValue().toString();
                String theDescription = dataSnapshot.child("description").getValue().toString();
                String theLink = dataSnapshot.child("mediaUrl").getValue().toString();

                title.setText(theTitle);
                description.setText(theDescription);

                try {

                    BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                    TrackSelector selector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                    exoPlayer = ExoPlayerFactory.newSimpleInstance(SkitDetails.this, selector);

                    Uri videoUri = Uri.parse(theLink);

                    DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("Campus Rush");
                    ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                    MediaSource videoSource = new ExtractorMediaSource(videoUri, dataSourceFactory, extractorsFactory, null, null);

                    videoPlayer.setPlayer(exoPlayer);
                    exoPlayer.prepare(videoSource);

                    if (exoPlayer.isLoading()){

                        progressBar.setVisibility(View.VISIBLE);

                    } else {

                        progressBar.setVisibility(View.GONE);

                    }

                    exoPlayer.setPlayWhenReady(true);

                } catch (Exception e){

                    Common.showErrorDialog(SkitDetails.this, "Error Occurred While Parsing Video !");

                }


                skitLikesRef.child(currentSkitId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        /*---   LIKES   ---*/
                        int countLike = (int) dataSnapshot.getChildrenCount();

                        likes.setText(String.valueOf(countLike));

                        if (dataSnapshot.child(currentUid).exists()){

                            likeBtn.setImageResource(R.drawable.liked_icon);

                            likeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    skitLikesRef.child(currentSkitId).child(currentUid).removeValue();
                                }
                            });

                        } else {

                            likeBtn.setImageResource(R.drawable.unliked_icon);

                            likeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    skitLikesRef.child(currentSkitId).child(currentUid).setValue("liked");

                                }
                            });

                        }

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

    }

    @Override
    public void onBackPressed() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer.stop();
        }
        finish();
    }
}
