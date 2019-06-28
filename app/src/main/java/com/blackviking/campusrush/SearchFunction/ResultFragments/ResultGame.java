package com.blackviking.campusrush.SearchFunction.ResultFragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Common.GetTimeAgo;
import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.Plugins.GamersHub.GameFeedDetail;
import com.blackviking.campusrush.Plugins.GamersHub.GameFeedModel;
import com.blackviking.campusrush.Plugins.GamersHub.GameFeedViewHolder;
import com.blackviking.campusrush.Plugins.GamersHub.GamersHub;
import com.blackviking.campusrush.Plugins.GamersHub.TrendingGamesModel;
import com.blackviking.campusrush.Plugins.GamersHub.TrendingGamesViewHolder;
import com.blackviking.campusrush.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResultGame extends Fragment {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference gameFeedRef;
    private RecyclerView gameFeedRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<GameFeedModel, GameFeedViewHolder> gameFeedAdapter;
    private RelativeLayout emptyData;
    private String searchParam;

    public ResultGame() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_result_game, container, false);

        /*---   SEARCH PARAM   ---*/
        searchParam = Paper.book().read(Common.SEARCH_STRING);

        /*---   FIREBASE   ---*/
        gameFeedRef = db.getReference("GameFeed");

        /*---   WIDGET   ---*/
        gameFeedRecycler = (RecyclerView)v.findViewById(R.id.gameFeedRecycler);
        emptyData = (RelativeLayout)v.findViewById(R.id.emptyMaterialLayout);


        /*---   EMPTY CHECK   ---*/
        gameFeedRef.orderByChild("title")
                .startAt(searchParam)
                .endAt(searchParam+"\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    emptyData.setVisibility(View.GONE);
                    loadGameFeed();

                } else {

                    emptyData.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
                gameFeedRef.orderByChild("title")
                        .startAt(searchParam)
                        .endAt(searchParam+"\uf8ff").limitToLast(35)
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
                            .placeholder(R.drawable.image_placeholders)
                            .into(viewHolder.gameFeedImage);

                } else {

                    viewHolder.gameFeedImage.setImageResource(R.drawable.controller_medium);

                }


                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent detailsIntent = new Intent(getContext(), GameFeedDetail.class);
                        detailsIntent.putExtra("GameFeedId", gameFeedAdapter.getRef(viewHolder.getAdapterPosition()).getKey());
                        startActivity(detailsIntent);
                        getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        gameFeedRecycler.setAdapter(gameFeedAdapter);

    }

}
