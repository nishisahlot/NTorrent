package com.nikki.torrents.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.nikki.torrents.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Nishi Sahlot on 4/1/2016.
 */
public class NetworkSettings extends Fragment {
    Context context;
    AppCompatActivity appCompatActivity;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.portNumber)
    TextView portNumber;

    public static Fragment newInstance(Bundle bundle){

        NetworkSettings networkSettings=new NetworkSettings();
        if(bundle!=null)networkSettings.setArguments(bundle);

        return networkSettings;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context =context;
        appCompatActivity =(AppCompatActivity)getActivity();
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.network_settings,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @OnClick({R.id.portSet})
    public void onViewClicked(View view){
        int id=view.getId();
        if(id==R.id.portSet){
            new ShowPortDialog();
        }
    }

    class ShowPortDialog{
        @InjectView(R.id.portNumber)
        EditText portNumber;
        View view;
        ShowPortDialog(){
            view=LayoutInflater.from(context).inflate(R.layout.port_dialog,null);
            ButterKnife.inject(this,view);
            show();
        }

        private void show(){
            new AlertDialog.Builder(context).setView(view).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).setNegativeButton(android.R.string.cancel,null).setTitle(R.string.tcp_port).create().show();
        }
    }
}
