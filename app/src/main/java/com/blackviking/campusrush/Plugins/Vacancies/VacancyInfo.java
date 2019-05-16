package com.blackviking.campusrush.Plugins.Vacancies;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Plugins.Scholarships.ScholarshipInfo;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class VacancyInfo extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private TextView title, location, company, type, reference, info;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference vacancyRef;
    private String currentJobId;

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

        setContentView(R.layout.activity_vacancy_info);


        /*---   INTENT   ---*/
        currentJobId = getIntent().getStringExtra("VacancyId");


        /*---   FIREBASE   ---*/
        vacancyRef = db.getReference("Vacancies");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        title = (TextView)findViewById(R.id.currentJobTitle);
        location = (TextView)findViewById(R.id.currentJobLocation);
        company = (TextView)findViewById(R.id.currentJobCompany);
        reference = (TextView)findViewById(R.id.currentJobReference);
        type = (TextView)findViewById(R.id.currentJobType);
        info = (TextView)findViewById(R.id.currentJobInfo);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Vacancy");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(VacancyInfo.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        loadCurrentJob();
    }

    private void loadCurrentJob() {

        vacancyRef.child(currentJobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String theTitle = dataSnapshot.child("title").getValue().toString();
                String theLocation = dataSnapshot.child("location").getValue().toString();
                String theCompany = dataSnapshot.child("company").getValue().toString();
                String theReference = dataSnapshot.child("reference").getValue().toString();
                String theType = dataSnapshot.child("type").getValue().toString();
                String theInfo = dataSnapshot.child("info").getValue().toString();


                title.setText(theTitle);
                location.setText(theLocation);
                company.setText(theCompany);
                reference.setText(theReference);
                type.setText(theType);
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
