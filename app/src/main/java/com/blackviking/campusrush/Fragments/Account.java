package com.blackviking.campusrush.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.AccountSettings;
import com.blackviking.campusrush.Settings.Settings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class Account extends Fragment {

    private CircleImageView myProfileImage;
    private TextView accountUsername, accountFullName, profileSetting, accountSetting;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef;
    private String currentUid;
    private RecyclerView myTimelineRecycler;


    public Account() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users").child(currentUid);


        /*---   WIDGETS   ---*/
        myProfileImage = (CircleImageView)v.findViewById(R.id.profileImage);
        accountUsername = (TextView)v.findViewById(R.id.accountUsername);
        accountFullName = (TextView)v.findViewById(R.id.accountFullName);
        accountSetting = (TextView)v.findViewById(R.id.accountSetting);
        profileSetting = (TextView)v.findViewById(R.id.profileSetting);
        myTimelineRecycler = (RecyclerView)v.findViewById(R.id.timelineRecycler);


        /*---   CURRENT USER   ---*/
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String theUsername = dataSnapshot.child("username").getValue().toString();
                String theFullName = dataSnapshot.child("lastName").getValue().toString()
                        + " " + dataSnapshot.child("firstName").getValue().toString();
                final String theImage = dataSnapshot.child("profilePictureThumb").getValue().toString();


                accountUsername.setText("@"+theUsername);
                accountFullName.setText(theFullName);

                if (!theImage.equalsIgnoreCase("")) {

                    Picasso.with(getContext()).load(theImage).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.profile).into(myProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(getContext()).load(theImage)
                                    .placeholder(R.drawable.profile).into(myProfileImage);
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   PROFILE SETTING   ---*/
        profileSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        /*---   ACCOUNT SETTING   ---*/
        accountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accountSettingIntent = new Intent(getContext(), AccountSettings.class);
                startActivity(accountSettingIntent);
                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        return v;
    }

}
