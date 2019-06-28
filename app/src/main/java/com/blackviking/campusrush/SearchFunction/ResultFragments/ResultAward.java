package com.blackviking.campusrush.SearchFunction.ResultFragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.Plugins.Awards.AwardListModel;
import com.blackviking.campusrush.Plugins.Awards.AwardListViewHolder;
import com.blackviking.campusrush.Plugins.Awards.AwardPolls;
import com.blackviking.campusrush.Plugins.Awards.Awards;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResultAward extends Fragment {

    private RelativeLayout emptyData;
    private RecyclerView awardsRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<AwardListModel, AwardListViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference awardRef, userRef;
    private String currentUid, currentDept;
    private String searchParam;


    public ResultAward() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_result_award, container, false);
        // Inflate the layout for this fragment

        /*---   SEARCH PARAM   ---*/
        searchParam = Paper.book().read(Common.SEARCH_STRING);

        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        awardRef = db.getReference("Awards");
        userRef = db.getReference("Users").child(currentUid);


        /*---   WIDGETS   ---*/
        awardsRecycler = (RecyclerView)v.findViewById(R.id.awardsRecycler);
        emptyData = (RelativeLayout)v.findViewById(R.id.emptyMaterialLayout);


        if (Common.isConnectedToInternet(getContext())) {

            /*---   CURRENT USER   ---*/
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String theDept = dataSnapshot.child("department").getValue().toString();

                    currentDept = theDept;

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            /*---   EMPTY CHECK   ---*/
            awardRef.orderByChild("awardName")
                    .startAt(searchParam)
                    .endAt(searchParam+"\uf8ff")
                    .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {

                        emptyData.setVisibility(View.GONE);
                        loadAwards();

                    } else {

                        emptyData.setVisibility(View.VISIBLE);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {

            Common.showErrorDialog(getContext(), "No Internet Access !");

        }

        return v;
    }

    private void loadAwards() {

        awardsRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        awardsRecycler.setLayoutManager(layoutManager);


        adapter = new FirebaseRecyclerAdapter<AwardListModel, AwardListViewHolder>(
                AwardListModel.class,
                R.layout.award_list_item,
                AwardListViewHolder.class,
                awardRef.orderByChild("awardName")
                        .startAt(searchParam)
                        .endAt(searchParam+"\uf8ff")
        ) {
            @Override
            protected void populateViewHolder(final AwardListViewHolder viewHolder, final AwardListModel model, int position) {

                viewHolder.awardName.setText(model.getAwardName());
                viewHolder.awardFaculty.setText(model.getAwardFaculty());
                viewHolder.awardDepartment.setText(model.getAwardDepartment());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        if (currentDept.equalsIgnoreCase(model.getAwardDepartment())) {

                            Intent awardsIntent = new Intent(getContext(), AwardPolls.class);
                            awardsIntent.putExtra("AwardId", adapter.getRef(position).getKey());
                            startActivity(awardsIntent);
                            getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);

                        } else if (!currentDept.equalsIgnoreCase(model.getAwardDepartment()) && !currentDept.equalsIgnoreCase("")){

                            Common.showErrorDialog(getContext(), "This Is Either Not Your Department Or Someone Has Already Used This Account To Vote In Another Department !");

                        } else if (currentDept.equalsIgnoreCase("")){

                            showConfirmationDialog("Are You Sure You Are A Student Of The Department Of "+model.getAwardDepartment()+" ? \nRemember, This Is A Permanent Choice!", model.getAwardDepartment(), adapter.getRef(viewHolder.getAdapterPosition()).getKey());

                        }
                    }
                });

            }
        };
        awardsRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void showConfirmationDialog(String theWarning, final String department, final String awardId){

        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Attention !")
                .setIcon(R.drawable.ic_attention_red)
                .setMessage(theWarning)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        userRef.child("department")
                                .setValue(department).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent awardsIntent = new Intent(getContext(), AwardPolls.class);
                                        awardsIntent.putExtra("AwardId", awardId);
                                        startActivity(awardsIntent);
                                        getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                                    }
                                }
                        );

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
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
