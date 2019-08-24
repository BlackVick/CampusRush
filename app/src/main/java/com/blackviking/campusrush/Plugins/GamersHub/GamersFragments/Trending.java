package com.blackviking.campusrush.Plugins.GamersHub.GamersFragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.Plugins.GamersHub.GameInfo;
import com.blackviking.campusrush.Plugins.GamersHub.GamersHub;
import com.blackviking.campusrush.Plugins.GamersHub.TrendingGamesModel;
import com.blackviking.campusrush.Plugins.GamersHub.TrendingGamesViewHolder;
import com.blackviking.campusrush.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class Trending extends Fragment {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference trendingRef;
    private RecyclerView trendingRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<TrendingGamesModel, TrendingGamesViewHolder> adapter;
    private RelativeLayout trendingNav;
    private ImageView pcGames, xboxGames, psGames, switchGames, stadiaGames;

    public Trending() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_trending, container, false);


        /*---   FIREBASE   ---*/
        trendingRef = db.getReference("GamersHubTrending");


        /*---   WIDGETS   ---*/
        trendingRecycler = (RecyclerView)v.findViewById(R.id.trendingGamesRecycler);
        trendingNav = (RelativeLayout)v.findViewById(R.id.trendingNav);
        pcGames = (ImageView)v.findViewById(R.id.pcGames);
        xboxGames = (ImageView)v.findViewById(R.id.xboxGames);
        psGames = (ImageView)v.findViewById(R.id.psGames);
        switchGames = (ImageView)v.findViewById(R.id.switchGames);
        stadiaGames = (ImageView)v.findViewById(R.id.stadiaGames);


        pcGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPlatformGames("pcGames");
            }
        });

        xboxGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPlatformGames("xboxGames");
            }
        });

        psGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPlatformGames("psGames");
            }
        });

        switchGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPlatformGames("switchGames");
            }
        });

        stadiaGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPlatformGames("stadiaGames");
            }
        });


        loadTrendingTitles();
        return v;
    }

    private void loadTrendingTitles() {

        trendingRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        trendingRecycler.setLayoutManager(layoutManager);


        trendingRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy < 0 && trendingNav.getVisibility() == View.GONE) {

                    trendingNav.setVisibility(View.VISIBLE);
                }
                else if(dy > 0 && trendingNav.getVisibility() == View.VISIBLE) {
                    trendingNav.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                /*if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    theNavLayout.setVisibility(View.VISIBLE);*/
                super.onScrollStateChanged(recyclerView, newState);
            }
        });


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
                viewHolder.gameGenre.setText(model.getGenre());
                viewHolder.gamePlatforms.setText(model.getPlatforms());
                viewHolder.gameReleaseDate.setText(model.getReleaseDate());

                if (!model.getImageUrl().equals("")){

                    Picasso.with(getContext())
                            .load(model.getImageUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.image_placeholders)
                            .into(viewHolder.gameImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getContext())
                                            .load(model.getImageUrl())
                                            .placeholder(R.drawable.image_placeholders)
                                            .into(viewHolder.gameImage);
                                }
                            });

                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent gameInfo = new Intent(getActivity(), GameInfo.class);
                        gameInfo.putExtra("GameId", theName);
                        startActivity(gameInfo);
                        getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        trendingRecycler.setAdapter(adapter);

    }

    private void loadPlatformGames(String platform) {

        trendingRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        trendingRecycler.setLayoutManager(layoutManager);


        trendingRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy < 0 && trendingNav.getVisibility() == View.GONE) {

                    trendingNav.setVisibility(View.VISIBLE);
                }
                else if(dy > 0 && trendingNav.getVisibility() == View.VISIBLE) {
                    trendingNav.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                /*if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    theNavLayout.setVisibility(View.VISIBLE);*/
                super.onScrollStateChanged(recyclerView, newState);
            }
        });


        adapter = new FirebaseRecyclerAdapter<TrendingGamesModel, TrendingGamesViewHolder>(
                TrendingGamesModel.class,
                R.layout.games_trending_item,
                TrendingGamesViewHolder.class,
                trendingRef.orderByChild(platform)
                .equalTo("true")
        ) {
            @Override
            protected void populateViewHolder(final TrendingGamesViewHolder viewHolder, final TrendingGamesModel model, int position) {

                final String theName = adapter.getRef(position).getKey();

                viewHolder.gameRating.setText(model.getRating());
                viewHolder.gameName.setText(theName);
                viewHolder.gameGenre.setText(model.getGenre());
                viewHolder.gamePlatforms.setText(model.getPlatforms());
                viewHolder.gameReleaseDate.setText(model.getReleaseDate());

                if (!model.getImageUrl().equals("")){

                    Picasso.with(getContext())
                            .load(model.getImageUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.image_placeholders)
                            .into(viewHolder.gameImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getContext())
                                            .load(model.getImageUrl())
                                            .placeholder(R.drawable.image_placeholders)
                                            .into(viewHolder.gameImage);
                                }
                            });

                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent gameInfo = new Intent(getActivity(), GameInfo.class);
                        gameInfo.putExtra("GameId", theName);
                        startActivity(gameInfo);
                        getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        trendingRecycler.setAdapter(adapter);

    }
}
