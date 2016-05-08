package com.nikki.torrents.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Nishi Sahlot on 3/2/2016.
 */
public class FramentAdapter  extends FragmentPagerAdapter{
    Fragment[] fragments;
    String[] pageTitle;
    public FramentAdapter(FragmentManager fm,Fragment[] fragments,String[] pageTitle) {
        super(fm);
        this.fragments=fragments;
        this.pageTitle=pageTitle;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitle[position];
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment=fragments[position];
        Bundle bundle=fragment.getArguments();
        if(bundle==null){
            bundle=new Bundle();
            fragment.setArguments(bundle);
        }
        bundle.putInt("position",position);

        return fragment;
    }

    @Override
    public int getCount() {
        return fragments.length;
    }
}
