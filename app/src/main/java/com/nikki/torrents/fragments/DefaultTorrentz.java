package com.nikki.torrents.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nikki.torrents.R;
import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.adapter.EmptyTorrentzAdapter;
import com.nikki.torrents.models.EmptyTorrentz;
import com.nikki.torrents.okhttp.OkHttpAsyncStringRequest;
import com.nikki.torrents.urls.Apis;
import com.nikki.torrents.utils.CheckConnection;
import com.nikki.torrents.utils.Constant;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Nishi Sahlot on 3/2/2016.
 */
public class DefaultTorrentz  extends Fragment {

    public DefaultTorrentz(){}


    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.torrentLayout)
    View torrentLayout;
    @InjectView(R.id.searchImage)
    ImageView searchImage;
    List<EmptyTorrentz> emptyTorrentzs;
    EmptyTorrentzAdapter emptyTorrentzAdapter;
    int page;
    boolean noMoreDataAvailable;
    public static Fragment newInstance(Bundle bundle) {
        DefaultTorrentz defaultTorrentz = new DefaultTorrentz();
        if (bundle != null)
            defaultTorrentz.setArguments(bundle);

        return defaultTorrentz;
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
        View view=inflater.inflate(R.layout.content_main, container, false);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        if(emptyTorrentzAdapter!=null)
            recyclerView.setAdapter(emptyTorrentzAdapter);
        setAdapter();
        getEmptyTorrentz(false);
        searchImage.setColorFilter(getResources().getColor(R.color.colorAccent),
                PorterDuff.Mode.MULTIPLY);
        return view;
    }

    @OnClick({R.id.torrentLayout})
    public void onViewClicked(View view){
        int id=view.getId();
        if(id==R.id.torrentLayout){

           if(getParentFragment()!=null&&getParentFragment() instanceof MainFragment){
               MainFragment mainFragment=(MainFragment)getParentFragment();
               mainFragment.searchView.showSearch(true);
           }
        }
    }

    private void visibleProgressBar(){
        //if(emptyTorrentzs==null||emptyTorrentzs.size()==0)
        progressBar.setVisibility(View.VISIBLE);
    }

    private void invisibleProgressBar(){
        progressBar.setVisibility(View.GONE);
    }

    private void getEmptyTorrentz(boolean force){
        torrentLayout.setVisibility(View.GONE);
        if(CheckConnection.checkConnection(context)){
            if(emptyTorrentzs==null||emptyTorrentzs.size()==0||force){

                if(emptyTorrentzs==null||emptyTorrentzs.size()==0){
                    noMoreDataAvailable=false;
                    page=0;
                }

                String url=Apis.emptySearch;
                if(page!=0){
                    url=Apis.torrentSearchPage+page;
                    try {
                        Snackbar.make(getView(), getString(R.string.loading_data), Snackbar.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }



                new OkHttpAsyncStringRequest(url, new OkHttpAsyncStringRequest.SetCallbacks<String>() {
                    @Override
                    public void onError() {
                        noMoreDataAvailable=true;
                        invisibleProgressBar();
                        showErrorText(getString(R.string.technical_difficulty));
                    }

                    @Override
                    public void onSuccess(String response) {
                        invisibleProgressBar();
                        if(response!=null){
                            List<EmptyTorrentz> emptyTorrentzs= EmptyTorrentz.parseTorrentz(response);
                            if(emptyTorrentzs==null||emptyTorrentzs.size()==0){
                                noMoreDataAvailable=true;
                            }else{
                                noMoreDataAvailable=false;
                                setMovieModels(emptyTorrentzs);
                            }


                            setAdapter();

                        }
                    }
                }, Constant.getRequestHeaders(),null);
                visibleProgressBar();
            }

        }else{
            showErrorText(getString(R.string.internet_message));
        }

    }

    private void setMovieModels(List<EmptyTorrentz> localMovieModels) {
        page++;
        if (emptyTorrentzs == null)
            emptyTorrentzs = localMovieModels;
        else
            emptyTorrentzs.addAll(localMovieModels);
    }

    private void showErrorText(String message) {
        if(emptyTorrentzs==null||emptyTorrentzs.size()==0)
        torrentLayout.setVisibility(View.VISIBLE);
    }
    private void setAdapter(){
        if(emptyTorrentzs!=null&&emptyTorrentzs.size()>0){
            torrentLayout.setVisibility(View.GONE);
            if(emptyTorrentzAdapter==null){
                recyclerView.setAdapter(emptyTorrentzAdapter=new EmptyTorrentzAdapter(emptyTorrentzs, new
                        EmptyTorrentzAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClicked(View view, int position) {
                                EmptyTorrentz emptyTorrentz = emptyTorrentzAdapter.emptyTorrentzs.get(position);

                                Bundle bundle = new Bundle();
                                bundle.putSerializable("emptyTorrentz", emptyTorrentz);

                                MainActivity.bringFragment(appCompatActivity,
                                        TorrentzList.newInstance(bundle), true);
                            }
                        }, new EmptyTorrentzAdapter.LoadPaging() {
                    @Override
                    public void loadMoreData() {
                        if (!noMoreDataAvailable)
                            getEmptyTorrentz(true);
                    }
                }));
            }else{
                emptyTorrentzAdapter.notifyDataSetChanged();
            }

        }else{
            showErrorText(getString(R.string.internet_message));
        }

    }


}
