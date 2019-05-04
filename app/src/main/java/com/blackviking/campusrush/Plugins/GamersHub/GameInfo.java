package com.blackviking.campusrush.Plugins.GamersHub;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GameInfo extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference trendingRef;
    private String currentGameId;
    private WebView gameWebPage;
    private TextView gameName, gameGenre, gameRating, gameReleaseDate, gamePlatforms;
    private ImageView gameImage;

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

        setContentView(R.layout.activity_game_info);


        /*---   INTENT   ---*/
        currentGameId = getIntent().getStringExtra("GameId");


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   FIREBASE   ---*/
        trendingRef = db.getReference("GamersHubTrending");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        gameWebPage = (WebView)findViewById(R.id.gameWebPage);
        gameImage = (ImageView)findViewById(R.id.currentGameImage);
        gameName = (TextView)findViewById(R.id.currentGameName);
        gameRating = (TextView)findViewById(R.id.currentGameRating);
        gameReleaseDate = (TextView)findViewById(R.id.currentGameReleaseDate);
        gamePlatforms = (TextView)findViewById(R.id.currentGamePlatforms);
        gameGenre = (TextView)findViewById(R.id.currentGameGenre);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText(currentGameId);
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(GameInfo.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        if (Common.isConnectedToInternet(getBaseContext()))
            loadCurrentGameInfo();
        else
            Common.showErrorDialog(this, "No Internet Access !");
    }

    private void loadCurrentGameInfo() {

        trendingRef.child(currentGameId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String theGenre = dataSnapshot.child("genre").getValue().toString();
                String thePlatform = dataSnapshot.child("platforms").getValue().toString();
                String theLink = dataSnapshot.child("link").getValue().toString();
                String theRating = dataSnapshot.child("rating").getValue().toString();
                String theImage = dataSnapshot.child("imageUrl").getValue().toString();
                String theReleaseDate = dataSnapshot.child("releaseDate").getValue().toString();

                gameName.setText(currentGameId);
                gameGenre.setText(theGenre);
                gamePlatforms.setText(thePlatform);
                gameRating.setText(theRating);
                gameReleaseDate.setText(theReleaseDate);

                if (!theImage.equals(""))
                    Picasso.with(getBaseContext())
                            .load(theImage)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(gameImage);


                if (!theLink.equals("")){

                    CookieSyncManager.createInstance(GameInfo.this);
                    CookieManager.getInstance().setAcceptCookie(true);

                    gameWebPage.getSettings().setJavaScriptEnabled(true);
                    gameWebPage.setFocusable(true);
                    gameWebPage.setFocusableInTouchMode(true);
                    gameWebPage.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                    gameWebPage.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                    gameWebPage.getSettings().setDomStorageEnabled(true);
                    gameWebPage.getSettings().setDatabaseEnabled(true);
                    gameWebPage.getSettings().setAppCacheEnabled(true);
                    gameWebPage.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                    gameWebPage.loadUrl(theLink);
                    gameWebPage.setWebViewClient(new WebViewClient() {



                        @Override
                        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                            Common.showErrorDialog(GameInfo.this, "Error Communicating With Server Server !");
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {

        if (gameWebPage.canGoBack()){

            gameWebPage.goBack();

        } else {

            finish();
        }
    }
}
