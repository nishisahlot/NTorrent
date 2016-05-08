package com.nikki.torrents.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by Nishi Sahlot on 3/2/2016.
 */
public class SearchedTorrentz extends Fragment {

    public SearchedTorrentz(){}


    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.errorText)
    TextView errorText;
    List<EmptyTorrentz> emptyTorrentzs;
    EmptyTorrentzAdapter emptyTorrentzAdapter;
    public static Fragment newInstance(Bundle bundle) {
        SearchedTorrentz searchedTorrentz = new SearchedTorrentz();
        if (bundle != null)
            searchedTorrentz.setArguments(bundle);

        return searchedTorrentz;
    }
    String query;


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

        View view=inflater.inflate(R.layout.torrentz_list_fragment,container,false);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        Bundle bundle=getArguments();

        if(bundle!=null)
            query=bundle.getString("query");
        setAdapter();
        getEmptyTorrentz(query);
        setTitle(query != null ? query : getString(R.string.torrentz));
        return view;
    }
    private void setTitle(String title){
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.getSupportActionBar().setTitle(title);
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void visibleProgressBar(){
        try {
            progressBar.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void invisibleProgressBar(){
        try {
            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getEmptyTorrentz(final String query){
        errorText.setVisibility(View.GONE);
        if(CheckConnection.checkConnection(context)){

            if(emptyTorrentzs==null||emptyTorrentzs.size()==0){
                new OkHttpAsyncStringRequest(Apis.emptySearch+(query!=null?query:""),
                        new OkHttpAsyncStringRequest.SetCallbacks<String>() {
                    @Override
                    public void onError() {
                        invisibleProgressBar();
                        queryOnGoogle(appCompatActivity,query);
                    }

                    @Override
                    public void onSuccess(String response) {
                        invisibleProgressBar();
                        if(response!=null){
                            emptyTorrentzs= EmptyTorrentz.parseTorrentz(response);
                            if(emptyTorrentzs==null||emptyTorrentzs.size()==0)
                                queryOnGoogle(appCompatActivity,query);
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

    public static void queryOnGoogle(AppCompatActivity appCompatActivity,String query){
        if(!TextUtils.isEmpty(query)){
            try {
                String url="https://www.google.co.in/search?q="+query+"+torrent";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(url));
                appCompatActivity.startActivity(intent);
                MainActivity.goBack(appCompatActivity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showErrorText(String message) {
        try {
            if (emptyTorrentzs == null || emptyTorrentzs.size() == 0) {
                errorText.setVisibility(View.VISIBLE);
                errorText.setText(message);
            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setAdapter(){
        if(emptyTorrentzs!=null&&emptyTorrentzs.size()>0){
            recyclerView.setAdapter(emptyTorrentzAdapter = new EmptyTorrentzAdapter(emptyTorrentzs, new
                    EmptyTorrentzAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClicked(View view, int position) {
                            EmptyTorrentz emptyTorrentz = emptyTorrentzAdapter.
                                    emptyTorrentzs.get(position);

                            Bundle bundle = new Bundle();
                            bundle.putSerializable("emptyTorrentz", emptyTorrentz);

                            MainActivity.bringFragment(appCompatActivity,
                                    TorrentzList.newInstance(bundle), true);
                        }
                    },null));
        }
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

