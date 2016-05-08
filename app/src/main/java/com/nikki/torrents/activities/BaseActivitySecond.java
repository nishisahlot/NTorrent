package com.nikki.torrents.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdView;
import com.nikki.torrents.R;
import com.nikki.torrents.fragments.SearchedTorrentz;
import com.nikki.torrents.databaseModels.MovieModel;
import com.nikki.torrents.utils.Constant;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BaseActivitySecond extends AppCompatActivity {
    @InjectView(R.id.adView)
    AdView adView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_activity_second);
        ButterKnife.inject(this);

        Constant.loadAds(adView);

        if(savedInstanceState==null){
            Intent intent=getIntent();
            MovieModel movieModel;
            if((movieModel=(MovieModel)intent.getSerializableExtra("movieModel"))!=null){
                Bundle bundle=new Bundle();
                bundle.putString("query", movieModel.movieName);
                MainActivity.bringFragment(this,
                        SearchedTorrentz.newInstance(bundle), false);
            }
        }


    }

}
