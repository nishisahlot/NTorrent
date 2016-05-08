package com.nikki.torrents.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nikki.torrents.R;
import com.nikki.torrents.databaseModels.TorrentModel;
import com.nikki.torrents.models.TorrentDetailsModel;
import com.nikki.torrents.utils.Constant;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Nishi Sahlot on 3/12/2016.
 */
public class TorrentDetails extends Fragment{
    public TorrentDetails(){}

    public static Fragment newInstance(Bundle bundle) {
        TorrentDetails torrentDetails = new TorrentDetails();
        if (bundle != null)
            torrentDetails.setArguments(bundle);

        return torrentDetails;
    }
    @InjectView(R.id.name)
    TextView name;
    @InjectView(R.id.storagePath)
    TextView storagePath;
    @InjectView(R.id.totalSize)
    TextView totalSize;
    @InjectView(R.id.numberOfFiles)
    TextView numberOfFiles;
    @InjectView(R.id.dateAdded)
    TextView dateAdded;
    @InjectView(R.id.createdDate)
    TextView createdDate;
    @InjectView(R.id.dateAddedContainer)
    View dateAddedContainer;





    TorrentDetailsModel torrentDetailsModel;


    TorrentModel torrentModel;


    AppCompatActivity appCompatActivity;
    Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        appCompatActivity = (AppCompatActivity) getActivity();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.torrent_details_fragment,container,false);
        ButterKnife.inject(this, view);
        Bundle bundle=getArguments();
        if(bundle!=null)
            torrentModel=(TorrentModel)bundle.getSerializable("torrentModel");

        if(torrentModel!=null){
            if(torrentDetailsModel==null){
                torrentDetailsModel=TorrentDetailsModel.getTorrentDetailsModel(torrentModel);
                if(torrentDetailsModel!=null){
                    setData(torrentDetailsModel);

                }
            }else{
                setData(torrentDetailsModel);
            }

        }

        return view;
    }

    private void setData(TorrentDetailsModel torrentDetailsModel){
        name.setText(torrentDetailsModel.name);
        storagePath.setText(torrentDetailsModel.storagePath);
        totalSize.setText(torrentDetailsModel.totalSize);
        numberOfFiles.setText(torrentDetailsModel.numberOfFiles);
        dateAddedContainer.setVisibility(View.GONE);
        if(!TextUtils.isEmpty(torrentDetailsModel.dateAdded)){
            dateAddedContainer.setVisibility(View.VISIBLE);
            dateAdded.setText(torrentDetailsModel.dateAdded);

        }
        createdDate.setText(torrentDetailsModel.torrentCreatedDate);
    }



    @OnClick({R.id.downloadLocationContainer,R.id.numberOfFilesContainer})
    public void onViewClicked(View view){
        int id=view.getId();
        if(id==R.id.downloadLocationContainer||id==R.id.numberOfFilesContainer){
            Constant.openFolder(appCompatActivity, new File(torrentModel.FilePath).getParent());
        }
    }




}
