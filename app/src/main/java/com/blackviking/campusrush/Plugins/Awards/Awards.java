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
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.jcminarro.roundkornerlayout.RoundKornerRelativeLayout;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Awards extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private RelativeLayout emptyData;
    private RecyclerView awardsRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<AwardListModel, AwardListViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference awardRef, userRef;
    private String currentUid, currentDept;
    private RoundKornerRelativeLayout unilagPortal;

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

        setContentView(R.layout.activity_awards);


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        awardRef = db.getReference("Awards");
        userRef = db.getReference("Users").child(currentUid);


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        awardsRecycler = (RecyclerView)findViewById(R.id.awardsRecycler);
        emptyData = (RelativeLayout)findViewById(R.id.emptyMaterialLayout);
        unilagPortal = (RoundKornerRelativeLayout) findViewById(R.id.unilagPortal);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Awards");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(Awards.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        unilagPortal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent schoolPortalIntent = new Intent(Awards.this, SchoolPortal.class);
                startActivity(schoolPortalIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        if (Common.isConnectedToInternet(getBaseContext())) {

            /*---   CURRENT USER   ---*/
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String theDept = dataSnapshot.child("department").getValue().toString();

                    currentDept = theDept;

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            /*---   EMPTY CHECK   ---*/
            awardRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {

                        emptyData.setVisibility(View.GONE);

                    } else {

                        emptyData.setVisibility(View.VISIBLE);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            loadAwards();

        } else {

            showErrorDialog("No Internet Access !");

        }
    }

    private void loadAwards() {

        awardsRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(Awards.this);
        awardsRecycler.setLayoutManager(layoutManager);


        adapter = new FirebaseRecyclerAdapter<AwardListModel, AwardListViewHolder>(
                AwardListModel.class,
                R.layout.award_list_item,
                AwardListViewHolder.class,
                awardRef
        ) {
            @Override
            protected void populateViewHolder(AwardListViewHolder viewHolder, final AwardListModel model, int position) {

                viewHolder.awardName.setText(model.getAwardName());
                viewHolder.awardFaculty.setText(model.getAwardFaculty());
                viewHolder.awardDepartment.setText(model.getAwardDepartment());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        if (currentDept.equalsIgnoreCase(model.getAwardDepartment())) {

                            Intent awardsIntent = new Intent(Awards.this, AwardPolls.class);
                            awardsIntent.putExtra("AwardId", adapter.getRef(position).getKey());
                            startActivity(awardsIntent);
                            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                        } else if (!currentDept.equalsIgnoreCase(model.getAwardDepartment()) && !currentDept.equalsIgnoreCase("")){

                            showErrorDialog("This Is Either Not Your Department Or Someone Has Already Used This Account To Vote In Another Department !");

                        } else if (currentDept.equalsIgnoreCase("")){

                            showConfirmationDialog("Are You Sure You Are A Student Of The Department Of "+model.getAwardDepartment()+" ? \nRemember, This Is A Permanent Choice!", model.getAwardDepartment(), adapter.getRef(position).getKey());

                        }
                    }
                });

            }
        };
        awardsRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void showConfirmationDialog(String theWarning, final String department, final String awardId){

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Attention !")
                .setIcon(R.drawable.ic_attention_red)
                .setMessage(theWarning)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        userRef.child("department")
                                .setValue(department).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent awardsIntent = new Intent(Awards.this, AwardPolls.class);
                                        awardsIntent.putExtra("AwardId", awardId);
                                        startActivity(awardsIntent);
                                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
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

    @Override
    public void onBackPressed() {
        finish();
    }

    /*---   WARNING DIALOG   ---*/
    public void showErrorDialog(String theWarning){

        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("Attention !")
                .setIcon(R.drawable.ic_attention_red)
                .setMessage(theWarning)
                .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.show();

    }
}
