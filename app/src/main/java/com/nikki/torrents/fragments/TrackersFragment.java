package com.nikki.torrents.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frostwire.jlibtorrent.TorrentInfo;
import com.nikki.torrents.R;
import com.nikki.torrents.adapter.TrackerAdapter;
import com.nikki.torrents.databaseModels.TorrentModel;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/13/2016.
 */
public class TrackersFragment extends Fragment{

    public TrackersFragment(){}
    public static Fragment newInstance(Bundle bundle) {
        TrackersFragment trackersFragment = new TrackersFragment();
        if (bundle != null)
            trackersFragment.setArguments(bundle);

        return trackersFragment;
    }


    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;


    TorrentModel torrentModel;
    Context context;
    AppCompatActivity appCompatActivity;

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
        return inflater.inflate(R.layout.tracker_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        Bundle bundle=getArguments();
        if(bundle!=null)
            torrentModel=(TorrentModel)bundle.getSerializable("torrentModel");

        if(torrentModel!=null){
            File torrentFile=new File(torrentModel.FilePath);
            TorrentInfo torrentInfo=null;
            if(torrentFile.exists())
                torrentInfo=new TorrentInfo(torrentFile);
            if(torrentInfo!=null){
                recyclerView.setAdapter(new TrackerAdapter(torrentInfo.trackers()));
            }
        }
    }
}
