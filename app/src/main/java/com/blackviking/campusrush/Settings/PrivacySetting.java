package com.blackviking.campusrush.Settings;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PrivacySetting extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private CheckBox privacyToggle;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private String currentUid;

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

        setContentView(R.layout.activity_privacy_setting);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users").child(currentUid);


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        privacyToggle = (CheckBox)findViewById(R.id.privacyToggle);


        /*---   CURRENT USER   ---*/
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String privacyState = dataSnapshot.child("privacy").getValue().toString();

                if (privacyState.equalsIgnoreCase("public")){

                    privacyToggle.setChecked(false);

                } else if (privacyState.equalsIgnoreCase("classified")) {

                    privacyToggle.setChecked(true);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Privacy");
        helpActivity.setVisibility(View.GONE);


        /*---   TOGGLE   ---*/
        privacyToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){

                    userRef.child("privacy").setValue("classified");

                } else {

                    userRef.child("privacy").setValue("public");

                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
