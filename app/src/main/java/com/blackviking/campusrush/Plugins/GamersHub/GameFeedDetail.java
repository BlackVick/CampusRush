package com.blackviking.campusrush.Plugins.GamersHub;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Common.GetTimeAgo;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GameFeedDetail extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference gameFeedRef;
    private String currentFeedId;
    private TextView feedTitle, feedDescription, feedTimeStamp;
    private ImageView feedImage;

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

        setContentView(R.layout.activity_game_feed_detail);


        /*---   INTENT   ---*/
        currentFeedId = getIntent().getStringExtra("FeedId");


        /*---   FIREBASE   ---*/
        gameFeedRef = db.getReference("GameFeed");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        feedImage = (ImageView)findViewById(R.id.gameFeedDetailImage);
        feedTitle = (TextView)findViewById(R.id.gameFeedDetailTitle);
        feedDescription = (TextView)findViewById(R.id.gameFeedDetailDescription);
        feedTimeStamp = (TextView)findViewById(R.id.gameFeedDetailTimeStamp);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Game Feed");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(GameFeedDetail.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        loadCurrentFeed();
    }

    private void loadCurrentFeed() {

        gameFeedRef.child(currentFeedId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String theTitle = dataSnapshot.child("title").getValue().toString();
                final String theImageLink = dataSnapshot.child("imageLink").getValue().toString();
                String theImageThumbLink = dataSnapshot.child("imageLinkThumb").getValue().toString();
                String theDescription = dataSnapshot.child("description").getValue().toString();
                String theTimeStamp = dataSnapshot.child("timeStamp").getValue().toString();


                /*---   GET TIME AGO ALGORITHM   ---*/
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = Long.parseLong(theTimeStamp);
                final String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());


                feedTitle.setText(theTitle);
                feedDescription.setText(theDescription);
                feedTimeStamp.setText(lastSeenTime);

                if (!theImageThumbLink.equalsIgnoreCase("")){

                    Picasso.with(getBaseContext())
                            .load(theImageThumbLink) // thumbnail url goes here
                            .into(feedImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Picasso.with(getBaseContext())
                                            .load(theImageLink) // image url goes here
                                            .placeholder(feedImage.getDrawable())
                                            .into(feedImage);
                                }
                                @Override
                                public void onError() {

                                }
                            });

                } else {

                    feedImage.setImageResource(R.drawable.controller_medium);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
