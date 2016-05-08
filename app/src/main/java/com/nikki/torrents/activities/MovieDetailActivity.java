package com.nikki.torrents.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdView;
import com.nikki.torrents.R;
import com.nikki.torrents.fragments.MovieDetailFragment;
import com.nikki.torrents.fragments.MoviesFragment;
import com.nikki.torrents.utils.Constant;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MovieDetailActivity extends AppCompatActivity {
    @InjectView(R.id.adView)
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.inject(this);

        Constant.loadAds(adView);

        if(savedInstanceState==null){
            Intent intent=getIntent();

            Bundle bundle=new Bundle();
            bundle.putSerializable("movieModel",intent.getSerializableExtra("movieModel"));
            bundle.putInt(MoviesFragment.EXTRA_MOVIE_POSITION, intent.getIntExtra(MoviesFragment.EXTRA_MOVIE_POSITION, 0));

            MainActivity.bringFragment(this, MovieDetailFragment.newInstance(bundle),false);
        }

    }



}
