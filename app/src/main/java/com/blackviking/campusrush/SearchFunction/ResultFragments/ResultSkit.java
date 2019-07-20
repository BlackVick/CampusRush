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
import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.Plugins.SkitCenter.SkitCenter;
import com.blackviking.campusrush.Plugins.SkitCenter.SkitDetails;
import com.blackviking.campusrush.Plugins.SkitCenter.SkitModel;
import com.blackviking.campusrush.Plugins.SkitCenter.SkitViewHolder;
import com.blackviking.campusrush.R;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class ResultSkit extends Fragment {

    private RelativeLayout emptyData;
    private RecyclerView skitRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<SkitModel, SkitViewHolder> adapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference skitRef, skitLikesRef, userRef;
    private String currentUid;
    private String searchParam;

    public ResultSkit() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_result_skit, container, false);

        /*---   SEARCH PARAM   ---*/
        searchParam = Paper.book().read(Common.SEARCH_STRING);

        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        skitRef = db.getReference("Skits");
        skitLikesRef = db.getReference("SkitLikes");
        userRef = db.getReference("Users").child(currentUid);


        /*---   WIDGET   ---*/
        emptyData = (RelativeLayout)v.findViewById(R.id.emptyMaterialLayout);
        skitRecycler = (RecyclerView)v.findViewById(R.id.skitRecycler);


        /*---   EMPTY CHECK   ---*/
        skitRef.orderByChild("title")
                .startAt(searchParam)
                .endAt(searchParam+"\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    emptyData.setVisibility(View.GONE);
                    loadSkits();

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

    private void loadSkits() {

        skitRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        skitRecycler.setLayoutManager(layoutManager);


        adapter = new FirebaseRecyclerAdapter<SkitModel, SkitViewHolder>(
                SkitModel.class,
                R.layout.skit_item,
                SkitViewHolder.class,
                skitRef.orderByChild("title")
                        .startAt(searchParam)
                        .endAt(searchParam+"\uf8ff").limitToLast(50)
        ) {
            @Override
            protected void populateViewHolder(final SkitViewHolder viewHolder, final SkitModel model, int position) {

                final String theId = adapter.getRef(position).getKey();

                viewHolder.skitTitle.setText(model.getTitle());
                viewHolder.skitOwner.setText("@"+model.getOwner());

                skitLikesRef.child(theId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        /*---   LIKES   ---*/
                        int countLike = (int) dataSnapshot.getChildrenCount();

                        viewHolder.skitLike.setText(String.valueOf(countLike));

                        if (dataSnapshot.child(currentUid).exists()){

                            viewHolder.likeButton.setImageResource(R.drawable.liked_icon);

                            viewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    skitLikesRef.child(theId).child(currentUid).removeValue();
                                }
                            });

                        } else {

                            viewHolder.likeButton.setImageResource(R.drawable.unliked_icon);

                            viewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    skitLikesRef.child(theId).child(currentUid).setValue("liked");

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                if (!model.getThumbnail().equals("")){

                    Picasso.with(getContext())
                            .load(model.getThumbnail())
                            .placeholder(R.drawable.ic_new_placeholder_icon)
                            .into(viewHolder.thumbnail);

                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent skitDetailsIntent = new Intent(getContext(), SkitDetails.class);
                        skitDetailsIntent.putExtra("SkitId", theId);
                        skitDetailsIntent.putExtra("SkitOwner", model.getOwner());
                        startActivity(skitDetailsIntent);
                        getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });


            }
        };
        skitRecycler.setAdapter(adapter);

    }
}
