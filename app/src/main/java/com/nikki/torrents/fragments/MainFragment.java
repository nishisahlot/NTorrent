package com.nikki.torrents.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;
import com.nikki.torrents.R;
import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.adapter.FramentAdapter;
import com.nikki.torrents.enums.DownloadFinish;
import com.nikki.torrents.okhttp.OkHttpAsyncStringRequest;
import com.nikki.torrents.urls.Apis;
import com.nikki.torrents.utils.CheckConnection;
import com.nikki.torrents.utils.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 2/18/2016.
 */
public class MainFragment extends FragmentWithBackButton{

    public MainFragment(){}


    private ShareActionProvider shareActionProvider;
    @InjectView(R.id.searchView)
    public SearchView searchView;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tabLayout)
    TabLayout tabLayout;
    @InjectView(R.id.viewPager)
    ViewPager viewPager;


    boolean queryNotSubmit;
    SearchAdapter searchAdapter;

    List<SearchItem> typeAheadData = new ArrayList<>();



    public static Fragment newInstance(Bundle bundle) {
        MainFragment mainFragment = new MainFragment();
        if (bundle != null)
            mainFragment.setArguments(bundle);

        return mainFragment;
    }
    DrawerLayout drawer;
    AppCompatActivity appCompatActivity;
    Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        appCompatActivity = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);
        drawer=((MainActivity)appCompatActivity).drawer;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.main_fragment,container,false);
        ButterKnife.inject(this, view);

        Bundle queue=new Bundle();
        queue.putSerializable("downloadFinish", DownloadFinish.DOWNLOADING);

        Bundle finished=new Bundle();
        finished.putSerializable("downloadFinish",DownloadFinish.FINISHED);

        viewPager.setAdapter(new FramentAdapter(getChildFragmentManager(), new Fragment[]{
                DefaultTorrentz.newInstance(null), Downloads.newInstance(queue),
                Downloads.newInstance(finished)
        }, new String[]{getString(R.string.torrentz), getString(R.string.queued)
                , getString(R.string.finished)}));

        tabLayout.setupWithViewPager(viewPager);


        setSearchView();

        appCompatActivity.setSupportActionBar(toolbar);
        try {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    appCompatActivity, drawer, toolbar, R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close);
            ((MainActivity)appCompatActivity).drawer.setDrawerListener(toggle);
            toggle.syncState();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle=getArguments();
        if(bundle!=null&&bundle.getString("torrent")!=null){
            bundle.remove("torrent");
            viewPager.setCurrentItem(1,true);
        }
    }

    private void setSearchView(){
        int theme=SearchView.THEME_LIGHT;

        searchView.setStyle(SearchView.STYLE_CLASSIC);
        searchView.setTheme(theme);
        searchView.setDivider(false);
        searchView.setHint(R.string.abc_search_hint);
        searchView.setDivider(false);
        searchAdapter=new SearchAdapter(context,new ArrayList<SearchItem>(),typeAheadData,theme);
        searchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String query = typeAheadData.get(position).get_text().toString();
                searchView.setQuery(query,true);
            }
        });
        searchView.setAdapter(searchAdapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                closeSearch();
                if (!TextUtils.isEmpty(query)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("query", query);
                    MainActivity.bringFragment(appCompatActivity,
                            SearchedTorrentz.newInstance(bundle), true);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!TextUtils.isEmpty(newText)) {
                    searchTorrent(newText);

                }

                return false;
            }
        });
        searchView.setOnSearchViewListener(new SearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                typeAheadData.clear();
            }
        });
    }

    private void closeSearch(){
        searchView.closeSearch(true);

    }
    private void searchTorrent(String query){
        query=query.replaceAll("\\s+","+");
        String url= Apis.movieQuery+"q="+query;

        if(CheckConnection.checkConnection(context)){
            if(!queryNotSubmit){
                new OkHttpAsyncStringRequest(url, new OkHttpAsyncStringRequest.SetCallbacks<String>() {
                    Pattern pattern=Pattern.compile("\\[\"[^\\[]+\"]");
                    @Override
                    public void onError() {
                        queryNotSubmit=false;
                    }

                    @Override
                    public void onSuccess(String response) {
                        queryNotSubmit=false;
                        if(response!=null){
                            if(!TextUtils.isEmpty(searchView.mSearchEditText.getText())){
                                Matcher matcher=pattern.matcher(response);

                                while (matcher.find()){
                                    typeAheadData.clear();

                                    for(String suggestion:new Gson().fromJson(matcher.group(), (Class<String[]>)
                                            $Gson$Types.getRawType(new TypeToken<String[]>() {
                                            }.getType()))){
                                        typeAheadData.add(new SearchItem(R.drawable.search_ic_search_black_24dp,
                                                suggestion));
                                    }

                                    searchView.startFilter(null);
                                }
                            }

                        }
                    }
                }, Constant.getRequestHeaders(),null);
                queryNotSubmit=true;
            }


        }else{
            Toast.makeText(context, getString(R.string.internet_message), Toast.LENGTH_SHORT).show();
        }

    }

    private void showSearchView(){

        searchView.showSearch(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        setShareIntent();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        setShareIntent();
    }
    private void setShareIntent() {

        try {
            String message = getString(R.string.download_n_text)+" \n"+
                    "https://play.google.com/store/apps/details?id=" + context.getPackageName();
            String email_title = getString(R.string.n_torrent_download);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            shareIntent.setType("text/plain");

            shareIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    email_title);
            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    message);

            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(shareIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id==R.id.action_search){
            showSearchView();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onBackButtonPressed() {
        try {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }else if(searchView.isSearchOpen()){
                closeSearch();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
