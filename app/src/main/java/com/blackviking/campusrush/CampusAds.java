package com.blackviking.campusrush;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Settings.Help;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CampusAds extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;

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

        setContentView(R.layout.activity_campus_ads);


        /*---   WIDGETS   ---*/
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

        activityName.setText("Campus Ads");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(CampusAds.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });
    }
}
