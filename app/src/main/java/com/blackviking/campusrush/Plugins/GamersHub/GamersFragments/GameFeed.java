package com.blackviking.campusrush.Plugins.GamersHub.GamersFragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.CampusRant.CampusRant;
import com.blackviking.campusrush.CampusRant.RantRoom;
import com.blackviking.campusrush.Common.GetTimeAgo;
import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.Plugins.GamersHub.AddGameFeed;
import com.blackviking.campusrush.Plugins.GamersHub.GameFeedDetail;
import com.blackviking.campusrush.Plugins.GamersHub.GameFeedModel;
import com.blackviking.campusrush.Plugins.GamersHub.GameFeedViewHolder;
import com.blackviking.campusrush.Plugins.GamersHub.GamersHub;
import com.blackviking.campusrush.Plugins.GamersHub.TrendingGamesModel;
import com.blackviking.campusrush.Plugins.GamersHub.TrendingGamesViewHolder;
import com.blackviking.campusrush.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class GameFeed extends Fragment {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference gameFeedRef;
    private RecyclerView gameFeedRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<GameFeedModel, GameFeedViewHolder> gameFeedAdapter;
    private FloatingActionButton addGameFeed, campusGamersChat;

    public GameFeed() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_game_feed, container, false);


        /*---   FIREBASE   ---*/
        gameFeedRef = db.getReference("GameFeed");


        /*---   WIDGETS   ---*/
        gameFeedRecycler = (RecyclerView)v.findViewById(R.id.gameFeedRecycler);
        addGameFeed = (FloatingActionButton)v.findViewById(R.id.addGameFeed);
        campusGamersChat = (FloatingActionButton)v.findViewById(R.id.campusGamersChat);


        addGameFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameFeedDetailIntent = new Intent(getActivity(), AddGameFeed.class);
                startActivity(gameFeedDetailIntent);
                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        campusGamersChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rantRoomIntent = new Intent(getActivity(), RantRoom.class);
                rantRoomIntent.putExtra("RantTopic", "campusrushgamers");
                startActivity(rantRoomIntent);
                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        loadGameFeed();
        return v;
    }

    private void loadGameFeed() {

        gameFeedRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        gameFeedRecycler.setLayoutManager(layoutManager);

        gameFeedAdapter = new FirebaseRecyclerAdapter<GameFeedModel, GameFeedViewHolder>(
                GameFeedModel.class,
                R.layout.game_feed_item,
                GameFeedViewHolder.class,
                gameFeedRef.limitToLast(35)
        ) {
            @Override
            protected void populateViewHolder(final GameFeedViewHolder viewHolder, final GameFeedModel model, int position) {

                /*---   GET TIME AGO ALGORITHM   ---*/
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = model.getTimeStamp();
                final String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getContext());

                viewHolder.gameFeedTitle.setText(model.getTitle());
                viewHolder.gameFeedTimeStamp.setText(lastSeenTime);


                if (!model.getImageLinkThumb().equalsIgnoreCase("")){

                    Picasso.with(getContext())
                            .load(model.getImageLinkThumb())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.image_placeholders)
                            .into(viewHolder.gameFeedImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getContext())
                                            .load(model.getImageLinkThumb())
                                            .placeholder(R.drawable.image_placeholders)
                                            .into(viewHolder.gameFeedImage);
                                }
                            });

                } else {

                    viewHolder.gameFeedImage.setImageResource(R.drawable.controller_medium);

                }


                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent detailsIntent = new Intent(getActivity(), GameFeedDetail.class);
                        detailsIntent.putExtra("GameFeedId", gameFeedAdapter.getRef(position).getKey());
                        startActivity(detailsIntent);
                        getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        gameFeedRecycler.setAdapter(gameFeedAdapter);

    }
}
