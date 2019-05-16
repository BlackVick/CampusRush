package com.blackviking.campusrush.Plugins.Vacancies;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Vacancies extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private RecyclerView vacancyRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference vacancyRef;
    private FirebaseRecyclerAdapter<VacancyModel, VacancyViewHolder> adapter;

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

        setContentView(R.layout.activity_vacancies);


        /*---   FIREBASE   ---*/
        vacancyRef = db.getReference("Vacancies");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        vacancyRecycler = (RecyclerView)findViewById(R.id.vacancyRecycler);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Vacancies");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(Vacancies.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        loadVacancies();
    }

    private void loadVacancies() {

        vacancyRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        vacancyRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<VacancyModel, VacancyViewHolder>(
                VacancyModel.class,
                R.layout.vacancy_item,
                VacancyViewHolder.class,
                vacancyRef
        ) {
            @Override
            protected void populateViewHolder(VacancyViewHolder viewHolder, VacancyModel model, int position) {

                viewHolder.title.setText(model.getTitle());
                viewHolder.company.setText(model.getCompany());
                viewHolder.location.setText(model.getLocation());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent vacancyIntent = new Intent(Vacancies.this, VacancyInfo.class);
                        vacancyIntent.putExtra("VacancyId", adapter.getRef(position).getKey());
                        startActivity(vacancyIntent);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        vacancyRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
