package com.nikki.torrents.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nikki.torrents.R;
import com.nikki.torrents.databaseModels.TorrentModel;
import com.nikki.torrents.downloadService.ListenerSingleton;
import com.nikki.torrents.enums.DownloadState;
import com.nikki.torrents.enums.TorrentHandleState;
import com.nikki.torrents.interfaces.TorrentServiceListener;
import com.nikki.torrents.utils.Constant;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/12/2016.
 */
public class TransferFragment extends Fragment {
    public TransferFragment(){}
    public static Fragment newInstance(Bundle bundle) {
        TransferFragment transferFragment = new TransferFragment();
        if (bundle != null)
            transferFragment.setArguments(bundle);

        return transferFragment;
    }
    TorrentModel torrentModel;



    @InjectView(R.id.downloaded)
    TextView downloaded;
    @InjectView(R.id.uploaded)
    TextView uploaded;
    @InjectView(R.id.downloadingSpeed)
    TextView downloadingSpeed;
    @InjectView(R.id.uploadingSpeed)
    TextView uploadingSpeed;
    @InjectView(R.id.DownLimit)
    TextView DownLimit;
    @InjectView(R.id.upLimit)
    TextView upLimit;
    @InjectView(R.id.seeds)
    TextView seeds;
    @InjectView(R.id.peers)
    TextView peers;
    @InjectView(R.id.shareRatio)
    TextView shareRatio;
    @InjectView(R.id.status)
    TextView status;

    String key;

    Context context;
    AppCompatActivity appCompatActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        appCompatActivity = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);
        key=getClass().getName();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transfer_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this,view);
        Bundle bundle=getArguments();
        if(bundle!=null)
            torrentModel=(TorrentModel)bundle.getSerializable("torrentModel");

        setData();
        setListener(torrentServiceListener);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListener(null);
    }
    TorrentServiceListener torrentServiceListener=new TorrentServiceListener() {
        @Override
        public void updateBlockProgress(TorrentModel torrentModelLocal) {

            try {
                if(torrentModel!=null&&torrentModelLocal.FilePath.equals(torrentModel.FilePath)){
                    torrentModel=torrentModelLocal;
                    setData();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void downloadFinish(TorrentModel torrentModel) {

        }

        @Override
        public void torrentAdded(TorrentModel torrentModel) {

        }
    };

    private void setListener(TorrentServiceListener torrentServiceListener){
        try {
            if (torrentServiceListener == null) {
                ListenerSingleton.getInstance().serviceListenerMap.remove(key);
            } else {
                ListenerSingleton.getInstance().serviceListenerMap.put(key, torrentServiceListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setData(){
        if(torrentModel!=null){
            downloaded.setText(Constant.byteCountToDisplaySize(torrentModel.FileDownloaded));
            uploaded.setText(Constant.byteCountToDisplaySize(torrentModel.FileUploaded));
            if(torrentModel.FileDownloaded!=0)
            shareRatio.setText(String.format("%.3f", (float)torrentModel.FileUploaded / torrentModel.FileDownloaded));

            status.setText((torrentModel.torrentHandleState==null||
                    torrentModel.torrentHandleState== TorrentHandleState.PAUSE)?
                    getString(R.string.stopped):(torrentModel.DownloadState!=null&&torrentModel.DownloadState== DownloadState.COMPLTE_DOWNLOADED)?
            getString(R.string.seeding):getString(R.string.downloading));
            if(torrentModel.DownloadLimit!=null){
                DownLimit.setText(String.format("%s/s", Constant.byteCountToDisplaySize(torrentModel.DownloadLimit)));
            }else{
                DownLimit.setText(context.getString(R.string.unlimited));
            }
            if(torrentModel.UploadLimit!=null){
                upLimit.setText(String.format("%s/s", Constant.byteCountToDisplaySize(torrentModel.UploadLimit)));
            }else{
                upLimit.setText(context.getString(R.string.unlimited));
            }
            if(torrentModel.torrentHandleState==null||torrentModel.torrentHandleState==TorrentHandleState.PAUSE){
                seeds.setText(String.format("0 of %d connected", torrentModel.TotalSeeds));
                peers.setText(String.format("0 of %d connected", torrentModel.TotalPeers));
            }else{
                seeds.setText(String.format("%d of %d connected", torrentModel.connectedSeeders, torrentModel.TotalSeeds));
                peers.setText(String.format("%d of %d connected", torrentModel.connectedPeers, torrentModel.TotalPeers));
            }
            downloadingSpeed.setText(torrentModel.downloadingSpeed);
            uploadingSpeed.setText(torrentModel.uploadingSpeed);
        }

    }



}
