package com.blackviking.campusrush.Plugins.Scholarships;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Scholarships extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private RecyclerView scholarshipRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference scholarshipRef;
    private FirebaseRecyclerAdapter<ScholarshipModel, ScholarshipViewHolder> adapter;

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

        setContentView(R.layout.activity_scholarships);


        /*---   FIREBASE   ---*/
        scholarshipRef = db.getReference("Scholarships");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        scholarshipRecycler = (RecyclerView)findViewById(R.id.scholarshipRecycler);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Scholarships");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(Scholarships.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        loadScholarships();
    }

    private void loadScholarships() {

        scholarshipRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        scholarshipRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<ScholarshipModel, ScholarshipViewHolder>(
                ScholarshipModel.class,
                R.layout.scholarship_item,
                ScholarshipViewHolder.class,
                scholarshipRef
        ) {
            @Override
            protected void populateViewHolder(ScholarshipViewHolder viewHolder, ScholarshipModel model, int position) {

                viewHolder.title.setText(model.getTitle());
                viewHolder.location.setText(model.getLocation());
                viewHolder.deadline.setText("Application Deadline : "+model.getDeadline());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent scholarshipIntent = new Intent(Scholarships.this, ScholarshipInfo.class);
                        scholarshipIntent.putExtra("ScholarshipId", adapter.getRef(position).getKey());
                        startActivity(scholarshipIntent);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        scholarshipRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
