package com.blackviking.campusrush.Plugins.GamersHub;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Common.GetTimeAgo;
import com.blackviking.campusrush.Home;
import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GamersHub extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference trendingRef, gameFeedRef, userRef;
    private RecyclerView trendingRecycler, gameFeedRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<TrendingGamesModel, TrendingGamesViewHolder> adapter;
    private FirebaseRecyclerAdapter<GameFeedModel, GameFeedViewHolder> gameFeedAdapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FloatingActionButton addGameFeed;
    private String currentUserType, currentUid;

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

        setContentView(R.layout.activity_gamers_hub);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   FIREBASE   ---*/
        trendingRef = db.getReference("GamersHubTrending");
        gameFeedRef = db.getReference("GameFeed");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users").child(currentUid);


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        trendingRecycler = (RecyclerView)findViewById(R.id.trendingGamesRecycler);
        gameFeedRecycler = (RecyclerView)findViewById(R.id.gameFeedRecycler);
        addGameFeed = (FloatingActionButton)findViewById(R.id.addGameFeed);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Gamers Hub");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(GamersHub.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        /*---   CURRENT USER   ---*/
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentUserType = dataSnapshot.child("userType").getValue().toString();

                /*---   CURRENT USER   ---*/
                if (currentUserType.equalsIgnoreCase("Admin")) {
                    addGameFeed.show();
                    addGameFeed.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent gameFeedDetailIntent = new Intent(GamersHub.this, AddGameFeed.class);
                            startActivity(gameFeedDetailIntent);
                            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                        }
                    });
                } else {

                    addGameFeed.hide();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        loadTrendingTitles();
        loadGameFeed();
    }

    private void loadGameFeed() {

        gameFeedRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        gameFeedRecycler.setLayoutManager(layoutManager);

        gameFeedAdapter = new FirebaseRecyclerAdapter<GameFeedModel, GameFeedViewHolder>(
                GameFeedModel.class,
                R.layout.game_feed_item,
                GameFeedViewHolder.class,
                gameFeedRef
        ) {
            @Override
            protected void populateViewHolder(final GameFeedViewHolder viewHolder, final GameFeedModel model, int position) {

                /*---   GET TIME AGO ALGORITHM   ---*/
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = model.getTimeStamp();
                final String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                viewHolder.gameFeedTitle.setText(model.getTitle());
                viewHolder.gameFeedTimeStamp.setText(lastSeenTime);


                if (!model.getImageLinkThumb().equalsIgnoreCase("")){

                    Picasso.with(getBaseContext())
                            .load(model.getImageLinkThumb())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(viewHolder.gameFeedImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(model.getImageLinkThumb())
                                            .placeholder(R.drawable.ic_loading_animation)
                                            .into(viewHolder.gameFeedImage);
                                }
                            });

                } else {

                    viewHolder.gameFeedImage.setImageResource(R.drawable.controller_medium);

                }


                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent detailsIntent = new Intent(GamersHub.this, GameFeedDetail.class);
                        detailsIntent.putExtra("FeedId", gameFeedAdapter.getRef(position).getKey());
                        startActivity(detailsIntent);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        gameFeedRecycler.setAdapter(gameFeedAdapter);
        gameFeedAdapter.notifyDataSetChanged();

    }

    private void loadTrendingTitles() {

        trendingRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        trendingRecycler.setLayoutManager(layoutManager);


        adapter = new FirebaseRecyclerAdapter<TrendingGamesModel, TrendingGamesViewHolder>(
                TrendingGamesModel.class,
                R.layout.games_trending_item,
                TrendingGamesViewHolder.class,
                trendingRef.orderByChild("rating")
        ) {
            @Override
            protected void populateViewHolder(final TrendingGamesViewHolder viewHolder, final TrendingGamesModel model, int position) {

                final String theName = adapter.getRef(position).getKey();

                viewHolder.gameRating.setText(model.getRating());
                viewHolder.gameName.setText(theName);

                if (!model.getImageUrl().equals("")){

                    Picasso.with(getBaseContext())
                            .load(model.getImageUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(viewHolder.gameImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(model.getImageUrl())
                                            .placeholder(R.drawable.ic_loading_animation)
                                            .into(viewHolder.gameImage);
                                }
                            });

                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent gameInfo = new Intent(GamersHub.this, GameInfo.class);
                        gameInfo.putExtra("GameId", theName);
                        startActivity(gameInfo);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        trendingRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }
}
