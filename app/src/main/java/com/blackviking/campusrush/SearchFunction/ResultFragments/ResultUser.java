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
import com.blackviking.campusrush.Messaging.MessagesModel;
import com.blackviking.campusrush.Messaging.MessagesViewHolder;
import com.blackviking.campusrush.Messaging.Messaging;
import com.blackviking.campusrush.Model.UserModel;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.Profile.OtherUserProfile;
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
public class ResultUser extends Fragment {

    private RelativeLayout emptyData;
    private RecyclerView messagesRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<UserModel, MessagesViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef;
    private String currentUid;
    private String searchParam;

    public ResultUser() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_result_user, container, false);

        /*---   SEARCH PARAM   ---*/
        searchParam = Paper.book().read(Common.SEARCH_STRING);

        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users");

        /*---   WIDGET   ---*/
        emptyData = (RelativeLayout)v.findViewById(R.id.emptyMaterialLayout);
        messagesRecycler = (RecyclerView)v.findViewById(R.id.messagesRecycler);

        /*---   EMPTY CHECK   ---*/
        userRef.orderByChild("username")
                .startAt(searchParam)
                .endAt(searchParam+"\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    emptyData.setVisibility(View.GONE);
                    loadUsers();

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

    private void loadUsers() {
        messagesRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        messagesRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<UserModel, MessagesViewHolder>(
                UserModel.class,
                R.layout.message_user_layout,
                MessagesViewHolder.class,
                userRef.orderByChild("username")
                        .startAt(searchParam)
                        .endAt(searchParam+"\uf8ff")
        ) {
            @Override
            protected void populateViewHolder(final MessagesViewHolder viewHolder, final UserModel model, int position) {

                final String theKey = adapter.getRef(viewHolder.getAdapterPosition()).getKey();

                viewHolder.username.setText("@"+model.getUsername());

                if (!model.getProfilePictureThumb().equalsIgnoreCase("")){

                    Picasso.with(getContext())
                            .load(model.getProfilePictureThumb())
                            .placeholder(R.drawable.profile)
                            .into(viewHolder.userImage);

                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        if (theKey.equalsIgnoreCase(currentUid)){

                            Intent viewUserIntent = new Intent(getContext(), MyProfile.class);
                            startActivity(viewUserIntent);
                            getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                        } else {

                            Intent viewUserIntent = new Intent(getContext(), OtherUserProfile.class);
                            viewUserIntent.putExtra("UserId", theKey);
                            startActivity(viewUserIntent);
                            getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                        }

                    }
                });

            }
        };
        messagesRecycler.setAdapter(adapter);
    }

}
