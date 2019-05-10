package com.blackviking.campusrush;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Settings.Help;
import com.google.firebase.auth.FirebaseAuth;
import com.jcminarro.roundkornerlayout.RoundKornerRelativeLayout;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Login extends AppCompatActivity {

    private ImageView helpButton;
    private RoundKornerRelativeLayout buttonsLayout;
    private Button signInBtn, signUpBtn;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

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

        setContentView(R.layout.activity_login);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   WIDGETS   ---*/
        helpButton = (ImageView)findViewById(R.id.helpButton);
        buttonsLayout = (RoundKornerRelativeLayout)findViewById(R.id.buttonsLayout);
        signUpBtn = (Button)findViewById(R.id.signUpBtn);
        signInBtn = (Button)findViewById(R.id.signInBtn);


        /*---   SLOW BUTTONS REVEAL   ---*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                buttonsLayout.setVisibility(View.VISIBLE);
            }
        }, 2000);


        /*---   SIGN UP   ---*/
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(Login.this, SignUp.class);
                startActivity(signUpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                finish();
            }
        });


        /*---   SIGN IN   ---*/
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(Login.this, SignIn.class);
                startActivity(signInIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                finish();
            }
        });


        /*---   HELP   ---*/
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(Login.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        final String localUser = Paper.book().read(Common.USER_ID);
        if (localUser != null && mAuth.getCurrentUser() != null){

            if (!localUser.isEmpty()){

                Intent goToHome = new Intent(Login.this, Home.class);
                startActivity(goToHome);
                finish();

            }

        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
