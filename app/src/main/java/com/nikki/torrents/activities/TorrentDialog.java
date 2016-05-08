package com.nikki.torrents.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.nikki.torrents.R;
import com.nikki.torrents.fragments.TorrentDialogProgress;

import butterknife.ButterKnife;

public class TorrentDialog extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torrent_dialog);
        ButterKnife.inject(this);


        if(savedInstanceState==null){
            Bundle bundle=new Bundle();
            bundle.putString("torrent",getIntent().getStringExtra("torrent"));
            MainActivity.bringFragment(this, TorrentDialogProgress.newInstance(bundle),false);
        }


    }



}
