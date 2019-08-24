package com.blackviking.campusrush.Plugins.GamersHub;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Common.GetTimeAgo;
import com.blackviking.campusrush.Home;
import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GamersHub extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private GamersTabsPager tabsPager;

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

        setContentView(R.layout.activity_gamers_hub);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   WIDGETS   ---*/
        tabLayout = (TabLayout)findViewById(R.id.gamersTabs);
        viewPager = (ViewPager)findViewById(R.id.gamersViewPager);

        /*---   TABS   ---*/
        tabsPager = new GamersTabsPager(getSupportFragmentManager());
        viewPager.setAdapter(tabsPager);
        tabLayout.setupWithViewPager(viewPager);

        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Gamers Hub");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(GamersHub.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
