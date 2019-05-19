package com.blackviking.campusrush.Settings;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NotificationSetting extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private String currentUid, userNotiState, campusFeedNotiState, myFeedNotiState, gamersNotiState, skitNotiState;
    private SwitchCompat userNoti, campusFeedNoti, myFeedNoti, gamersNoti, skitNoti;

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

        setContentView(R.layout.activity_notification_setting);


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   LOCAL   ---*/
        Paper.init(this);
        userNotiState = Paper.book().read(Common.NOTIFICATION_STATE);
        campusFeedNotiState = Paper.book().read(Common.FEED_NOTIFICATION_STATE);
        myFeedNotiState = Paper.book().read(Common.MY_FEED_NOTIFICATION_STATE);
        gamersNotiState = Paper.book().read(Common.GAMERS_NOTIFICATION_STATE);
        skitNotiState = Paper.book().read(Common.SKIT_NOTIFICATION_STATE);


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        userNoti = (SwitchCompat)findViewById(R.id.personalNotificationSwitch);
        campusFeedNoti = (SwitchCompat)findViewById(R.id.feedNotificationSwitch);
        myFeedNoti = (SwitchCompat)findViewById(R.id.myFeedNotificationSwitch);
        gamersNoti = (SwitchCompat)findViewById(R.id.gamersNotificationSwitch);
        skitNoti = (SwitchCompat)findViewById(R.id.skitNotificationSwitch);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Notifications");
        helpActivity.setVisibility(View.GONE);

        /*---   NOTIFICATION SWITCH SETTINGS HANDLER   ---*/
        /*---   USER NOTIFICATION   ---*/
        if (userNotiState == null || TextUtils.isEmpty(userNotiState) || userNotiState.equals("false")) {
            userNoti.setChecked(false);
        } else {
            userNoti.setChecked(true);
        }

        userNoti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    /*---   SUBSCRIBE TO NOTIFICATION   ---*/
                    Paper.book().write(Common.NOTIFICATION_STATE, "true");
                    FirebaseMessaging.getInstance().subscribeToTopic(currentUid);

                } else {
                    /*---   UNSUBSCRIBE FROM NOTIFICATION   ---*/
                    Paper.book().write(Common.NOTIFICATION_STATE, "false");
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUid);
                }
            }
        });


        /*---   FEED NOTIFICATION   ---*/
        if (campusFeedNotiState == null || TextUtils.isEmpty(campusFeedNotiState) || campusFeedNotiState.equals("false")) {
            campusFeedNoti.setChecked(false);
        } else {
            campusFeedNoti.setChecked(true);
        }

        campusFeedNoti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    /*---   SUBSCRIBE TO NOTIFICATION   ---*/
                    Paper.book().write(Common.FEED_NOTIFICATION_STATE, "true");
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.FEED_NOTIFICATION_TOPIC);

                } else {
                    /*---   UNSUBSCRIBE FROM NOTIFICATION   ---*/
                    Paper.book().write(Common.FEED_NOTIFICATION_STATE, "false");
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC);
                }
            }
        });


        /*---   MY FEED NOTIFICATION   ---*/
        if (myFeedNotiState == null || TextUtils.isEmpty(myFeedNotiState) || myFeedNotiState.equals("false")) {
            myFeedNoti.setChecked(false);
        } else {
            myFeedNoti.setChecked(true);
        }

        myFeedNoti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    /*---   SUBSCRIBE TO NOTIFICATION   ---*/
                    Paper.book().write(Common.MY_FEED_NOTIFICATION_STATE, "true");
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.FEED_NOTIFICATION_TOPIC+currentUid);

                } else {
                    /*---   UNSUBSCRIBE FROM NOTIFICATION   ---*/
                    Paper.book().write(Common.MY_FEED_NOTIFICATION_STATE, "false");
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC+currentUid);
                }
            }
        });


        /*---   GAMERS NOTIFICATION   ---*/
        if (gamersNotiState == null || TextUtils.isEmpty(gamersNotiState) || gamersNotiState.equals("false")) {
            gamersNoti.setChecked(false);
        } else {
            gamersNoti.setChecked(true);
        }

        gamersNoti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    /*---   SUBSCRIBE TO NOTIFICATION   ---*/
                    Paper.book().write(Common.GAMERS_NOTIFICATION_STATE, "true");
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.GAMERS_NOTIFICATION_TOPIC);

                } else {
                    /*---   UNSUBSCRIBE FROM NOTIFICATION   ---*/
                    Paper.book().write(Common.GAMERS_NOTIFICATION_STATE, "false");
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.GAMERS_NOTIFICATION_TOPIC);
                }
            }
        });


        /*---   SKIT NOTIFICATION   ---*/
        if (skitNotiState == null || TextUtils.isEmpty(skitNotiState) || skitNotiState.equals("false")) {
            skitNoti.setChecked(false);
        } else {
            skitNoti.setChecked(true);
        }

        skitNoti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    /*---   SUBSCRIBE TO NOTIFICATION   ---*/
                    Paper.book().write(Common.SKIT_NOTIFICATION_STATE, "true");
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.SKIT_NOTIFICATION_TOPIC);

                } else {
                    /*---   UNSUBSCRIBE FROM NOTIFICATION   ---*/
                    Paper.book().write(Common.SKIT_NOTIFICATION_STATE, "false");
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.SKIT_NOTIFICATION_TOPIC);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
