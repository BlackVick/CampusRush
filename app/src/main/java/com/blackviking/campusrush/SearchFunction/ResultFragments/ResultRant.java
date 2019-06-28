package com.blackviking.campusrush.SearchFunction.ResultFragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.campusrush.CampusRant.CampusRant;
import com.blackviking.campusrush.CampusRant.RantRoom;
import com.blackviking.campusrush.CampusRant.RantTopicModel;
import com.blackviking.campusrush.CampusRant.RantTopicViewHolder;
import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResultRant extends Fragment {

    private RecyclerView rantTopicRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<RantTopicModel, RantTopicViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference topicRef;
    private RelativeLayout emptyData;
    private String searchParam;

    public ResultRant() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_result_rant, container, false);

        /*---   SEARCH PARAM   ---*/
        searchParam = Paper.book().read(Common.SEARCH_STRING);

        /*---   FIREBASE   ---*/
        topicRef = db.getReference("RantTopics");


        /*---   WIDGETS   ---*/
        rantTopicRecycler = (RecyclerView)v.findViewById(R.id.rantTopicRecycler);
        emptyData = (RelativeLayout)v.findViewById(R.id.emptyMaterialLayout);

        /*---   EMPTY CHECK   ---*/
        topicRef.orderByChild("name")
                .startAt(searchParam)
                .endAt(searchParam+"\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    emptyData.setVisibility(View.GONE);
                    loadRantTopics();

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

    private void loadRantTopics() {

        rantTopicRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rantTopicRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<RantTopicModel, RantTopicViewHolder>(
                RantTopicModel.class,
                R.layout.rant_topic_item,
                RantTopicViewHolder.class,
                topicRef.orderByChild("name")
                        .startAt(searchParam)
                        .endAt(searchParam+"\uf8ff")
        ) {
            @Override
            protected void populateViewHolder(RantTopicViewHolder viewHolder, final RantTopicModel model, int position) {

                viewHolder.topicName.setText("#"+model.getName());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent rantRoomIntent = new Intent(getContext(), RantRoom.class);
                        rantRoomIntent.putExtra("RantTopic", model.getName());
                        startActivity(rantRoomIntent);
                        getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        rantTopicRecycler.setAdapter(adapter);

    }

}
