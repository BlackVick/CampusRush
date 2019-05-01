package com.blackviking.campusrush.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.blackviking.campusrush.R;
import com.blackviking.campusrush.RantRoom;

/**
 * A simple {@link Fragment} subclass.
 */
public class CampusRant extends Fragment {

    private Button enterRantRoom;

    public CampusRant() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_campus_rant, container, false);


        enterRantRoom = (Button)v.findViewById(R.id.enterRantRoom);

        enterRantRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rantIntent = new Intent(getContext(), RantRoom.class);
                startActivity(rantIntent);
                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        return v;
    }

}
