package com.blackviking.campusrush.CampusBusiness;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CampusAds extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, promoterRef, subscriptionRef, businessProfileRef, feedRef;
    private String currentUid, userType, subscriptionStatus = "";
    private RecyclerView adRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<AdModel, AdViewHolder> adapter;
    private RelativeLayout notBusinessAccount, expiredBusinessAccount;
    private ImageView createBusinessProfile, renewSubscription;
    private FloatingActionButton promoteAd;
    private boolean isProfileCreated = false;
    private AdModel currentAd;

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

        setContentView(R.layout.activity_campus_ads);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        promoterRef = db.getReference("PromotedAds");
        feedRef = db.getReference("Feed");
        businessProfileRef = db.getReference("BusinessProfile");
        subscriptionRef = db.getReference("BusinessAccountSubscriptions");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        adRecycler = (RecyclerView)findViewById(R.id.adRecycler);
        notBusinessAccount = (RelativeLayout)findViewById(R.id.notBusinessAccount);
        expiredBusinessAccount = (RelativeLayout)findViewById(R.id.expiredBusinessAccount);
        createBusinessProfile = (ImageView)findViewById(R.id.createBusinessProfile);
        renewSubscription = (ImageView)findViewById(R.id.renewSubscription);
        promoteAd = (FloatingActionButton)findViewById(R.id.promoteAd);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Campus Ads");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(CampusAds.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   CURRENT USER   ---*/
        if (Common.isConnectedToInternet(getBaseContext())) {

            /*---   SUBSCRIPTION LOT   ---*/
            subscriptionRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(currentUid).exists()){

                        subscriptionStatus = dataSnapshot.child(currentUid).child("subscriptionStatus").getValue().toString();

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            businessProfileRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(currentUid).exists()){

                        isProfileCreated = true;

                    } else {

                        isProfileCreated = false;

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    userType = dataSnapshot.child("userType").getValue().toString();

                    if (userType.equalsIgnoreCase("Business") || userType.equalsIgnoreCase("Admin")) {

                        if (isProfileCreated) {

                            if (subscriptionStatus.equalsIgnoreCase("Active")) {

                                notBusinessAccount.setVisibility(View.GONE);
                                expiredBusinessAccount.setVisibility(View.GONE);
                                adRecycler.setVisibility(View.VISIBLE);
                                promoteAd.show();

                                loadPromotedAds();

                            } else {

                                notBusinessAccount.setVisibility(View.GONE);
                                expiredBusinessAccount.setVisibility(View.VISIBLE);
                                adRecycler.setVisibility(View.GONE);


                                /*---   RENEW SUBSCRIPTION   ---*/
                                renewSubscription.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent renewSubIntent = new Intent(CampusAds.this, RenewSubscription.class);
                                        startActivity(renewSubIntent);
                                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                                    }
                                });

                            }

                        } else {

                            notBusinessAccount.setVisibility(View.VISIBLE);
                            expiredBusinessAccount.setVisibility(View.GONE);
                            adRecycler.setVisibility(View.GONE);
                            promoteAd.hide();

                            /*---   CREATE BUSINESS PROFILE*/
                            createBusinessProfile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent setBusinessUp = new Intent(CampusAds.this, SetBusinessAccount.class);
                                    startActivity(setBusinessUp);
                                    overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                                }
                            });

                        }

                    } else {

                        expiredBusinessAccount.setVisibility(View.GONE);
                        notBusinessAccount.setVisibility(View.VISIBLE);
                        adRecycler.setVisibility(View.GONE);
                        promoteAd.hide();

                        /*---   CREATE BUSINESS PROFILE*/
                        createBusinessProfile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent setBusinessUp = new Intent(CampusAds.this, SetBusinessAccount.class);
                                startActivity(setBusinessUp);
                                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                            }
                        });

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {

            finish();

        }

    }

    private void loadPromotedAds() {

        adRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        adRecycler.setLayoutManager(layoutManager);


        adapter = new FirebaseRecyclerAdapter<AdModel, AdViewHolder>(
                AdModel.class,
                R.layout.ad_item,
                AdViewHolder.class,
                promoterRef.child(currentUid)
        ) {
            @Override
            protected void populateViewHolder(final AdViewHolder viewHolder, AdModel model, final int position) {

                if (!model.getImageThumbUrl().equalsIgnoreCase("")){

                    viewHolder.adImage.setVisibility(View.VISIBLE);

                    Picasso.with(getBaseContext())
                            .load(model.getImageThumbUrl())
                            .placeholder(R.drawable.image_placeholders)
                            .into(viewHolder.adImage);

                } else {

                    viewHolder.adImage.setVisibility(View.GONE);
                    viewHolder.adText.setMaxLines(6);
                }

                if (!model.getUpdate().equalsIgnoreCase("")){

                    viewHolder.adText.setVisibility(View.VISIBLE);
                    viewHolder.adText.setText(model.getUpdate());

                } else {

                    viewHolder.adText.setVisibility(View.GONE);

                }

                viewHolder.adPromote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialog(adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                    }
                });

            }
        };
        adRecycler.setAdapter(adapter);


        promoteAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createAdIntent = new Intent(CampusAds.this, CreateAd.class);
                startActivity(createAdIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

    }

    private void openDialog(final String adId) {

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Attention !")
                .setIcon(R.drawable.ic_attention_red)
                .setMessage("You Want To Promote This Ad Again?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        feedRef.child(adId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    currentAd = dataSnapshot.getValue(AdModel.class);

                                    DatabaseReference pushRef = promoterRef.push();
                                    final String pushId = pushRef.getKey();

                                    promoterRef.child(currentUid)
                                            .child(adId)
                                            .removeValue();

                                    feedRef.child(adId).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    promoterRef.child(currentUid)
                                                            .child(pushId)
                                                            .setValue(currentAd);

                                                    feedRef.child(pushId)
                                                            .setValue(currentAd).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                                }
                                            });
                                } else {

                                    DatabaseReference pushRef = promoterRef.push();
                                    final String pushId = pushRef.getKey();

                                    promoterRef.child(currentUid)
                                            .child(adId)
                                            .removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    promoterRef.child(currentUid)
                                                            .child(pushId)
                                                            .setValue(currentAd);

                                                    feedRef.child(pushId)
                                                            .setValue(currentAd).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                                }
                                            });

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
