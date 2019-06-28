package com.blackviking.campusrush.SearchFunction;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.blackviking.campusrush.SearchFunction.ResultFragments.ResultAward;
import com.blackviking.campusrush.SearchFunction.ResultFragments.ResultGame;
import com.blackviking.campusrush.SearchFunction.ResultFragments.ResultRant;
import com.blackviking.campusrush.SearchFunction.ResultFragments.ResultSkit;
import com.blackviking.campusrush.SearchFunction.ResultFragments.ResultUser;

/**
 * Created by Scarecrow on 3/9/2018.
 */

public class TabsPager extends FragmentStatePagerAdapter {

    String[] titles = new String[]{"", "", "", "", ""};

    public TabsPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){

            case 0:
                ResultUser resultUser = new ResultUser();
                return resultUser;
            case 1:
                ResultAward resultAward = new ResultAward();
                return resultAward;
            case 2:
                ResultRant resultRant = new ResultRant();
                return resultRant;
            case 3:
                ResultGame resultGame = new ResultGame();
                return resultGame;
            case 4:
                ResultSkit resultSkit = new ResultSkit();
                return resultSkit;
        }

        return null;
    }

    @Override
    public int getCount() {
        return 5;
    }
}
