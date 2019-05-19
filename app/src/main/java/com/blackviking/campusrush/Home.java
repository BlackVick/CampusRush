package com.blackviking.campusrush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Fragments.Account;
import com.blackviking.campusrush.Fragments.CampusRant;
import com.blackviking.campusrush.Fragments.Feed;
import com.blackviking.campusrush.Fragments.FeedUpdate;
import com.blackviking.campusrush.Fragments.Materials;
import com.blackviking.campusrush.Plugins.Awards.Awards;
import com.blackviking.campusrush.Plugins.GamersHub.GamersHub;
import com.blackviking.campusrush.Plugins.Scholarships.Scholarships;
import com.blackviking.campusrush.Plugins.SkitCenter.SkitCenter;
import com.blackviking.campusrush.Plugins.Vacancies.Vacancies;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.Settings.Settings;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, rateRef;
    private CircleImageView userImage;
    private TextView userFullName, userName;
    private ImageView feed, materials, feedUpdate, campusRant, account;
    private DrawerLayout rootLayout;
    private BroadcastReceiver mMessageReceiver = null;
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

        setContentView(R.layout.activity_home);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Feed");


        /*---   LOCAL   ---*/
        Paper.init(this);
        Paper.book().write(Common.APP_STATE, "Foreground");


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        rateRef = db.getReference("Rating");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        feed = (ImageView)findViewById(R.id.feed);
        materials = (ImageView)findViewById(R.id.materials);
        feedUpdate = (ImageView)findViewById(R.id.feedPost);
        campusRant = (ImageView)findViewById(R.id.schoolRant);
        account = (ImageView)findViewById(R.id.account);


        /*---   FRAGMENTS   ---*/
        final Feed feedFragment = new Feed();
        final Materials materialsFragment = new Materials();
        final FeedUpdate updateFragment = new FeedUpdate();
        final CampusRant rantFragment = new CampusRant();
        final Account accountFragment = new Account();


        /*---   NAVIGATION VIEW   ---*/
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        userFullName = (TextView)headerView.findViewById(R.id.appBarFullName);
        userName = (TextView)headerView.findViewById(R.id.appBarUsername);
        userImage = (CircleImageView)headerView.findViewById(R.id.appBarUserImage);
        rootLayout = (DrawerLayout)findViewById(R.id.drawer_layout);


        /*---   CURRENT USER   ---*/
        userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String theUsername = dataSnapshot.child("username").getValue().toString();
                String theFullName = dataSnapshot.child("lastName").getValue().toString()
                        + " " + dataSnapshot.child("firstName").getValue().toString();
                final String theImage = dataSnapshot.child("profilePictureThumb").getValue().toString();
                String security = dataSnapshot.child("riskLevel").getValue().toString();

                if (security.equalsIgnoreCase("Danger")){

                    Paper.book().destroy();

                    FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUid);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC+currentUid);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.SKIT_NOTIFICATION_TOPIC);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.GAMERS_NOTIFICATION_TOPIC);

                    mAuth.signOut();
                    Intent signoutIntent = new Intent(Home.this, Login.class);
                    signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(signoutIntent);
                    finish();

                }


                userName.setText("@"+theUsername);
                userFullName.setText(theFullName);

                if (!theImage.equalsIgnoreCase("")) {

                    Picasso.with(getBaseContext()).load(theImage).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.profile).into(userImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(getBaseContext()).load(theImage)
                                    .placeholder(R.drawable.profile).into(userImage);
                        }
                    });

                }

                userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myProfileIntent = new Intent(Home.this, MyProfile.class);
                        startActivity(myProfileIntent);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*---   RATING   ---*/
        rateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String rateStatus = dataSnapshot.child("status").getValue().toString();

                if (rateStatus.equalsIgnoreCase("Active")){

                    openRatingDialog();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   DRAWER LAYOUT   ---*/
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, rootLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        rootLayout.addDrawerListener(toggle);
        toggle.syncState();


        /*---   IN APP NOTIFICATION   ---*/
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("Message");

                Snackbar snackbar = Snackbar
                        .make(rootLayout, message, Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                sbView.setBackgroundColor(ContextCompat.getColor(Home.this, R.color.colorPrimaryDark));
                snackbar.setDuration(2500);
                snackbar.show();
            }
        };


        /*---   BOTTOM NAVIGATION   ---*/
        feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.color.bottom_nav_clicked);
                materials.setBackgroundResource(R.drawable.white_grey_border_top);
                feedUpdate.setBackgroundResource(R.drawable.white_grey_border_top);
                campusRant.setBackgroundResource(R.drawable.white_grey_border_top);
                account.setBackgroundResource(R.drawable.white_grey_border_top);

                toolbar.setTitle("Feed");

                setFragment(feedFragment);
            }
        });

        materials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.drawable.white_grey_border_top);
                materials.setBackgroundResource(R.color.bottom_nav_clicked);
                feedUpdate.setBackgroundResource(R.drawable.white_grey_border_top);
                campusRant.setBackgroundResource(R.drawable.white_grey_border_top);
                account.setBackgroundResource(R.drawable.white_grey_border_top);

                toolbar.setTitle("Materials");

                setFragment(materialsFragment);
            }
        });

        feedUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.drawable.white_grey_border_top);
                materials.setBackgroundResource(R.drawable.white_grey_border_top);
                feedUpdate.setBackgroundResource(R.color.bottom_nav_clicked);
                campusRant.setBackgroundResource(R.drawable.white_grey_border_top);
                account.setBackgroundResource(R.drawable.white_grey_border_top);

                toolbar.setTitle("Update");

                setFragment(updateFragment);
            }
        });

        campusRant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.drawable.white_grey_border_top);
                materials.setBackgroundResource(R.drawable.white_grey_border_top);
                feedUpdate.setBackgroundResource(R.drawable.white_grey_border_top);
                campusRant.setBackgroundResource(R.color.bottom_nav_clicked);
                account.setBackgroundResource(R.drawable.white_grey_border_top);

                toolbar.setTitle("Campus Rant");

                setFragment(rantFragment);
            }
        });

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setBackgroundResource(R.drawable.white_grey_border_top);
                materials.setBackgroundResource(R.drawable.white_grey_border_top);
                feedUpdate.setBackgroundResource(R.drawable.white_grey_border_top);
                campusRant.setBackgroundResource(R.drawable.white_grey_border_top);
                account.setBackgroundResource(R.color.bottom_nav_clicked);

                toolbar.setTitle("Account");

                setFragment(accountFragment);
            }
        });


        /*---   SET BASE FRAGMENT   ---*/
        setBaseFragment(feedFragment, toolbar);


    }

    private void openRatingDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Rate Us !")
                .setIcon(R.drawable.ic_rate_us)
                .setMessage("How well do you like Campus Rush and how likely are you to recommend to a friend?\n\nLet us know on Playstore.\nRate Now?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationContext().getPackageName()));
                        startActivity(rateIntent);

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

    private void setBaseFragment(Feed feedFragment, Toolbar toolbar) {

        setFragment(feedFragment);

        feed.setBackgroundResource(R.color.bottom_nav_clicked);
        materials.setBackgroundResource(R.drawable.white_grey_border_top);
        feedUpdate.setBackgroundResource(R.drawable.white_grey_border_top);
        campusRant.setBackgroundResource(R.drawable.white_grey_border_top);
        account.setBackgroundResource(R.drawable.white_grey_border_top);

    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingIntent = new Intent(Home.this, Settings.class);
            startActivity(settingIntent);
            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            return true;

        } else if (id == R.id.action_about) {

            Intent aboutIntent = new Intent(Home.this, About.class);
            startActivity(aboutIntent);
            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_award) {

            Intent awardIntent = new Intent(Home.this, Awards.class);
            startActivity(awardIntent);
            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

        } else if (id == R.id.nav_scholarships) {

            Intent scholarshipIntent = new Intent(Home.this, Scholarships.class);
            startActivity(scholarshipIntent);
            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

        } else if (id == R.id.nav_vacancies) {

            Intent vacanciesIntent = new Intent(Home.this, Vacancies.class);
            startActivity(vacanciesIntent);
            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

        } else if (id == R.id.nav_gamers) {

            Intent gamersIntent = new Intent(Home.this, GamersHub.class);
            startActivity(gamersIntent);
            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

        } else if (id == R.id.nav_skit_center) {

            Intent skitIntent = new Intent(Home.this, SkitCenter.class);
            startActivity(skitIntent);
            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

        } else if (id == R.id.nav_settings) {

            Intent settingIntent = new Intent(Home.this, Settings.class);
            startActivity(settingIntent);
            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

        } else if (id == R.id.nav_sign_out) {

            Paper.book().destroy();

            FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUid);
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC+currentUid);
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC);
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.SKIT_NOTIFICATION_TOPIC);
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.GAMERS_NOTIFICATION_TOPIC);

            mAuth.signOut();
            Intent signoutIntent = new Intent(Home.this, Login.class);
            signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(signoutIntent);
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*---   ONLINE STATE   ---*/
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("NOTIFICATION_BROADCAST"));
        Paper.book().write(Common.APP_STATE, "Foreground");
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*---   ONLINE STATE   ---*/
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        Paper.book().write(Common.APP_STATE, "Background");
    }
}
