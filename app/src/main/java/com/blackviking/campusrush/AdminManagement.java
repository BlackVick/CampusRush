package com.blackviking.campusrush;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.Model.AdminManageModel;
import com.blackviking.campusrush.Settings.Help;
import com.blackviking.campusrush.ViewHolder.AdminManageViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AdminManagement extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private RecyclerView adminRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<AdminManageModel, AdminManageViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, adminRef;
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

        setContentView(R.layout.activity_admin_management);


        /*---   FIREBASE   ---*/
        adminRef = db.getReference("AdminManagement");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        adminRecycler = (RecyclerView)findViewById(R.id.adminRecycler);



        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Admin Management");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(AdminManagement.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        if (Common.isConnectedToInternet(getBaseContext())){
            loadItems();
        } else {
            Common.showErrorDialog(this, "No Internet Access !");
        }
    }

    private void loadItems() {

        adminRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        adminRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<AdminManageModel, AdminManageViewHolder>(
                AdminManageModel.class,
                R.layout.admin_manage_item,
                AdminManageViewHolder.class,
                adminRef
        ) {
            @Override
            protected void populateViewHolder(final AdminManageViewHolder viewHolder, final AdminManageModel model, int position) {

                if (model.getAdminType().equalsIgnoreCase("Feed")){

                    viewHolder.adminItemTitle.setText(model.getAdminType());
                    if (!model.getImageThumbUrl().equalsIgnoreCase("")){

                        Picasso.with(getBaseContext())
                                .load(model.getImageThumbUrl())
                                .placeholder(R.drawable.campus_rush_feed_placeholder)
                                .into(viewHolder.adminItemImage);

                    } else {

                        viewHolder.adminItemImage.setImageResource(R.drawable.campus_rush_colored_logo);

                    }

                    if (!model.getUpdate().equalsIgnoreCase("")){

                        viewHolder.adminItemDescription.setText(model.getUpdate());

                    } else {

                        viewHolder.adminItemDescription.setText("It is an Image Feed");

                    }


                } else if (model.getAdminType().equalsIgnoreCase("Skit")){

                    viewHolder.adminItemTitle.setText(model.getAdminType());
                    viewHolder.adminItemDescription.setText(model.getTitle());

                    Picasso.with(getBaseContext())
                            .load(model.getThumbnail())
                            .placeholder(R.drawable.campus_rush_feed_placeholder)
                            .into(viewHolder.adminItemImage);

                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent managementDetail = new Intent(AdminManagement.this, ManagementDetail.class);
                        managementDetail.putExtra("ItemId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                        managementDetail.putExtra("ItemType", model.getAdminType());
                        startActivity(managementDetail);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        adminRecycler.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
