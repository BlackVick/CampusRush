package com.blackviking.campusrush;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Services.CheckSubStatusService;
import com.blackviking.campusrush.Settings.Help;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jcminarro.roundkornerlayout.RoundKornerRelativeLayout;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Login extends AppCompatActivity {

    private ImageView helpButton;
    private TextView signUpLink, resetPassword;
    private EditText email, password;
    private RelativeLayout loginLayout, bottomPort;
    private LinearLayout topPort;
    private Button signInBtn;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private ProgressBar progressBar;
    private String currentUid;

    private Animation fromBottom, fromTop;

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
        loginLayout = (RelativeLayout)findViewById(R.id.loginLayout);
        email = (EditText)findViewById(R.id.signInEmail);
        password = (EditText)findViewById(R.id.signInPassword);
        signInBtn = (Button)findViewById(R.id.loginButton);
        resetPassword = (TextView)findViewById(R.id.resetPassword);
        signUpLink = (TextView)findViewById(R.id.signUpLink);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        bottomPort = (RelativeLayout)findViewById(R.id.lowerPort);
        topPort = (LinearLayout)findViewById(R.id.topPort);

        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom);
        fromTop = AnimationUtils.loadAnimation(this, R.anim.from_top);

        bottomPort.setAnimation(fromBottom);
        topPort.setAnimation(fromTop);


        /*---   SLOW BUTTONS REVEAL   ---*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loginLayout.setVisibility(View.VISIBLE);
            }
        }, 1500);



        /*---   RESET   ---*/
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext()))
                    resetThePassword();
                else
                    showErrorDialog("No Internet Access !");
            }
        });

        /*---   SIGN UP   ---*/
        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(Login.this, SignUp.class);
                startActivity(signUpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   SIGN IN   ---*/
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext()))
                    signInUser();
                else
                    showErrorDialog("No Internet Access !");
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

    private void signInUser() {

        progressBar.setVisibility(View.VISIBLE);
        signInBtn.setEnabled(false);
        signUpLink.setEnabled(false);
        resetPassword.setEnabled(false);

        String theEmail = email.getText().toString().trim();
        String thePassword = password.getText().toString();

        if (TextUtils.isEmpty(theEmail) || !isValidEmail(theEmail)){

            email.setError("Provide A Valid E-Mail Address");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signInBtn);
            progressBar.setVisibility(View.GONE);
            email.requestFocus();
            signInBtn.setEnabled(true);
            signUpLink.setEnabled(true);
            resetPassword.setEnabled(true);

        } else if (TextUtils.isEmpty(thePassword)){

            password.setError("Provide Your Password");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signInBtn);
            progressBar.setVisibility(View.GONE);
            password.requestFocus();
            signInBtn.setEnabled(true);
            signUpLink.setEnabled(true);
            resetPassword.setEnabled(true);

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

                            Toast.makeText(Login.this, "Unknown Error Occurred, Sign In Failed", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            signInBtn.setEnabled(true);
                            signUpLink.setEnabled(true);
                            resetPassword.setEnabled(true);

                        }
                    }
                }
        );

    }

    private void handleSignInResponse() {

        if (mAuth.getCurrentUser() != null) {

            currentUid = mAuth.getCurrentUser().getUid();

            progressBar.setVisibility(View.GONE);

            /*---   LOCAL   ---*/
            Paper.book().write(Common.USER_ID, currentUid);

            FirebaseMessaging.getInstance().subscribeToTopic(currentUid);
            Paper.book().write(Common.NOTIFICATION_STATE, "true");

            Paper.book().write(Common.isSubServiceRunning, false);
            Intent intent = new Intent(getApplicationContext(), CheckSubStatusService.class);
            startService(intent);

            FirebaseMessaging.getInstance().subscribeToTopic(Common.FEED_NOTIFICATION_TOPIC+currentUid);
            Paper.book().write(Common.MY_FEED_NOTIFICATION_STATE, "true");

            Intent signInIntent = new Intent(Login.this, Home.class);
            startActivity(signInIntent);
            finish();
            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

        } else {

            progressBar.setVisibility(View.GONE);

        }

    }

    private void resetThePassword() {

        progressBar.setVisibility(View.VISIBLE);
        signInBtn.setEnabled(false);
        signUpLink.setEnabled(false);
        resetPassword.setEnabled(false);

        String theResetMail = email.getText().toString().trim();

        if (TextUtils.isEmpty(theResetMail) || !isValidEmail(theResetMail)){

            email.setError("Please Provide The E-Mail You Want A Password Reset For.");
            email.requestFocus();
            progressBar.setVisibility(View.GONE);
            signInBtn.setEnabled(true);
            signUpLink.setEnabled(true);
            resetPassword.setEnabled(true);

        } else {

            mAuth.sendPasswordResetEmail(theResetMail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                Toast.makeText(Login.this, "Password Reset Instructions Have Been Sent To Your Mail !", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                signInBtn.setEnabled(true);
                                signUpLink.setEnabled(true);
                                resetPassword.setEnabled(true);

                            } else {

                                Toast.makeText(Login.this, "Password Reset Failed", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                signInBtn.setEnabled(true);
                                signUpLink.setEnabled(true);
                                resetPassword.setEnabled(true);

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
