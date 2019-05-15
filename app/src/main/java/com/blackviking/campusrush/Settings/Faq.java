package com.blackviking.campusrush.Settings;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.R;
import com.blackviking.campusrush.ViewHolder.ExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Faq extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private ExpandableListView listView;
    private ExpandableListAdapter adapter;
    private List<String> listFaqTitle;
    private HashMap<String, List<String>> listHash;

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

        setContentView(R.layout.activity_faq);


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        listView = (ExpandableListView)findViewById(R.id.faqExpandable);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_right, R.anim.slide_right);
            }
        });

        activityName.setText("Frequently Asked Questions");
        helpActivity.setVisibility(View.GONE);


        /*---   INITIALIZE EXPANDABLE   ---*/
        initData();
        adapter = new ExpandableListAdapter(this, listFaqTitle, listHash);
        listView.setAdapter(adapter);
    }

    private void initData() {

        listFaqTitle = new ArrayList<>();
        listHash = new HashMap<>();

        listFaqTitle.add("SIGNING UP AND SIGNING IN");
        listFaqTitle.add("ARE MY DETAILS SAFE?");
        listFaqTitle.add("HOW DO I MANAGE MY PROFILE?");
        listFaqTitle.add("HOW TO MANAGE FEED AND TIMELINE");
        listFaqTitle.add("HOW TO UPLOAD AND DOWNLOAD MATERIALS");
        listFaqTitle.add("HOW TO RANT SAFELY");
        listFaqTitle.add("HOW TO HOST DEPARTMENTAL AWARDS");
        listFaqTitle.add("HOW DO I UPLOAD MY SKITS?");
        listFaqTitle.add("WHY ARE MY SKITS NOT ONLINE?");
        listFaqTitle.add("HOW DO I MAINTAIN MY PRIVACY?");



        List<String> signUpAndSignIn = new ArrayList<>();
        signUpAndSignIn.add("");

        List<String> amISafe = new ArrayList<>();
        amISafe.add("");

        List<String> manageProfile = new ArrayList<>();
        manageProfile.add("");

        List<String> manageTimeline = new ArrayList<>();
        manageTimeline.add("");

        List<String> manageMaterials = new ArrayList<>();
        manageMaterials.add("");

        List<String> manageRant = new ArrayList<>();
        manageRant.add("");

        List<String> hostAwards = new ArrayList<>();
        hostAwards.add("");

        List<String> uploadSkits = new ArrayList<>();
        uploadSkits.add("");

        List<String> missingSkits = new ArrayList<>();
        missingSkits.add("");

        List<String> privacyFaq = new ArrayList<>();
        privacyFaq.add("");


        listHash.put(listFaqTitle.get(0), signUpAndSignIn);
        listHash.put(listFaqTitle.get(1), amISafe);
        listHash.put(listFaqTitle.get(2), manageProfile);
        listHash.put(listFaqTitle.get(3), manageTimeline);
        listHash.put(listFaqTitle.get(4), manageMaterials);
        listHash.put(listFaqTitle.get(5), manageRant);
        listHash.put(listFaqTitle.get(6), hostAwards);
        listHash.put(listFaqTitle.get(7), uploadSkits);
        listHash.put(listFaqTitle.get(8), missingSkits);
        listHash.put(listFaqTitle.get(9), privacyFaq);

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_right, R.anim.slide_right);
    }
}
