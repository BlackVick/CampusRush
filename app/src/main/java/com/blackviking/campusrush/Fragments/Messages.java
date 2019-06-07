package com.blackviking.campusrush.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.Messaging.MessagesModel;
import com.blackviking.campusrush.Messaging.MessagesViewHolder;
import com.blackviking.campusrush.Messaging.Messaging;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class Messages extends Fragment {

    private RecyclerView messagesRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<MessagesModel, MessagesViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, messagesRef, messageListRef;
    private String currentUid;

    public Messages() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_messages, container, false);


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users");
        messageListRef = db.getReference("MessageList").child(currentUid);
        messagesRef = db.getReference("Messages").child(currentUid);


        /*---   WIDGET   ---*/
        messagesRecycler = (RecyclerView)v.findViewById(R.id.messagesRecycler);


        loadMessages();

        return v;
    }

    private void loadMessages() {

        messagesRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        messagesRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<MessagesModel, MessagesViewHolder>(
                MessagesModel.class,
                R.layout.message_user_layout,
                MessagesViewHolder.class,
                messageListRef.orderByChild("lastMessageTimestamp")
        ) {
            @Override
            protected void populateViewHolder(final MessagesViewHolder viewHolder, MessagesModel model, int position) {

                final String userKey = adapter.getRef(viewHolder.getAdapterPosition()).getKey();

                userRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userImagelnk = dataSnapshot.child("profilePictureThumb").getValue().toString();
                        final String serverUsername = dataSnapshot.child("username").getValue().toString();

                        viewHolder.username.setText("@"+serverUsername);

                        if (!userImagelnk.equalsIgnoreCase("")){

                            Picasso.with(getContext())
                                    .load(userImagelnk)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.profile)
                                    .into(viewHolder.userImage, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(getContext())
                                                    .load(userImagelnk)
                                                    .placeholder(R.drawable.profile)
                                                    .into(viewHolder.userImage);
                                        }
                                    });

                        }

                        userRef.removeEventListener(this);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                messagesRef.child(userKey)
                        .orderByChild("messageRead")
                        .equalTo("unread")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                int unreadMessageCount = (int) dataSnapshot.getChildrenCount();

                                if (unreadMessageCount > 0){

                                    viewHolder.unreadCount.setVisibility(View.VISIBLE);
                                    viewHolder.unreadCount.setText(String.valueOf(unreadMessageCount));

                                } else {

                                    viewHolder.unreadCount.setVisibility(View.GONE);

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                viewHolder.userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!userKey.equalsIgnoreCase("")){

                            Intent viewUserIntent = new Intent(getContext(), OtherUserProfile.class);
                            viewUserIntent.putExtra("UserId", userKey);
                            startActivity(viewUserIntent);
                            getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                        }

                    }
                });

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent messageIntent = new Intent(getContext(), Messaging.class);
                        messageIntent.putExtra("UserId", userKey);
                        startActivity(messageIntent);
                        getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        messagesRecycler.setAdapter(adapter);

    }

}
