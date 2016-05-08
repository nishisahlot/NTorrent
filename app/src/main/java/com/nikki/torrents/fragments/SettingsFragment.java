package com.nikki.torrents.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.nikki.torrents.R;
import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.adapter.SettingsAdapter;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/27/2016.
 */
public class SettingsFragment extends Fragment {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    public static Fragment newInstance(Bundle bundle) {
        SettingsFragment settingsFragment = new SettingsFragment();
        if (bundle != null)
            settingsFragment.setArguments(bundle);

        return settingsFragment;
    }
    AppCompatActivity appCompatActivity;
    Context context;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
        appCompatActivity=(AppCompatActivity)getActivity();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new SettingsAdapter(context, new OnItemClickRecyclerListener() {
            @Override
            public void onItemClicked(View view, int position) {
                switch (position){
                    case 0:
                        MainActivity.bringFragment(appCompatActivity,LimitSettings.newInstance(null),true);
                        break;
                    case 1:
                        MainActivity.bringFragment(appCompatActivity,NetworkSettings.newInstance(null),true);
                        break;
                }
            }
        }));
        setTitle(getString(R.string.action_settings));
    }
    private void setTitle(String title){
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.getSupportActionBar().setTitle(title);
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
