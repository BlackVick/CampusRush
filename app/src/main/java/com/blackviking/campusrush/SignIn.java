package com.blackviking.campusrush;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignIn extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView resetPassword;
    private MaterialEditText email, password;
    private Button signInBtn;
    private Login loginActivity;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String currentUid;

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

        setContentView(R.layout.activity_sign_in);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   WIDGETS   ---*/
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        resetPassword = (TextView)findViewById(R.id.resetPassword);
        email = (MaterialEditText)findViewById(R.id.signInEmail);
        password = (MaterialEditText)findViewById(R.id.signInPassword);
        signInBtn = (Button)findViewById(R.id.signUserIn);


        /*---   RESET   ---*/
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext()))
                    resetThePassword();
                else
                    Toast.makeText(SignIn.this, "No Internet Access !", Toast.LENGTH_SHORT).show();
            }
        });


        /*---   SIGN IN   ---*/
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if (Common.isConnectedToInternet(getBaseContext()))
                    signInUser();
                else
                    Toast.makeText(SignIn.this, "No Internet Access !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInUser() {

        progressBar.setVisibility(View.VISIBLE);
        signInBtn.setEnabled(false);

        String theEmail = email.getText().toString().trim();
        String thePassword = password.getText().toString();

        if (TextUtils.isEmpty(theEmail) || !isValidEmail(theEmail)){

            showErrorDialog("Provide A Valid E-Mail Address");
            progressBar.setVisibility(View.GONE);
            signInBtn.setEnabled(true);

        } else if (TextUtils.isEmpty(thePassword)){

            showErrorDialog("Provide Your Password");
            progressBar.setVisibility(View.GONE);
            signInBtn.setEnabled(true);

        } else {

            signInWithEmail(theEmail, thePassword);

        }

    }

    private void signInWithEmail(String theEmail, String thePassword) {

        mAuth.signInWithEmailAndPassword(theEmail, thePassword).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            handleSignInResponse();

                        } else {

                            showErrorDialog("Unknown Error Occurred, \nSign In Failed");
                            progressBar.setVisibility(View.GONE);
                            signInBtn.setEnabled(true);

                        }
                    }
                }
        );
        
    }

    private void handleSignInResponse() {

        if (mAuth.getCurrentUser() != null) {

            currentUid = mAuth.getCurrentUser().getUid();

            if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){

                progressBar.setVisibility(View.GONE);

                /*---   LOCAL   ---*/
                Paper.book().write(Common.USER_ID, currentUid);

                FirebaseMessaging.getInstance().subscribeToTopic(currentUid);
                Paper.book().write(Common.NOTIFICATION_STATE, "true");

                FirebaseMessaging.getInstance().subscribeToTopic(Common.FEED_NOTIFICATION_TOPIC+currentUid);
                Paper.book().write(Common.MY_FEED_NOTIFICATION_STATE, "true");

                Intent checkForVerification = new Intent(SignIn.this, Home.class);
                startActivity(checkForVerification);
                finish();
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

            } else {
                
                progressBar.setVisibility(View.GONE);

                Intent checkForVerification = new Intent(SignIn.this, UserVerification.class);
                startActivity(checkForVerification);
                finish();
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

            }

        } else {

            progressBar.setVisibility(View.GONE);

        }

    }

    private void resetThePassword() {

        progressBar.setVisibility(View.VISIBLE);
        
        String theResetMail = email.getText().toString().trim();

        if (TextUtils.isEmpty(theResetMail) || !isValidEmail(theResetMail)){

            showErrorDialog("Please Provide The E-Mail You Want A Password Reset For.");
            progressBar.setVisibility(View.GONE);

        } else {

            mAuth.sendPasswordResetEmail(theResetMail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                showErrorDialog("Password Reset Instructions Have Been Sent To Your Mail !");
                                progressBar.setVisibility(View.GONE);

                            } else {

                                showErrorDialog("Password Reset Failed !");
                                progressBar.setVisibility(View.GONE);

                            }
                        }
                    });

        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void showErrorDialog(String theWarning){

        AlertDialog alertDialog = new AlertDialog.Builder(this)
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

    @Override
    public void onBackPressed() {
        Intent goBack = new Intent(SignIn.this, Login.class);
        startActivity(goBack);
        finish();
    }
}
