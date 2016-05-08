package com.nikki.torrents.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.nikki.torrents.R;
import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.activities.TorrentDialog;
import com.nikki.torrents.adapter.TorrentzListAdapter;
import com.nikki.torrents.models.EmptyTorrentz;
import com.nikki.torrents.models.Torrentz;
import com.nikki.torrents.models.TorrentzWebsite;
import com.nikki.torrents.okhttp.OkHttpAsyncStringRequest;
import com.nikki.torrents.urls.Apis;
import com.nikki.torrents.utils.CheckConnection;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 2/19/2016.
 */
public class TorrentzList extends Fragment {

    public TorrentzList() {
    }
    EmptyTorrentz emptyTorrentz;
    List<Torrentz> torrentzs;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;

    public static Fragment newInstance(Bundle bundle) {
        TorrentzList torrentzList = new TorrentzList();
        if (bundle != null)
            torrentzList.setArguments(bundle);

        return torrentzList;
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
        return inflater.inflate(R.layout.torrentz_list_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));


        Bundle bundle = getArguments();
        if(bundle!=null){
            emptyTorrentz=(EmptyTorrentz)bundle.getSerializable("emptyTorrentz");
            if(emptyTorrentz!=null){
                findTorrentz();
            }
        }

        setTitle(emptyTorrentz != null ? (emptyTorrentz.softwareName != null ? emptyTorrentz.softwareName:
                getString(R.string.torrentz)):getString(R.string.torrentz));

    }

    private void setTitle(String title){
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.getSupportActionBar().setTitle(title);
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void findTorrentz(){
        if(CheckConnection.checkConnection(context)){
            String url=Apis.BASE_URL+emptyTorrentz.link;

            new OkHttpAsyncStringRequest(url, new OkHttpAsyncStringRequest.SetCallbacks<String>() {
                @Override
                public void onError() {
                    invisibleProgressBar();
                }

                @Override
                public void onSuccess(String response) {
                    invisibleProgressBar();
                    if(response!=null){
                        torrentzs= Torrentz.parseTorrentz(response);
                        recyclerView.setAdapter(new TorrentzListAdapter(torrentzs, new TorrentzListAdapter.
                                OnItemClickListener() {
                            @Override
                            public void onItemClicked(View view, int position) {
                                try {
                                    Torrentz torrentz=torrentzs.get(position);
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(torrentz.torrentzLink));
                                    startActivity(browserIntent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }));
                        parseTorrentz();
                    }
                }
            },null,null);
            visibleProgressBar();
        }
    }

    private void visibleProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void invisibleProgressBar(){
        progressBar.setVisibility(View.GONE);
    }

    private void parseTorrentz(){

        if(torrentzs!=null&&torrentzs.size()>0){
            if(CheckConnection.checkConnection(context)){
               new ParseMagnetLinks(torrentzs);
            }
        }
    }

    class ParseMagnetLinks extends AsyncTask<Void,Void,TorrentzWebsite>{

        ProgressDialog progressDialog;
        List<Torrentz> torrentzs;
        ParseMagnetLinks(List<Torrentz> torrentzs){
            this.torrentzs=torrentzs;
            execute();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=new ProgressDialog(context);
            progressDialog.setMessage(getString(R.string.parsing));
            progressDialog.show();
        }

        @Override
        protected TorrentzWebsite doInBackground(Void... params) {

            for(Torrentz torrentz:torrentzs){
                TorrentzWebsite torrentzWebsite=TorrentzWebsite.
                        parseWebsite(torrentz.torrentzLink);
                if(torrentzWebsite!=null){
                    return torrentzWebsite;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(final TorrentzWebsite torrentzWebsite) {
            super.onPostExecute(torrentzWebsite);
            try {
                if(progressDialog!=null&&progressDialog.isShowing())
                    progressDialog.dismiss();

                if(torrentzWebsite!=null){

                    new AlertDialog.Builder(context).setMessage(
                            getString(R.string.found_download)).setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent1=new Intent(appCompatActivity,TorrentDialog.class);
                            intent1.putExtra("torrent", torrentzWebsite.magnetUrl);
                            startActivity(intent1);

                        }
                    }).setNegativeButton(android.R.string.cancel,null).
                            create().show();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
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


