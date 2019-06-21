package com.blackviking.campusrush.Plugins.Awards;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Interface.ItemClickListener;
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

public class PollCandidates extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private RecyclerView candidateRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<CandidateModel, CandidateViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference awardRef;
    private String currentAward, currentPoll, currentUid;
    private ProgressBar progressBar;

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

        setContentView(R.layout.activity_poll_candidates);


        /*---   LOCAL   ---*/
        currentAward = getIntent().getStringExtra("AwardId");
        currentPoll = getIntent().getStringExtra("PollId");


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        awardRef = db.getReference("Awards").child(currentAward).child("Polls").child(currentPoll);


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        candidateRecycler = (RecyclerView)findViewById(R.id.candidateRecycler);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Nominees");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(PollCandidates.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   CHECK   ---*/
        progressBar.setVisibility(View.VISIBLE);

        if (Common.isConnectedToInternet(getBaseContext())) {

            awardRef.child("Results")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.child(currentUid).exists()) {

                                showAlreadyVotedDialog();

                            } else {

                                progressBar.setVisibility(View.GONE);
                                progressBar = null;

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


            loadCandidates();

        } else {
            Toast.makeText(this, "No Internet Connection !", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void loadCandidates() {

        candidateRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        candidateRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<CandidateModel, CandidateViewHolder>(
                CandidateModel.class,
                R.layout.award_candidate_item,
                CandidateViewHolder.class,
                awardRef.child("Candidates")
        ) {
            @Override
            protected void populateViewHolder(CandidateViewHolder viewHolder, final CandidateModel model, int position) {

                viewHolder.candidateName.setText(model.getName());

                if (!model.getImage().equals("")){

                    Picasso.with(getBaseContext())
                            .load(model.getImage())
                            .placeholder(R.drawable.campus_rush_feed_placeholder)
                            .into(viewHolder.candidateImage);

                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        if (Common.isConnectedToInternet(getBaseContext()))
                            showConfirmationDialog("Are You Sure "+model.getName()+" Is Who You Want To Pick? \nPlease Note That Your Votes Are Final. No Re-Do.", model.getName());
                        else
                            Common.showErrorDialog(PollCandidates.this, "No Internet Access !");
                    }
                });

            }
        };
        candidateRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void showConfirmationDialog(String theWarning, final String name){

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Attention !")
                .setIcon(R.drawable.ic_attention_red)
                .setMessage(theWarning)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        awardRef.child("Results")
                                .child(currentUid)
                                .setValue(name).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        finish();
                                    }
                                }
                        );

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

    public void showAlreadyVotedDialog(){

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Attention !")
                .setIcon(R.drawable.ic_attention_red)
                .setMessage("You Have Already Voted For This Award Class, Thank You")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
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
