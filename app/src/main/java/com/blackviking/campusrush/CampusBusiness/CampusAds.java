package com.blackviking.campusrush.CampusBusiness;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CampusAds extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, promoterRef;
    private String currentUid, userType;
    private RecyclerView adRecycler;
    private LinearLayoutManager layoutManager;
    private RelativeLayout notBusinessAccount;
    private ImageView createBusinessProfile;
    private FloatingActionButton promoteAd;

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
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        adRecycler = (RecyclerView)findViewById(R.id.adRecycler);
        notBusinessAccount = (RelativeLayout)findViewById(R.id.notBusinessAccount);
        createBusinessProfile = (ImageView)findViewById(R.id.createBusinessProfile);
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

            userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    userType = dataSnapshot.child("userType").getValue().toString();

                    if (userType.equalsIgnoreCase("Business") || userType.equalsIgnoreCase("Admin")) {

                        notBusinessAccount.setVisibility(View.GONE);
                        adRecycler.setVisibility(View.VISIBLE);
                        promoteAd.show();

                        loadPromotedAds();

                    } else {

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

            Common.showErrorDialog(this, "No Internet Access");
            finish();

        }

    }

    private void loadPromotedAds() {

        adRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        adRecycler.setLayoutManager(layoutManager);


        promoteAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createAdIntent = new Intent(CampusAds.this, CreateAd.class);
                startActivity(createAdIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

    }
}
