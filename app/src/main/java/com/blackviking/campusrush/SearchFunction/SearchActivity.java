package com.blackviking.campusrush.SearchFunction;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.R;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SearchActivity extends AppCompatActivity {

    private ImageView exitActivity, searchBtn;
    private EditText searchText;
    private String theSearchText;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabsPager tabsPager;

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

        setContentView(R.layout.activity_search);

        /*---   WIDGETS   ---*/
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        searchBtn = (ImageView)findViewById(R.id.searchButton);
        searchText = (EditText)findViewById(R.id.searchText);
        tabLayout = (TabLayout) findViewById(R.id.searchTabs);
        viewPager = (ViewPager) findViewById(R.id.searchViewPager);

        searchText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    if (Common.isConnectedToInternet(getBaseContext())) {
                        searchCampusRush();
                        searchText.onEditorAction(EditorInfo.IME_ACTION_DONE);
                    }
                    else {
                        showErrorDialog("No Internet Access !");
                    }

                }
                return false;
            }
        });


        /*---   APP BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {
                    searchCampusRush();
                    searchText.onEditorAction(EditorInfo.IME_ACTION_DONE);
                }
                else {
                    showErrorDialog("No Internet Access !");
                }
            }
        });

    }

    private void searchCampusRush() {

        theSearchText = searchText.getText().toString().trim();
        Paper.book().write(Common.SEARCH_STRING, theSearchText);

        if (!TextUtils.isEmpty(theSearchText)){

            tabLayout.setVisibility(View.VISIBLE);
            /*----------    TABS HANDLER   ----------*/
            tabsPager = new TabsPager(getSupportFragmentManager());
            viewPager.setAdapter(tabsPager);
            tabLayout.setupWithViewPager(viewPager);

            tabLayout.getTabAt(0).setIcon(R.drawable.new_color_account_icon);
            tabLayout.getTabAt(1).setIcon(R.drawable.new_award_icon);
            tabLayout.getTabAt(2).setIcon(R.drawable.new_rant_icon);
            tabLayout.getTabAt(3).setIcon(R.drawable.red_gamers_hub);
            tabLayout.getTabAt(4).setIcon(R.drawable.new_skit_icon);

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
