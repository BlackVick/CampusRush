package com.blackviking.campusrush;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.jcminarro.roundkornerlayout.RoundKornerRelativeLayout;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Login extends AppCompatActivity {

    private ImageView helpButton;
    private RoundKornerRelativeLayout buttonsLayout;
    private Button signInBtn, signUpBtn;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*---   FONT MANAGEMENT   ---*/
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Thin.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_login);


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
    }
}
