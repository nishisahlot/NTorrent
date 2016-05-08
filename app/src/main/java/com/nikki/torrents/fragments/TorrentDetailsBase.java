package com.nikki.torrents.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.R;
import com.nikki.torrents.adapter.FramentAdapter;
import com.nikki.torrents.databaseModels.TorrentModel;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/12/2016.
 */
public class TorrentDetailsBase extends Fragment {

    public TorrentDetailsBase() {
    }
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tabLayout)
    TabLayout tabLayout;
    @InjectView(R.id.viewPager)
    ViewPager viewPager;
    TorrentModel torrentModel;

    public static Fragment newInstance(Bundle bundle) {
        TorrentDetailsBase torrentDetailsBase = new TorrentDetailsBase();
        if (bundle != null)
            torrentDetailsBase.setArguments(bundle);

        return torrentDetailsBase;
    }



    AppCompatActivity appCompatActivity;
    Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        appCompatActivity = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View  view=inflater.inflate(R.layout.torrent_detail_base,container,false);
        ButterKnife.inject(this, view);
        Bundle bundle=getArguments();
        if(bundle!=null)
            torrentModel=(TorrentModel)bundle.getSerializable("torrentModel");

        viewPager.setAdapter(new FramentAdapter(getChildFragmentManager(),
                new Fragment[]{TorrentDetails.newInstance(bundle),
                        TransferFragment.newInstance(bundle),
                        TrackersFragment.newInstance(bundle)},
                new String[]{getString(R.string.details),getString(R.string.status),getString(R.string.tracker)}));
        tabLayout.setupWithViewPager(viewPager);

        appCompatActivity.setSupportActionBar(toolbar);
        ActionBar actionBar = appCompatActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if(torrentModel!=null)
            actionBar.setTitle(torrentModel.TorrentName);
        }

        return view;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id=item.getItemId();
        if(id==android.R.id.home){
            MainActivity.goBack(appCompatActivity);
            return true;
        }


        return super.onOptionsItemSelected(item);

    }
}
