package com.blackviking.campusrush.Plugins.GamersHub;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.blackviking.campusrush.Plugins.GamersHub.GamersFragments.GameFeed;
import com.blackviking.campusrush.Plugins.GamersHub.GamersFragments.Trending;


/**
 * Created by Scarecrow on 3/9/2018.
 */

public class GamersTabsPager extends FragmentStatePagerAdapter {

    String[] titles = new String[]{"TRENDING", "GAME FEED"};

    public GamersTabsPager(FragmentManager fm) {
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
                Trending trending = new Trending();
                return trending;
            case 1:
                GameFeed gameFeed = new GameFeed();
                return gameFeed;
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
