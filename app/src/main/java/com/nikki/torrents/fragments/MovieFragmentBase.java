package com.nikki.torrents.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lapism.searchview.SearchView;
import com.nikki.torrents.R;
import com.nikki.torrents.adapter.FramentAdapter;
import com.nikki.torrents.enums.SearchEnumNormal;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 4/6/2016.
 */
public class MovieFragmentBase  extends FragmentWithBackButton{

    public MovieFragmentBase(){}
    public static Fragment newInstance(Bundle bundle) {
        MovieFragmentBase movieFragmentBase = new MovieFragmentBase();
        if (bundle != null)
            movieFragmentBase.setArguments(bundle);

        return movieFragmentBase;
    }

    AppCompatActivity appCompatActivity;
    Context context;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tabLayout)
    TabLayout tabLayout;
    @InjectView(R.id.viewPager)
    ViewPager viewPager;
    @InjectView(R.id.searchView)
    public SearchView searchView;

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
        View view=inflater.inflate(R.layout.movie_fragment_base, container, false);
        ButterKnife.inject(this,view);

        String query=null;
        Bundle bundle=getArguments();
        if(bundle!=null)
            query=bundle.getString("query");

        SearchEnumNormal searchEnumNormal=SearchEnumNormal.SEARCH_MOVIE;
        searchEnumNormal.query=query;
        bundle=new Bundle();
        bundle.putSerializable("searchEnumNormal", searchEnumNormal);
        Fragment moviesFragment=MoviesFragment.newInstance(bundle);

        searchEnumNormal=SearchEnumNormal.SEARCH_TV;
        searchEnumNormal.query=query;
        bundle=new Bundle();
        bundle.putSerializable("searchEnumNormal", searchEnumNormal);
        Fragment tvFragment=MoviesFragment.newInstance(bundle);

        viewPager.setAdapter(new FramentAdapter(getChildFragmentManager(), new Fragment[]{
                moviesFragment, tvFragment
        }, new String[]{getString(R.string.movies),
                getString(R.string.tv_shows)}));

        tabLayout.setupWithViewPager(viewPager);

        setTitle(query);

        return view;
    }
    private void setTitle(String title) {
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.getSupportActionBar().setTitle(title);
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onBackButtonPressed() {
        try {
            if (searchView.isSearchOpen()) {
                searchView.closeSearch(true);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
