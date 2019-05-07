package com.blackviking.campusrush.Plugins.Awards;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AwardPolls extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private RecyclerView pollsRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<PollModel, AwardListViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference awardRef;
    private String currentAward;

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

        setContentView(R.layout.activity_award_polls);


        /*---   LOCAL   ---*/
        currentAward = getIntent().getStringExtra("AwardId");


        /*---   FIREBASE   ---*/
        awardRef = db.getReference("Awards");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        pollsRecycler = (RecyclerView)findViewById(R.id.pollsRecycler);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Polls");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(AwardPolls.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        loadPolls();

    }

    private void loadPolls() {

        pollsRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(AwardPolls.this);
        pollsRecycler.setLayoutManager(layoutManager);


        adapter = new FirebaseRecyclerAdapter<PollModel, AwardListViewHolder>(
                PollModel.class,
                R.layout.award_list_item,
                AwardListViewHolder.class,
                awardRef.child(currentAward).child("Polls")
        ) {
            @Override
            protected void populateViewHolder(AwardListViewHolder viewHolder, PollModel model, int position) {

                viewHolder.awardDepartment.setVisibility(View.GONE);
                viewHolder.awardFaculty.setVisibility(View.GONE);

                viewHolder.awardName.setText(model.getName());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent awardsIntent = new Intent(AwardPolls.this, PollCandidates.class);
                        awardsIntent.putExtra("AwardId", currentAward);
                        awardsIntent.putExtra("PollId", adapter.getRef(position).getKey());
                        startActivity(awardsIntent);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        pollsRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }
}
