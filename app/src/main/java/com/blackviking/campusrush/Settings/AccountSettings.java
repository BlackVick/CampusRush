package com.blackviking.campusrush.Settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Home;
import com.blackviking.campusrush.Login;
import com.blackviking.campusrush.R;
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

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AccountSettings extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private TextView privacy, logout, accountMail, accountUsername;
    private LinearLayout username, mail;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef;
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

        setContentView(R.layout.activity_account_settings);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users").child(currentUid);


        /*---   CURRENT USER   ---*/
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String theUsername = dataSnapshot.child("username").getValue().toString();
                String theEmail = dataSnapshot.child("email").getValue().toString();

                accountUsername.setText("@"+theUsername);
                accountMail.setText(theEmail);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        privacy = (TextView)findViewById(R.id.privacy);
        logout = (TextView)findViewById(R.id.logout);
        accountMail = (TextView)findViewById(R.id.accountMail);
        accountUsername = (TextView)findViewById(R.id.accountUsername);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Account");
        helpActivity.setVisibility(View.GONE);


        /*---   LOGOUT   ---*/
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogForLogout();
            }
        });


        /*---   PRIVACY   ---*/
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent privacyIntent = new Intent(AccountSettings.this, PrivacySetting.class);
                startActivity(privacyIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

    }

    private void openDialogForLogout() {

        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("Log Out !")
                .setIcon(R.drawable.ic_attention_red)
                .setMessage("Are You Sure You Want To Log Out?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Paper.book().destroy();

                        FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUid);
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC+currentUid);
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.FEED_NOTIFICATION_TOPIC);
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.SKIT_NOTIFICATION_TOPIC);
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.GAMERS_NOTIFICATION_TOPIC);


                        mAuth.signOut();
                        Intent signoutIntent = new Intent(AccountSettings.this, Login.class);
                        signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(signoutIntent);
                        finish();

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
}
