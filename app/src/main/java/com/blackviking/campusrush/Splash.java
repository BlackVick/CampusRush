package com.blackviking.campusrush;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.blackviking.campusrush.Common.Common;
import com.google.firebase.auth.FirebaseAuth;

import io.paperdb.Paper;

public class Splash extends AppCompatActivity {

    public static final int SKIP_SPLASH = 2500;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private RelativeLayout stroke1, stroke2, stroke3, stroke4, stroke5, stroke6;
    private Animation rightToLeft, rightToLeftInner, rightToLeftMed;
    private String localUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        /*---   LOCAL   ---*/
        localUser = Paper.book().read(Common.USER_ID);

        /*---   WIDGETS   ---*/
        stroke1 = (RelativeLayout)findViewById(R.id.stroke1);
        stroke2 = (RelativeLayout)findViewById(R.id.stroke2);
        stroke3 = (RelativeLayout)findViewById(R.id.stroke3);
        stroke4 = (RelativeLayout)findViewById(R.id.stroke4);
        stroke5 = (RelativeLayout)findViewById(R.id.stroke5);
        stroke6 = (RelativeLayout)findViewById(R.id.stroke6);

        rightToLeft = AnimationUtils.loadAnimation(this, R.anim.right_to_left);
        rightToLeftInner = AnimationUtils.loadAnimation(this, R.anim.right_to_left_inner);
        rightToLeftMed = AnimationUtils.loadAnimation(this, R.anim.right_to_left_med);

        stroke1.setAnimation(rightToLeft);
        stroke2.setAnimation(rightToLeftInner);
        stroke3.setAnimation(rightToLeftMed);
        stroke4.setAnimation(rightToLeftMed);
        stroke5.setAnimation(rightToLeftInner);
        stroke6.setAnimation(rightToLeft);


        if (!TextUtils.isEmpty(localUser)){

            Intent goToHome = new Intent(Splash.this, Home.class);
            startActivity(goToHome);
            finish();

        } else {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent loginIntent = new Intent(Splash.this, Login.class);
                    startActivity(loginIntent);
                    finish();
                }
            }, SKIP_SPLASH);

        }

    }
}
