package com.nikki.torrents.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nikki.torrents.R;
import com.nikki.torrents.adapter.UploadSpeedAdapter;
import com.nikki.torrents.databaseModels.TorrentModel;
import com.nikki.torrents.enums.UploadDownload;
import com.nikki.torrents.interfaces.LimitInterface;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/30/2016.
 */
public class ShowUploadLimit {
    List<Integer> integers;
    TorrentModel torrentModel;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.speedCheckBox)
    CheckBox speedCheckBox;
    @InjectView(R.id.textView)
    TextView textView;
    @InjectView(R.id.container)
    View container;
    View view;
    AlertDialog alertDialog;
    UploadDownload uploadDownload;
    UploadSpeedAdapter uploadSpeedAdapter;
    int selectedSpeed;
    LimitInterface limitInterface;
    int ONE_KB=1024;
    Context context;
    public ShowUploadLimit(Context context,TorrentModel torrentModel,UploadDownload uploadDownload,LimitInterface limitInterface) {
        integers = new ArrayList<>();
        integers.add(5);
        for (int i = 5; i < 5000; ) {
            if (i < 100)
                i += 5;
            else if (i < 250)
                i += 10;
            else if (i < 2000)
                i += 25;
            else i += 100;
            integers.add(i);
        }
        this.context=context;
        this.limitInterface=limitInterface;
        this.torrentModel=torrentModel;
        view= LayoutInflater.from(context).inflate(R.layout.upload_limit,null);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.uploadDownload = uploadDownload;
        textView.setText(context.getString(R.string.unlimited));
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedSpeed = 0;
                speedCheckBox.setChecked(true);
                uploadSpeedAdapter.checkPosition = selectedSpeed;
                uploadSpeedAdapter.notifyDataSetChanged();
            }
        });
        speedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && selectedSpeed != 0) {
                    selectedSpeed = 0;
                    uploadSpeedAdapter.checkPosition = selectedSpeed;
                    uploadSpeedAdapter.notifyDataSetChanged();
                }
                if (!isChecked && selectedSpeed == 0)
                    speedCheckBox.setChecked(true);
            }
        });
        show();
    }



    void show() {

        String message;

        if(uploadDownload == UploadDownload.UPLOAD_LIMIT){
            message=context.getString(R.string.upload_limit);
            if(torrentModel.UploadLimit!=null)
                selectedSpeed=torrentModel.UploadLimit/ONE_KB;

        }else{
            message=context.getString(R.string.download_limit);
            if(torrentModel.DownloadLimit!=null)
                selectedSpeed=torrentModel.DownloadLimit/ONE_KB;
        }

        if(selectedSpeed==0)
            speedCheckBox.setChecked(true);

        recyclerView.setAdapter(uploadSpeedAdapter=new UploadSpeedAdapter(integers, new OnItemClickRecyclerListener() {
            @Override
            public void onItemClicked(View view, int position) {

                selectedSpeed=integers.get(position);
                speedCheckBox.setChecked(false);
                System.out.println(""+selectedSpeed);
            }
        },selectedSpeed));

        int scrollIndex=integers.indexOf(selectedSpeed);
        if(scrollIndex!=-1)
            recyclerView.scrollToPosition(scrollIndex);

        alertDialog=new AlertDialog.Builder(context).setTitle(message).setView(view).setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(limitInterface!=null)
                            limitInterface.selectedSpeedInBytes(selectedSpeed*ONE_KB);


                    }
                }).setNegativeButton(android.R.string.cancel, null).create();
        alertDialog.show();
    }
}
