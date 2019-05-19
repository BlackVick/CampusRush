package com.blackviking.campusrush.Plugins.SkitCenter;

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

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SkitCenter extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FloatingActionButton skitManagement, addSkit;
    private RecyclerView skitRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<SkitModel, SkitViewHolder> adapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference skitRef, skitLikesRef, userRef;
    private String currentUid;
    private enum VolumeState {ON, OFF};
    private PlayerView videoSurfaceView;
    private SimpleExoPlayer videoPlayer;

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

        setContentView(R.layout.activity_skit_center);
        
        
        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        skitRef = db.getReference("Skits");
        skitLikesRef = db.getReference("SkitLikes");
        userRef = db.getReference("Users").child(currentUid);


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        skitRecycler = (RecyclerView)findViewById(R.id.skitRecycler);
        skitManagement = (FloatingActionButton)findViewById(R.id.skitManagement);
        addSkit = (FloatingActionButton)findViewById(R.id.addSkit);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Skit Center");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(SkitCenter.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   ADD SKIT   ---*/
        addSkit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addSkitIntent = new Intent(SkitCenter.this, AddNewSkit.class);
                startActivity(addSkitIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   CURRENT USER   ---*/
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userType = dataSnapshot.child("userType").getValue().toString();

                if (userType.equalsIgnoreCase("Admin")){

                    skitManagement.show();
                    skitManagement.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent managementIntent = new Intent(SkitCenter.this, SkitManagement.class);
                            startActivity(managementIntent);
                            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                        }
                    });

                } else {

                    skitManagement.hide();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        
        
        loadSkits();
        
    }

    private void loadSkits() {

        skitRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        skitRecycler.setLayoutManager(layoutManager);


        adapter = new FirebaseRecyclerAdapter<SkitModel, SkitViewHolder>(
                SkitModel.class,
                R.layout.skit_item,
                SkitViewHolder.class,
                skitRef.orderByChild("status").equalTo("Approved").limitToLast(50)
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

                    Picasso.with(getBaseContext())
                            .load(model.getThumbnail())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(viewHolder.thumbnail, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(model.getThumbnail())
                                            .placeholder(R.drawable.ic_loading_animation)
                                            .into(viewHolder.thumbnail);
                                }
                            });

                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent skitDetailsIntent = new Intent(SkitCenter.this, SkitDetails.class);
                        skitDetailsIntent.putExtra("SkitId", theId);
                        skitDetailsIntent.putExtra("SkitOwner", model.getOwner());
                        startActivity(skitDetailsIntent);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });


            }
        };
        skitRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
