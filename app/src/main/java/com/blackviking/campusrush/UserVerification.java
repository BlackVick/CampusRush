package com.blackviking.campusrush;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserVerification extends AppCompatActivity {

    private ImageView reloadPage;
    private Button resendVerification;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
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

        setContentView(R.layout.activity_user_verification);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   CURRENT USER   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        reloadPage = (ImageView)findViewById(R.id.reloadPage);
        resendVerification = (Button)findViewById(R.id.resendVerification);

        /*---   VERIFICATION   ---*/
        if (Common.isConnectedToInternet(getBaseContext())) {

            confirmVerification();

        } else {

            Toast.makeText(this, "No Internet Access !", Toast.LENGTH_SHORT).show();

        }


        /*---   RESEND VERIFICATION   ---*/
        resendVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null){

                    mAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            showErrorDialog("Verification Mail Has Been Sent To Your Mail");
                        }
                    });

                }
            }
        });


        /*---   RELOAD   ---*/
        reloadPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.getCurrentUser()
                        .reload()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){

                                    confirmVerification();

                                }
                            }
                        });
            }
        });

    }

    private void confirmVerification() {

        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (mAuth.getCurrentUser().isEmailVerified()) {

                        Paper.book().write(Common.USER_ID, currentUid);

                        FirebaseMessaging.getInstance().subscribeToTopic(currentUid);
                        Paper.book().write(Common.NOTIFICATION_STATE, "true");

                        FirebaseMessaging.getInstance().subscribeToTopic(Common.FEED_NOTIFICATION_TOPIC+currentUid);
                        Paper.book().write(Common.MY_FEED_NOTIFICATION_STATE, "true");

                        Intent homeIntent = new Intent(UserVerification.this, Home.class);
                        startActivity(homeIntent);
                        finish();
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                    }
                }
            });

        }

    }

    public void showErrorDialog(String theWarning){

        AlertDialog alertDialog = new AlertDialog.Builder(this)
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

    @Override
    public void onBackPressed() {
        finish();
    }
}
