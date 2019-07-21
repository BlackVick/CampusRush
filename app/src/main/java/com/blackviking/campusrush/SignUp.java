package com.blackviking.campusrush;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignUp extends AppCompatActivity {

    private Login loginActivity;
    private EditText username, firstName, lastName, eMail, password, confirmPassword;
    private Button signUp;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, authed;
    private TextView policyText;
    private RelativeLayout rootLayout;
    private String currentUid;
    private CheckBox privacy;
    private Spinner gender;
    private String selectGender = "", theUsername ="", theFirstName = "";
    private String theLastName = "", theEmail = "", thePassword = "", thePasswordConfirm = "";
    private ProgressBar progressBar;
    private Boolean isUserCreated;

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

        setContentView(R.layout.activity_sign_up);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        authed = db.getReference("AuthedUsers");
        mAuth = FirebaseAuth.getInstance();


        /*---   WIDGETS   ---*/
        username = (EditText)findViewById(R.id.signUpUserName);
        firstName = (EditText)findViewById(R.id.signUpFirstName);
        lastName = (EditText)findViewById(R.id.signUpLastName);
        eMail = (EditText)findViewById(R.id.signUpEmail);
        password = (EditText)findViewById(R.id.signUpPassword);
        confirmPassword = (EditText)findViewById(R.id.signUpConfirmPassword);
        signUp = (Button)findViewById(R.id.signUserUp);
        signUp.setEnabled(false);
        policyText = (TextView)findViewById(R.id.policyTextView);
        rootLayout = (RelativeLayout)findViewById(R.id.signUpRootLayout);
        privacy = (CheckBox)findViewById(R.id.privacyPolicyCheck);
        gender = (Spinner)findViewById(R.id.genderSpinner);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);


        isUserCreated = false;



        /*---   PRIVACY POLICY   ---*/
        /*---   PRIVACY SHIIIT   ---*/
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

        policyText.setText(ss);
        policyText.setMovementMethod(LinkMovementMethod.getInstance());



        /*---   POLICY CHECK   ---*/
        privacy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){

                    signUp.setEnabled(true);

                } else {

                    signUp.setEnabled(false);

                }

            }
        });


        /*---   GENDER SPINNER   ---*/
        /*---   GENDER   ---*/
        /*---   FILL GENDER SPINNER   ---*/
        List<String> genderList = new ArrayList<>();
        genderList.add(0, "Gender");
        genderList.add("Male");
        genderList.add("Female");
        genderList.add("I Am Not Sure");

        ArrayAdapter<String> dataAdapterGender;
        dataAdapterGender = new ArrayAdapter(this, android.R.layout.simple_spinner_item, genderList);
        dataAdapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(dataAdapterGender);
        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Gender")){

                    selectGender = parent.getItemAtPosition(position).toString();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        /*---   SIGN UP   ---*/
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                theUsername = username.getText().toString().trim().toLowerCase();
                theFirstName = firstName.getText().toString().trim();
                theLastName = lastName.getText().toString().trim();
                theEmail = eMail.getText().toString().trim();
                thePassword = password.getText().toString();
                thePasswordConfirm = confirmPassword.getText().toString();

                if (!isUserCreated) {

                    if (Common.isConnectedToInternet(getBaseContext()))
                        signUserUp();
                    else
                        Common.showErrorDialog(SignUp.this, "No Internet Access");

                } else {

                    if (Common.isConnectedToInternet(getBaseContext())) {
                        handleSignInResponse(theUsername, theEmail, theFirstName, theLastName, selectGender);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    else {
                        Common.showErrorDialog(SignUp.this, "No Internet Access");
                    }

                }

            }
        });

    }

    private void signUserUp() {

        progressBar.setVisibility(View.VISIBLE);
        signUp.setEnabled(false);


        if (TextUtils.isEmpty(theUsername)){

            username.setError("Please Pick A Unique Username");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signUp);
            progressBar.setVisibility(View.GONE);
            username.requestFocus();
            signUp.setEnabled(true);

        } else if (TextUtils.isEmpty(theFirstName) || TextUtils.isEmpty(theLastName)){

            firstName.setError("Please Provide Your Name !");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signUp);
            progressBar.setVisibility(View.GONE);
            firstName.requestFocus();
            signUp.setEnabled(true);

        } else if (TextUtils.isEmpty(theEmail) || !isValidEmail(theEmail)){

            eMail.setError("E-Mail Can Not Be Empty Or Invalid !");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signUp);
            progressBar.setVisibility(View.GONE);
            eMail.requestFocus();
            signUp.setEnabled(true);

        } else if (selectGender.equalsIgnoreCase("")){

            Common.showErrorDialog(SignUp.this, "What Gender Do You Identify As?");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signUp);
            progressBar.setVisibility(View.GONE);
            signUp.setEnabled(true);

        } else if (TextUtils.isEmpty(thePassword) || TextUtils.isEmpty(thePasswordConfirm)){

            password.setError("Provide A Secure Password !");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signUp);
            progressBar.setVisibility(View.GONE);
            password.requestFocus();
            signUp.setEnabled(true);

        } else if (thePassword.length() < 6){

            password.setError("Password Too Weak");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signUp);
            progressBar.setVisibility(View.GONE);
            password.requestFocus();
            signUp.setEnabled(true);

        } else if (!thePassword.equals(thePasswordConfirm)){

            password.setError("Passwords Do Not Match, Please Retry !");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signUp);
            progressBar.setVisibility(View.GONE);
            password.requestFocus();
            signUp.setEnabled(true);

        } else {

            mAuth.createUserWithEmailAndPassword(theEmail, thePassword).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                isUserCreated = true;
                                handleSignInResponse(theUsername, theEmail, theFirstName, theLastName, selectGender);


                            } else {

                                Common.showErrorDialog(SignUp.this, "An Unknown Error Occurred While Signing Up With Email. Provided Mail Might Already Exist Or Be Invalid. Please Try Again Later !");
                                progressBar.setVisibility(View.GONE);
                                signUp.setEnabled(true);

                            }
                        }
                    }
            );

        }
    }

    private void handleSignInResponse(final String theUsername, final String theEmail, final String theFirstName, final String theLastName, final String selectGender) {

        if (mAuth.getCurrentUser() != null) {

            if (!TextUtils.isEmpty(theUsername) || !TextUtils.isEmpty(theEmail) || !TextUtils.isEmpty(theFirstName)
                    || !TextUtils.isEmpty(theLastName) || !TextUtils.isEmpty(selectGender)) {

                currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                authed.child(currentUid).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {


                        final Map<String, Object> newUserMap = new HashMap<>();
                        newUserMap.put("username", theUsername);
                        newUserMap.put("firstName", theFirstName);
                        newUserMap.put("lastName", theLastName);
                        newUserMap.put("email", theEmail);
                        newUserMap.put("gender", selectGender);
                        newUserMap.put("status", "New to Campus Rush");
                        newUserMap.put("profilePicture", "");
                        newUserMap.put("profilePictureThumb", "");
                        newUserMap.put("department", "");
                        newUserMap.put("privacy", "public");
                        newUserMap.put("userType", "User");
                        newUserMap.put("bio", "");
                        newUserMap.put("riskLevel", "Normal");
                        newUserMap.put("messaging", "public");


                        /*---   REGISTER USER   ---*/
                        authed.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child(currentUid).exists()){


                                    userRef
                                            .orderByChild("username")
                                            .equalTo(theUsername)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if (!dataSnapshot.exists()){

                                                username.setTextColor(getResources().getColor(R.color.proceed));

                                                /*---   DATABASE   ---*/
                                                userRef.child(currentUid).setValue(newUserMap).addOnCompleteListener(
                                                        new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()){

                                                                    mAuth.getCurrentUser()
                                                                            .sendEmailVerification();
                                                                    updateUI();

                                                                }

                                                            }
                                                        }
                                                );


                                            } else {

                                                YoYo.with(Techniques.Shake)
                                                        .duration(500)
                                                        .playOn(signUp);
                                                username.setTextColor(getResources().getColor(R.color.red));
                                                username.requestFocus();
                                                username.setError("Username Already In Use");
                                                progressBar.setVisibility(View.GONE);
                                                signUp.setEnabled(true);

                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });


                                } else {

                                    Common.showErrorDialog(SignUp.this, "Unknown Error Occurred !");
                                    progressBar.setVisibility(View.GONE);

                                }

                                authed.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });

            } else {

                YoYo.with(Techniques.Shake)
                        .duration(500)
                        .playOn(signUp);
                Common.showErrorDialog(SignUp.this, "Some Info Are Missing !");
                progressBar.setVisibility(View.GONE);
                signUp.setEnabled(true);

            }

        } else {

            progressBar.setVisibility(View.GONE);

        }

    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void updateUI() {

        if (mAuth.getCurrentUser() !=  null){

            currentUid = mAuth.getCurrentUser().getUid();

            progressBar.setVisibility(View.GONE);

            Paper.book().write(Common.USER_ID, currentUid);

            FirebaseMessaging.getInstance().subscribeToTopic(currentUid);
            Paper.book().write(Common.NOTIFICATION_STATE, "true");

            FirebaseMessaging.getInstance().subscribeToTopic(Common.ADMIN_MESSAGE);

            FirebaseMessaging.getInstance().subscribeToTopic(Common.FEED_NOTIFICATION_TOPIC+currentUid);
            Paper.book().write(Common.MY_FEED_NOTIFICATION_STATE, "true");

            Paper.book().write(Common.isSubServiceRunning, false);

            Intent homeIntent = new Intent(SignUp.this, Home.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

        }

    }

    @Override
    public void onBackPressed() {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            authed.child(user.getUid()).removeValue();
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            finish();
                        }
                    });
        } else {

            finish();

        }


    }
}
