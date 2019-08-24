package com.blackviking.campusrush.CampusBusiness;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Model.UserModel;
import com.blackviking.campusrush.R;
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


public class RenewSubscription extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;

    private TextView price;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, priceRef, subscriptionRef, transactionRef;
    private String currentUid, userEmail;
    private android.app.AlertDialog mDialog;
    private Charge charge;
    private Transaction transaction;
    private String paystackPublicKey = "pk_live_ff14fabadbbe3bb22c5f3dc42da377993cf9b7f9";
    private MaterialEditText cardNumber, cardCVV, cardExMonth, cardExYear;
    private Button payButton;
    private String subscriptionStatus = "";
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

        setContentView(R.layout.activity_renew_subscription);


        /*---   PAYSTACK   ---*/
        PaystackSdk.setPublicKey(paystackPublicKey);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        priceRef = db.getReference("BusinessAccountPrice");
        subscriptionRef = db.getReference("BusinessAccountSubscriptions");
        transactionRef = db.getReference("TransactionHistory");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);

        price = (TextView)findViewById(R.id.businessAccountPrice);
        cardNumber = (MaterialEditText)findViewById(R.id.cardNumberEdt);
        cardCVV = (MaterialEditText)findViewById(R.id.cardCvcEdt);
        cardExMonth = (MaterialEditText)findViewById(R.id.cardExMonthEdt);
        cardExYear = (MaterialEditText)findViewById(R.id.cardExYearEdt);
        payButton = (Button)findViewById(R.id.authorizeTransaction);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Renew Subscription");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(RenewSubscription.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   PRICE   ---*/
        priceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                PriceModel currentPriceModel = dataSnapshot.getValue(PriceModel.class);

                if (currentPriceModel != null) {

                    theActualPrice = currentPriceModel.getPrice();
                    theActualPriceForText = theActualPrice / 100;

                    price.setText("₦ " + String.valueOf(theActualPriceForText) + " per Month");

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

                UserModel currentUser = dataSnapshot.getValue(UserModel.class);

                if (currentUser != null){

                    userEmail = currentUser.getEmail();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())){

                    startFreshCharge();

                } else {

                    showErrorDialog("No Internet Access !");

                }
            }
        });
    }

    private void startFreshCharge() {

        charge = new Charge();
        charge.setCard(loadCardFromForm());


        mDialog = new SpotsDialog(RenewSubscription.this, "Performing transaction... please wait");
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
        chargeCard();

    }

    private void chargeCard() {

        transaction = null;
        PaystackSdk.chargeCard(RenewSubscription.this, charge, new Paystack.TransactionCallback() {

            // This is called only after transaction is successful
            @Override
            public void onSuccess(Transaction transaction) {
                mDialog.dismiss();

                final long date = System.currentTimeMillis();
                final SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yy HH:mm");
                final String dateString = sdf.format(date);

                RenewSubscription.this.transaction = transaction;

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
                        .updateChildren(newSubscriptionMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        /*---   TRANSACTION HISTORY   ---*/
                        transactionRef.push().setValue(newTransactionMap);

                        Toast.makeText(RenewSubscription.this, "Transaction Successful", Toast.LENGTH_SHORT).show();
                        finish();

                    }
                });
            }

            // This is called only before requesting OTP
            // Save reference so you may send to server if
            // error occurs with OTP
            // No need to dismiss dialog
            @Override
            public void beforeValidate(Transaction transaction) {
                RenewSubscription.this.transaction = transaction;
            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                // If an access code has expired, simply ask your server for a new one
                // and restart the charge instead of displaying error
                RenewSubscription.this.transaction = transaction;
                if (error instanceof ExpiredAccessCodeException) {
                    RenewSubscription.this.startFreshCharge();
                    RenewSubscription.this.chargeCard();
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
