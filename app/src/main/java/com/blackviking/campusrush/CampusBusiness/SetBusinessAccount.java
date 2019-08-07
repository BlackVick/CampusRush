package com.blackviking.campusrush.CampusBusiness;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.AddFeed;
import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Services.SubscriptionService;
import com.blackviking.campusrush.Settings.Help;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.exceptions.ExpiredAccessCodeException;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;
import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SetBusinessAccount extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, promoterRef, priceRef, subscriptionRef, businessProfileRef, transactionRef;
    private String currentUid, userType, userEmail;
    private EditText name, address, category, description, phone, facebook, instagram, twitter;
    private TextView price, policyTextView;
    private Button paystackBtn, createBtn;
    private CheckBox businessTerms;

    private android.app.AlertDialog mDialog;
    private Charge charge;
    private Transaction transaction;
    private String paystackPublicKey = "pk_live_ff14fabadbbe3bb22c5f3dc42da377993cf9b7f9";
    private MaterialEditText cardNumber, cardCVV, cardExMonth, cardExYear;
    private Button payButton;
    private boolean hasPaid = false;
    private boolean isProfileCreated = false;
    private String currentPrice = "", subscriptionStatus = "";
    private int theActualPrice = 0, theActualPriceForText = 0;

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


        /*---   PAYSTACK   ---*/
        PaystackSdk.setPublicKey(paystackPublicKey);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        promoterRef = db.getReference("PromotedAds");
        priceRef = db.getReference("BusinessAccountPrice");
        subscriptionRef = db.getReference("BusinessAccountSubscriptions");
        transactionRef = db.getReference("TransactionHistory");
        businessProfileRef = db.getReference("BusinessProfile");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        name = (EditText)findViewById(R.id.businessNameEdt);
        address = (EditText)findViewById(R.id.businessAddressEdt);
        category = (EditText)findViewById(R.id.businessCategoryEdt);
        description = (EditText)findViewById(R.id.businessDescriptionEdt);
        phone = (EditText)findViewById(R.id.businessPhoneEdt);
        facebook = (EditText)findViewById(R.id.businessFacebookEdt);
        instagram = (EditText)findViewById(R.id.businessInstagramEdt);
        twitter = (EditText)findViewById(R.id.businessTwitterEdt);
        price = (TextView)findViewById(R.id.businessAccountPrice);
        policyTextView = (TextView)findViewById(R.id.policyTextView);
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

        activityName.setText("Business Account");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(SetBusinessAccount.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   PRICE   ---*/
        priceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentPrice = dataSnapshot.child("price").getValue().toString();

                theActualPrice = Integer.parseInt(currentPrice);
                theActualPriceForText = Integer.parseInt(currentPrice)/100;

                price.setText("₦ " + theActualPriceForText + " per Month");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*---   SUBSCRIPTION LOT   ---*/
        subscriptionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(currentUid).exists()){

                    subscriptionStatus = dataSnapshot.child(currentUid).child("subscriptionStatus").getValue().toString();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*---   CURRENT USER   ---*/
        userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                userType = dataSnapshot.child("userType").getValue().toString();
                userEmail = dataSnapshot.child("email").getValue().toString();

                if (userType.equalsIgnoreCase("Business") && subscriptionStatus.equalsIgnoreCase("Active")
                        || userType.equalsIgnoreCase("Admin") && subscriptionStatus.equalsIgnoreCase("Active")){

                    hasPaid = true;

                } else {

                    hasPaid = false;

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   PRIVACY AND TERMS LINK   ---*/
        String text = "I have read and agreed to all the TERMS OF USE and PRIVACY POLICY";
        SpannableString ss = new SpannableString(text);
        ClickableSpan termsText = new ClickableSpan() {
            @Override
            public void onClick(View widget) {

                String termUrl = "https://campus-rush.web.app/customer-terms-and-privacy-policy.html#terms";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(termUrl));
                startActivity(i);

            }
        };

        ClickableSpan privacyText = new ClickableSpan() {
            @Override
            public void onClick(View widget) {

                String privacyUrl = "https://campus-rush.web.app/customer-terms-and-privacy-policy.html#privacyPolicy";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(privacyUrl));
                startActivity(i);

            }
        };


        ss.setSpan(termsText, 34, 46, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(privacyText, 51, 65, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        policyTextView.setText(ss);
        policyTextView.setMovementMethod(LinkMovementMethod.getInstance());


        /*---   TERMS   ---*/
        businessTerms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){

                    if (hasPaid) {
                        createBtn.setVisibility(View.VISIBLE);
                        paystackBtn.setVisibility(View.GONE);
                    } else {
                        createBtn.setVisibility(View.GONE);
                        paystackBtn.setVisibility(View.VISIBLE);
                    }


                } else {

                    createBtn.setVisibility(View.GONE);
                    paystackBtn.setVisibility(View.GONE);

                }

            }
        });


        paystackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext()))
                    openPaystackDialog();
                else
                    showErrorDialog("No Internet Access !");
            }
        });


        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBusinessAccount();
            }
        });
    }

    private void createBusinessAccount() {

        String theBusinessName = name.getText().toString().trim();
        String theBusinessAddress = address.getText().toString().trim();
        String theBusinessCategory = category.getText().toString().trim();
        String theBusinessDescription = description.getText().toString().trim();
        String theBusinessPhone = phone.getText().toString().trim();
        String theBusinessFacebook = facebook.getText().toString().trim();
        String theBusinessInstagram = instagram.getText().toString().trim();
        String theBusinessTwitter = twitter.getText().toString().trim();

        if (hasPaid){

            if (TextUtils.isEmpty(theBusinessName)){

                showErrorDialog("You need a Business Name");

            } else if (TextUtils.isEmpty(theBusinessAddress)){

                showErrorDialog("You need a Business Address");

            } else if (TextUtils.isEmpty(theBusinessCategory)){

                showErrorDialog("What line of business/ trade are you into?");

            } else if (TextUtils.isEmpty(theBusinessDescription)){

                showErrorDialog("Please describe what you do.");

            } else if (TextUtils.isEmpty(theBusinessPhone)){

                showErrorDialog("You Need A Business Contact");

            } else {

                final Map<String, Object> newBusinessProfileMap = new HashMap<>();
                newBusinessProfileMap.put("businessName", theBusinessName);
                newBusinessProfileMap.put("businessAddress", theBusinessAddress);
                newBusinessProfileMap.put("businessCategory", theBusinessCategory);
                newBusinessProfileMap.put("businessDescription", theBusinessDescription);
                newBusinessProfileMap.put("businessPhone", theBusinessPhone);
                newBusinessProfileMap.put("businessFacebook", theBusinessFacebook);
                newBusinessProfileMap.put("businessInstagram", theBusinessInstagram);
                newBusinessProfileMap.put("businessTwitter", theBusinessTwitter);

                businessProfileRef.child(currentUid).setValue(newBusinessProfileMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SetBusinessAccount.this, "Profile Created !", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

            }

        }

    }

    private void openPaystackDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.paystack_card_verification_layout,null);

        cardNumber = (MaterialEditText) viewOptions.findViewById(R.id.cardNumberEdt);
        cardCVV = (MaterialEditText) viewOptions.findViewById(R.id.cardCvcEdt);
        cardExMonth = (MaterialEditText) viewOptions.findViewById(R.id.cardExMonthEdt);
        cardExYear = (MaterialEditText) viewOptions.findViewById(R.id.cardExYearEdt);
        payButton = (Button) viewOptions.findViewById(R.id.authorizeTransaction);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        /*---   DIALOG LAYOUT POSITION   ---*/
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);


        /*---   START TRANSACTION   ---*/
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startFreshCharge(alertDialog);

            }
        });


        alertDialog.show();

    }

    private void startFreshCharge(AlertDialog alertDialog) {

        charge = new Charge();
        charge.setCard(loadCardFromForm());


        mDialog = new SpotsDialog(SetBusinessAccount.this, "Performing transaction... please wait");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        charge.setAmount(theActualPrice);
        charge.setEmail(userEmail);
        charge.setReference("Charged_For_Campus_Rush_Unilag_" + Calendar.getInstance().getTimeInMillis());
        try {
            charge.putCustomField("Charged From", "Android SDK");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        chargeCard(alertDialog);

    }

    private void chargeCard(final AlertDialog alertDialog) {

        transaction = null;
        PaystackSdk.chargeCard(SetBusinessAccount.this, charge, new Paystack.TransactionCallback() {

            // This is called only after transaction is successful
            @Override
            public void onSuccess(Transaction transaction) {
                mDialog.dismiss();

                hasPaid = true;

                final long date = System.currentTimeMillis();
                final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                final String dateString = sdf.format(date);

                SetBusinessAccount.this.transaction = transaction;

                final Map<String, Object> newSubscriptionMap = new HashMap<>();
                newSubscriptionMap.put("subscriptionStatus", "Active");
                newSubscriptionMap.put("paymentStatus", "Paid");
                newSubscriptionMap.put("transactionRef", transaction.getReference());
                newSubscriptionMap.put("transactionDate", dateString);


                final Map<String, Object> newTransactionMap = new HashMap<>();
                newTransactionMap.put("transactionPrice", "₦ "+theActualPriceForText);
                newTransactionMap.put("transactionFor", "Business Account Subscription");
                newTransactionMap.put("transactionRef", transaction.getReference());
                newTransactionMap.put("transactionDate", dateString);
                newTransactionMap.put("transactionUser", currentUid);


                subscriptionRef.child(currentUid)
                        .setValue(newSubscriptionMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Intent intent = new Intent(SetBusinessAccount.this, SubscriptionService.class);
                        startService(intent);

                        /*---   TRANSACTION HISTORY   ---*/
                        transactionRef.push().setValue(newTransactionMap);

                        userRef.child(currentUid)
                                .child("userType")
                                .setValue("Business");
                        alertDialog.dismiss();

                        Toast.makeText(SetBusinessAccount.this, "Transaction Successful", Toast.LENGTH_SHORT).show();
                        paystackBtn.setVisibility(View.GONE);
                        createBtn.setVisibility(View.VISIBLE);

                    }
                });
            }

            // This is called only before requesting OTP
            // Save reference so you may send to server if
            // error occurs with OTP
            // No need to dismiss dialog
            @Override
            public void beforeValidate(Transaction transaction) {
                SetBusinessAccount.this.transaction = transaction;
            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                // If an access code has expired, simply ask your server for a new one
                // and restart the charge instead of displaying error
                SetBusinessAccount.this.transaction = transaction;
                if (error instanceof ExpiredAccessCodeException) {
                    SetBusinessAccount.this.startFreshCharge(alertDialog);
                    SetBusinessAccount.this.chargeCard(alertDialog);
                    return;
                }

                mDialog.dismiss();

            }

        });
    }

    private Card loadCardFromForm() {
        //validate fields
        Card card;

        String theCardNum = cardNumber.getText().toString().trim();

        //build card object with ONLY the number, update the other fields later
        card = new Card.Builder(theCardNum, 0, 0, "").build();
        String cvc = cardCVV.getText().toString().trim();
        //update the cvc field of the card
        card.setCvc(cvc);

        //validate expiry month;
        String sMonth = cardExMonth.getText().toString().trim();
        int month = 0;
        try {
            month = Integer.parseInt(sMonth);
        } catch (Exception ignored) {
        }

        card.setExpiryMonth(month);

        String sYear = cardExYear.getText().toString().trim();
        int year = 0;
        try {
            year = Integer.parseInt(sYear);
        } catch (Exception ignored) {
        }
        card.setExpiryYear(year);


        if (card.isValid()){
            return card;
        } else {
            showErrorDialog("Invalid Card !");
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /*---   WARNING DIALOG   ---*/
    public void showErrorDialog(String theWarning){

        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this)
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
}
