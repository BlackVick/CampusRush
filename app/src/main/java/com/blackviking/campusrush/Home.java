package com.blackviking.campusrush;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.CampusBusiness.CampusAds;
import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Fragments.Account;
import com.blackviking.campusrush.Fragments.Messages;
import com.blackviking.campusrush.Fragments.Feed;
import com.blackviking.campusrush.Fragments.Materials;
import com.blackviking.campusrush.Fragments.Notifications;
import com.blackviking.campusrush.Model.MaterialModel;
import com.blackviking.campusrush.Plugins.Awards.Awards;
import com.blackviking.campusrush.Plugins.GamersHub.GamersHub;
import com.blackviking.campusrush.Plugins.SkitCenter.SkitCenter;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.SearchFunction.SearchActivity;
import com.blackviking.campusrush.Services.CheckSubStatusService;
import com.blackviking.campusrush.Services.SubscriptionService;
import com.blackviking.campusrush.Settings.Settings;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
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
    private DatabaseReference userRef, rateRef, notificationRef, adminRef, campusAdRef, businessProfileRef;
    private CircleImageView userImage;
    private TextView userFullName, userName, notificationCount, anticipateText;
    private ImageView feed, materials, messagesNav, account, anticipateImage, notifications;
    public RelativeLayout navLayout;
    private DrawerLayout rootLayout;
    private BroadcastReceiver mMessageReceiver = null;
    private String currentUid, intentInstruction;
    private LinearLayout admin, awards, campusRantLay, gamersHub, skitCenter, campusAds, settings, signOut, anticipate;
    private TextView adminCount;
    private boolean isSubServiceOn;

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



        /*---   LOCAL   ---*/
        Paper.init(this);
        Paper.book().write(Common.APP_STATE, "Foreground");
            isSubServiceOn = Paper.book().read(Common.isSubServiceRunning);


        /*---   INTENT CLAUSE   ---*/
        if (getIntent() != null)
            intentInstruction = getIntent().getStringExtra("IntentInstruction");


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        rateRef = db.getReference("Rating");
        notificationRef = db.getReference("Notifications");
        adminRef = db.getReference("AdminManagement");
        campusAdRef = db.getReference("CampusAd");
        businessProfileRef = db.getReference("BusinessProfile");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        feed = (ImageView)findViewById(R.id.feed);
        materials = (ImageView)findViewById(R.id.materials);
        messagesNav = (ImageView)findViewById(R.id.messagesNav);
        account = (ImageView)findViewById(R.id.account);
        anticipateImage = (ImageView)findViewById(R.id.anticipateImage);
        notifications = (ImageView) findViewById(R.id.notifications);
        navLayout = (RelativeLayout)findViewById(R.id.navLayout);
        notificationCount = (TextView)findViewById(R.id.notificationCount);
        userFullName = (TextView)findViewById(R.id.appBarFullName);
        userName = (TextView)findViewById(R.id.appBarUsername);
        userImage = (CircleImageView)findViewById(R.id.appBarUserImage);
        rootLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        admin = (LinearLayout)findViewById(R.id.customAdminManage);
        awards = (LinearLayout)findViewById(R.id.customNavAwards);
        campusRantLay = (LinearLayout)findViewById(R.id.customNavCampusRant);
        gamersHub = (LinearLayout)findViewById(R.id.customNavGamersHub);
        skitCenter = (LinearLayout)findViewById(R.id.customNavSkitCenter);
        campusAds = (LinearLayout)findViewById(R.id.customNavBusiness);
        settings = (LinearLayout)findViewById(R.id.customNavSettings);
        signOut = (LinearLayout)findViewById(R.id.customNavSignOut);
        anticipate = (LinearLayout)findViewById(R.id.anticipateLayout);
        adminCount = (TextView) findViewById(R.id.managementCount);
        anticipateText = (TextView) findViewById(R.id.anticipateText);


        /*---   FRAGMENTS   ---*/
        final Feed feedFragment = new Feed();
        final Materials materialsFragment = new Materials();
        final Messages messages = new Messages();
        final Notifications notificationFragment = new Notifications();
        final Account accountFragment = new Account();


        /*---   NAVIGATION VIEW   ---*/
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);


        /*---   BUSINESS   ---*/
        businessProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(currentUid).exists()){
                    Intent intent = new Intent(getApplicationContext(), CheckSubStatusService.class);
                    startService(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   CURRENT USER   ---*/
        userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String theUsername = dataSnapshot.child("username").getValue().toString();
                String theFullName = dataSnapshot.child("lastName").getValue().toString()
                        + " " + dataSnapshot.child("firstName").getValue().toString();
                final String theImage = dataSnapshot.child("profilePictureThumb").getValue().toString();
                String security = dataSnapshot.child("riskLevel").getValue().toString();
                String userType = dataSnapshot.child("userType").getValue().toString();

                if (userType.equalsIgnoreCase("Admin")){
                    admin.setVisibility(View.VISIBLE);
                } else if (userType.equalsIgnoreCase("User")){
                    admin.setVisibility(View.GONE);
                }

                if (security.equalsIgnoreCase("Danger")){

                    Paper.book().destroy();

                    FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUid);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.ADMIN_MESSAGE);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC+currentUid);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.SKIT_NOTIFICATION_TOPIC);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.GAMERS_NOTIFICATION_TOPIC);

                    if (isSubServiceOn) {
                        Intent intent = new Intent(Home.this, SubscriptionService.class);
                        stopService(intent);
                    }

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
                        rootLayout.closeDrawer(GravityCompat.START);
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

        /*---   CAMPUS ADS   ---*/
        campusAdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String status = dataSnapshot.child("status").getValue().toString();
                String info = dataSnapshot.child("info").getValue().toString();

                if (status.equalsIgnoreCase("Active")){

                    campusAds.setVisibility(View.VISIBLE);

                } else {

                    campusAds.setVisibility(View.GONE);
                    anticipate.setVisibility(View.VISIBLE);
                    anticipateText.setText(info);
                    YoYo.with(Techniques.RubberBand)
                            .duration(500)
                            .repeat(3)
                            .playOn(anticipateImage);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            anticipate.setVisibility(View.GONE);
                        }
                    }, 3000);
                    anticipateImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openCampusAdDialog();
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   NOTIFICATIONS   ---*/
        notificationRef
                .child(currentUid)
                .orderByChild("status").equalTo("Unread")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int notificationCountInt = (int) dataSnapshot.getChildrenCount();

                if (notificationCountInt > 0){

                    notificationCount.setVisibility(View.VISIBLE);
                    notificationCount.setText(String.valueOf(notificationCountInt));

                } else {

                    notificationCount.setVisibility(View.GONE);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   ADMIN MANAGEMENT   ---*/
        adminRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int managementCount = (int) dataSnapshot.getChildrenCount();

                        if (managementCount > 0){

                            adminCount.setVisibility(View.VISIBLE);
                            adminCount.setText(String.valueOf(managementCount));

                        } else {

                            adminCount.setVisibility(View.GONE);

                        }

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


        /*---   CUSTOM NAV   ---*/
        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootLayout.closeDrawer(GravityCompat.START);
                Intent adminIntent = new Intent(Home.this, AdminManagement.class);
                startActivity(adminIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        awards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                rootLayout.closeDrawer(GravityCompat.START);
                if (mAuth.getCurrentUser().isEmailVerified()) {

                    Intent awardIntent = new Intent(Home.this, Awards.class);
                    startActivity(awardIntent);
                    overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                } else {

                    Intent verifyIntent = new Intent(Home.this, UserVerification.class);
                    startActivity(verifyIntent);

                }

            }
        });

        campusRantLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootLayout.closeDrawer(GravityCompat.START);
                Intent rantIntent = new Intent(Home.this, com.blackviking.campusrush.CampusRant.CampusRant.class);
                startActivity(rantIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        gamersHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootLayout.closeDrawer(GravityCompat.START);
                Intent gamersIntent = new Intent(Home.this, GamersHub.class);
                startActivity(gamersIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        skitCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootLayout.closeDrawer(GravityCompat.START);
                Intent skitIntent = new Intent(Home.this, SkitCenter.class);
                startActivity(skitIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        campusAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootLayout.closeDrawer(GravityCompat.START);

                if (mAuth.getCurrentUser().isEmailVerified()) {

                    Intent campusAdsIntent = new Intent(Home.this, CampusAds.class);
                    startActivity(campusAdsIntent);
                    overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                } else {

                    Intent verifyIntent = new Intent(Home.this, UserVerification.class);
                    startActivity(verifyIntent);

                }

            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootLayout.closeDrawer(GravityCompat.START);
                Intent settingIntent = new Intent(Home.this, Settings.class);
                startActivity(settingIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootLayout.closeDrawer(GravityCompat.START);
                Paper.book().destroy();

                FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUid);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.ADMIN_MESSAGE);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC+currentUid);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.SKIT_NOTIFICATION_TOPIC);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.GAMERS_NOTIFICATION_TOPIC);

                if (isSubServiceOn) {
                    Intent intent = new Intent(Home.this, SubscriptionService.class);
                    stopService(intent);
                }

                mAuth.signOut();
                Intent signoutIntent = new Intent(Home.this, Login.class);
                signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(signoutIntent);
                finish();
            }
        });




        /*---   BOTTOM NAVIGATION   ---*/
        feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setImageResource(R.drawable.new_color_feed_icon);
                materials.setImageResource(R.drawable.new_offwhite_material_icon);
                messagesNav.setImageResource(R.drawable.new_offwhite_messaging_icon);
                notifications.setImageResource(R.drawable.new_offwhite_notification_icon);
                account.setImageResource(R.drawable.new_offwhite_account_icon);

                toolbar.setTitle("Feed");

                setFragment(feedFragment);
            }
        });

        materials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setImageResource(R.drawable.new_offwhite_feed_icon);
                materials.setImageResource(R.drawable.new_color_material_icon);
                messagesNav.setImageResource(R.drawable.new_offwhite_messaging_icon);
                notifications.setImageResource(R.drawable.new_offwhite_notification_icon);
                account.setImageResource(R.drawable.new_offwhite_account_icon);

                toolbar.setTitle("Materials");

                setFragment(materialsFragment);
            }
        });

        messagesNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setImageResource(R.drawable.new_offwhite_feed_icon);
                materials.setImageResource(R.drawable.new_offwhite_material_icon);
                messagesNav.setImageResource(R.drawable.new_color_messaging_icon);
                notifications.setImageResource(R.drawable.new_offwhite_notification_icon);
                account.setImageResource(R.drawable.new_offwhite_account_icon);

                toolbar.setTitle("Messages");

                setFragment(messages);
            }
        });

        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setImageResource(R.drawable.new_offwhite_feed_icon);
                materials.setImageResource(R.drawable.new_offwhite_material_icon);
                messagesNav.setImageResource(R.drawable.new_offwhite_messaging_icon);
                notifications.setImageResource(R.drawable.new_color_notification_icon);
                account.setImageResource(R.drawable.new_offwhite_account_icon);

                toolbar.setTitle("Notifications");

                setFragment(notificationFragment);
            }
        });

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.setImageResource(R.drawable.new_offwhite_feed_icon);
                materials.setImageResource(R.drawable.new_offwhite_material_icon);
                messagesNav.setImageResource(R.drawable.new_offwhite_messaging_icon);
                notifications.setImageResource(R.drawable.new_offwhite_notification_icon);
                account.setImageResource(R.drawable.new_color_account_icon);

                toolbar.setTitle("Account");

                setFragment(accountFragment);
            }
        });


        /*---   SET BASE FRAGMENT   ---*/
        if (intentInstruction != null) {

            if (intentInstruction.equalsIgnoreCase("Notification")) {

                toolbar.setTitle("Notifications");
                setNotificationFragment(notificationFragment, toolbar);

            }

        } else {

            toolbar.setTitle("Feed");
            setBaseFragment(feedFragment, toolbar);

        }


    }

    private void openCampusAdDialog() {
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.campus_ad_intro_layout,null);


        final Button dismissInfo = (Button) viewOptions.findViewById(R.id.dismissInfo);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dismissInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void setNotificationFragment(Notifications notificationFragment, Toolbar toolbar) {

        setFragment(notificationFragment);

        feed.setImageResource(R.drawable.new_offwhite_feed_icon);
        materials.setImageResource(R.drawable.new_offwhite_material_icon);
        messagesNav.setImageResource(R.drawable.new_offwhite_messaging_icon);
        notifications.setImageResource(R.drawable.new_color_notification_icon);
        account.setImageResource(R.drawable.new_offwhite_account_icon);

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

        feed.setImageResource(R.drawable.new_color_feed_icon);
        materials.setImageResource(R.drawable.new_offwhite_material_icon);
        messagesNav.setImageResource(R.drawable.new_offwhite_messaging_icon);
        notifications.setImageResource(R.drawable.new_offwhite_notification_icon);
        account.setImageResource(R.drawable.new_offwhite_account_icon);

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

        } else if (id == R.id.action_search){

            Intent searchIntent = new Intent(Home.this, SearchActivity.class);
            startActivity(searchIntent);
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
