package com.blackviking.campusrush.CampusBusiness;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.FeedDetails;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.Profile.OtherUserProfile;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
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

public class AdDetails extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference promoterRef, businessProfileRef;
    private String currentBusinessId, currentAdId, currentUid;

    private ImageView adImage;
    private TextView adDescription, adCompany, adContact;

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

        setContentView(R.layout.activity_ad_details);


        /*---   LOCAL   ---*/
        currentBusinessId = getIntent().getStringExtra("AdSenderId");
        currentAdId = getIntent().getStringExtra("AdId");


        /*---   FIREBASE   ---*/
        promoterRef = db.getReference("PromotedAds");
        businessProfileRef = db.getReference("BusinessProfile");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        adImage = (ImageView)findViewById(R.id.adDetailImage);
        adDescription = (TextView)findViewById(R.id.adDetailDescription);
        adCompany = (TextView)findViewById(R.id.adDetailBusinessName);
        adContact = (TextView)findViewById(R.id.adDetailBusinessPhone);



        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Advert Detail");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(AdDetails.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        loadAd();


    }

    private void loadAd() {

        promoterRef.child(currentBusinessId)
                .child(currentAdId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String theAdImage = dataSnapshot.child("imageUrl").getValue().toString();
                final String theAdImageThumb = dataSnapshot.child("imageThumbUrl").getValue().toString();
                final String theAdDescription = dataSnapshot.child("update").getValue().toString();
                final String theAdCompany = dataSnapshot.child("business").getValue().toString();
                final String theAdContact = dataSnapshot.child("contact").getValue().toString();

                if (!theAdImageThumb.equalsIgnoreCase("")){

                    adImage.setVisibility(View.VISIBLE);
                    Picasso.with(getBaseContext())
                            .load(theAdImageThumb) // thumbnail url goes here
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(adImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Picasso.with(getBaseContext())
                                            .load(theAdImage) // image url goes here
                                            .placeholder(adImage.getDrawable())
                                            .into(adImage);
                                }
                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(theAdImageThumb) // thumbnail url goes here
                                            .into(adImage, new Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    Picasso.with(getBaseContext())
                                                            .load(theAdImage) // image url goes here
                                                            .placeholder(adImage.getDrawable())
                                                            .into(adImage);
                                                }
                                                @Override
                                                public void onError() {

                                                }
                                            });
                                }
                            });

                } else {

                    adImage.setVisibility(View.GONE);

                }

                if (!theAdDescription.equalsIgnoreCase("")){

                    adDescription.setVisibility(View.VISIBLE);
                    adDescription.setText(theAdDescription);

                } else {

                    adDescription.setVisibility(View.GONE);

                }

                adCompany.setText(theAdCompany);
                adCompany.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (currentBusinessId.equals(currentUid)) {

                            Intent posterProfile = new Intent(AdDetails.this, MyProfile.class);
                            startActivity(posterProfile);
                            overridePendingTransition(R.anim.slide_left, R.anim.slide_out);

                        } else {

                            Intent posterProfile = new Intent(AdDetails.this, OtherUserProfile.class);
                            posterProfile.putExtra("UserId", currentBusinessId);
                            startActivity(posterProfile);
                            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                        }

                    }
                });

                adContact.setText(theAdContact);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
