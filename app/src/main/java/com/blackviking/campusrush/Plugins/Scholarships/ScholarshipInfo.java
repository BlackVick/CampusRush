package com.blackviking.campusrush.Plugins.Scholarships;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ScholarshipInfo extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private TextView title, location, program, eligibility, commencement, deadline, info;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference scholarshipRef;
    private String currentScholarshipId;

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

        setContentView(R.layout.activity_scholarship_info);


        /*---   INTENT   ---*/
        currentScholarshipId = getIntent().getStringExtra("ScholarshipId");


        /*---   FIREBASE   ---*/
        scholarshipRef = db.getReference("Scholarships");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        title = (TextView)findViewById(R.id.currentScholarshipTitle);
        location = (TextView)findViewById(R.id.currentScholarshipLocation);
        program = (TextView)findViewById(R.id.currentScholarshipProgram);
        eligibility = (TextView)findViewById(R.id.currentScholarshipEligibility);
        commencement = (TextView)findViewById(R.id.currentScholarshipCommencement);
        deadline = (TextView)findViewById(R.id.currentScholarshipDeadline);
        info = (TextView)findViewById(R.id.currentScholarshipInformation);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Scholarship");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(ScholarshipInfo.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        loadCurrentScholarship();
    }

    private void loadCurrentScholarship() {

        scholarshipRef.child(currentScholarshipId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String theTitle = dataSnapshot.child("title").getValue().toString();
                String theLocation = dataSnapshot.child("location").getValue().toString();
                String theProgram = dataSnapshot.child("program").getValue().toString();
                String theEligibility = dataSnapshot.child("eligibility").getValue().toString();
                String theCommencement = dataSnapshot.child("commencement").getValue().toString();
                String theDeadline = dataSnapshot.child("deadline").getValue().toString();
                String theInfo = dataSnapshot.child("info").getValue().toString();


                title.setText(theTitle);
                location.setText(theLocation);
                program.setText(theProgram);
                eligibility.setText(theEligibility);
                commencement.setText(theCommencement);
                deadline.setText(theDeadline);
                info.setText(theInfo);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
