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

        listFaqTitle.add("SIGNING UP & SIGNING IN");
        listFaqTitle.add("ARE MY DETAILS SAFE?");
        listFaqTitle.add("HOW DO I MANAGE MY PROFILE?");
        listFaqTitle.add("HOW TO MANAGE FEED & TIMELINE");
        listFaqTitle.add("HOW TO UPLOAD & DOWNLOAD MATERIALS");
        listFaqTitle.add("WHAT IS CAMPUS RANT?");
        listFaqTitle.add("HOW TO HOST DEPARTMENTAL AWARDS");
        listFaqTitle.add("HOW DO I UPLOAD MY SKITS?");
        listFaqTitle.add("WHY ARE MY SKITS NOT ONLINE?");
        listFaqTitle.add("HOW DO I MAINTAIN MY PRIVACY?");



        List<String> signUpAndSignIn = new ArrayList<>();
        signUpAndSignIn.add("SIGN UP");
        signUpAndSignIn.add("* Provide basic information required by CAMPUS RUSH to sign up;");
        signUpAndSignIn.add("     -A Unique Username.");
        signUpAndSignIn.add("     -Full Name.");
        signUpAndSignIn.add("     -Valid E-Mail Address.");
        signUpAndSignIn.add("     -Gender.");
        signUpAndSignIn.add("* Read and agree to all Privacy Policies and Terms Of Use.");
        signUpAndSignIn.add("* Be sure to have internet connectivity.");
        signUpAndSignIn.add("* Click SIGN UP button.");
        signUpAndSignIn.add("");
        signUpAndSignIn.add("SIGN UP FAILED");
        signUpAndSignIn.add("* Check internet connectivity.");
        signUpAndSignIn.add("* Check if all required fields are filled properly.");
        signUpAndSignIn.add("* Possibly refresh your mobile phone.");
        signUpAndSignIn.add("* Try again");
        signUpAndSignIn.add("");
        signUpAndSignIn.add("SIGN UP SUCCESS");
        signUpAndSignIn.add("* After successful sign up, A verification mail is sent to the provided mail.");
        signUpAndSignIn.add("* Open the mail and verify your account.");
        signUpAndSignIn.add("");
        signUpAndSignIn.add("VERIFICATION NOT RECEIVED?");
        signUpAndSignIn.add("* Open CAMPUS RUSH app and click on resend verification.");
        signUpAndSignIn.add("* If failed again, Contact Admin By Going to the HELP setting and clicking on 'CONTACT US'.");
        signUpAndSignIn.add("* If Successful, Enjoy all free access.");
        signUpAndSignIn.add("");
        signUpAndSignIn.add("SIGN IN");
        signUpAndSignIn.add("* Check for internet connectivity.");
        signUpAndSignIn.add("* Provide your E-Mail and password to the respective fields.");
        signUpAndSignIn.add("* Click on SIGN IN button.");
        signUpAndSignIn.add("* If that don't work, Well your case is special.");

        List<String> amISafe = new ArrayList<>();
        amISafe.add("* All user details remain encrypted on a secure server so you have nothing to fear.");
        amISafe.add("* Your details will not be shared with any 3rd party.");
        amISafe.add("* If you still have worries, Please contact us");

        List<String> manageProfile = new ArrayList<>();
        manageProfile.add("* The profile settings can be accessed in 2 ways;");
        manageProfile.add("     (I). Click on the navigation drawer located at the upper left corner of the home page, then Click on the profile image.");
        manageProfile.add("     (II). Click on the account tab located at the lower right corner of the bottom navigation bar, then click on profile setting.");
        manageProfile.add("* User are allowed to alter very few details;");
        manageProfile.add("     (I). Profile Pictures");
        manageProfile.add("     (II). Status");
        manageProfile.add("     (III). Bio");

        List<String> manageTimeline = new ArrayList<>();
        manageTimeline.add("* The feed contains updates from all users.");
        manageTimeline.add("* You can also post a feed by clicking on Feed Update Tab at the center of the bottom navigation bar.");
        manageTimeline.add("* Users can post image only, text only or both as feed updates");
        manageTimeline.add("* The general feed view is available to every user but you timeline only contains your own feed.");
        manageTimeline.add("* Feeds can be viewed for more details, like and also commented on.");

        List<String> manageMaterials = new ArrayList<>();
        manageMaterials.add("* Material tab is the second tab on the bottom navigation bar.");
        manageMaterials.add("* If your faculty does not appear on the list");
        manageMaterials.add("* Click the floaty button at the bottom right corner");
        manageMaterials.add("* Fill in all the details required by the pop-up");
        manageMaterials.add("* Select file");
        manageMaterials.add("* upload file");
        manageMaterials.add("* Materials can also be downloaded very easily by clicking the download button beside the material information.");

        List<String> manageRant = new ArrayList<>();
        manageRant.add("* Campus Rant is a platform for every one to voice out their opinion about anything.");
        manageRant.add("* No restrictions, voice out your frustrations or talk about anything");
        manageRant.add("* Start a revolution to take over the school");
        manageRant.add("* Or whatever rocks your boat");

        List<String> hostAwards = new ArrayList<>();
        hostAwards.add("* You can host your departmental awards by?");
        hostAwards.add("     (I). Executives should have compiled names and images of the nominees");
        hostAwards.add("     (II). Provide faculty name, department name, different award types and Tag Line of the Award Event");
        hostAwards.add("     (III). Then contact CAMPUS RUSH on campus_rush@teenqtech.com");

        List<String> uploadSkits = new ArrayList<>();
        uploadSkits.add("* Access the Skit Center by opening the navigation drawer and clicking on Skit Center.");
        uploadSkits.add("* Click on the floaty button at the bottom right corner.");
        uploadSkits.add("* Upload your skit then add a title and optionally add a description.");
        uploadSkits.add("* Your skit will be available for view after review.");

        List<String> missingSkits = new ArrayList<>();
        missingSkits.add("* Any skit with offensive content are deleted and user put on watch list or blocked permanently");
        missingSkits.add("* If any complain or wrongful skit denial, Contact us");

        List<String> privacyFaq = new ArrayList<>();
        privacyFaq.add("* You can access your privacy setting by going to the Privacy Setting in Account.");
        privacyFaq.add("* Users can choose to be visible or anonymous on Campus Rush");


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
    }
}
