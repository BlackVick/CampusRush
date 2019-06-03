package com.blackviking.campusrush.CampusBusiness;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SetBusinessAccount extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, promoterRef;
    private String currentUid, userType;
    private MaterialEditText name, address, category, description, phone, facebook, instagram, twitter;
    private TextView price;
    private Button paystackBtn, createBtn;
    private CheckBox businessTerms;

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

        setContentView(R.layout.activity_set_business_account);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        promoterRef = db.getReference("PromotedAds");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        name = (MaterialEditText)findViewById(R.id.businessNameEdt);
        address = (MaterialEditText)findViewById(R.id.businessAddressEdt);
        category = (MaterialEditText)findViewById(R.id.businessCategoryEdt);
        description = (MaterialEditText)findViewById(R.id.businessDescriptionEdt);
        phone = (MaterialEditText)findViewById(R.id.businessPhoneEdt);
        facebook = (MaterialEditText)findViewById(R.id.businessFacebookEdt);
        instagram = (MaterialEditText)findViewById(R.id.businessInstagramEdt);
        twitter = (MaterialEditText)findViewById(R.id.businessTwitterEdt);
        price = (TextView)findViewById(R.id.businessAccountPrice);
        paystackBtn = (Button)findViewById(R.id.businessAccountPaymentBtn);
        createBtn = (Button)findViewById(R.id.createBusinessAccountBtn);
        businessTerms = (CheckBox)findViewById(R.id.acceptBusinessTermsCheckbox);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("SetUp Business Account");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(SetBusinessAccount.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   TERMS   ---*/
        businessTerms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){

                    paystackBtn.setVisibility(View.VISIBLE);

                } else {

                    paystackBtn.setVisibility(View.GONE);

                }

            }
        });
    }
}
