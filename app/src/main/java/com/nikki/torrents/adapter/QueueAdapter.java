package com.nikki.torrents.adapter;

import android.content.res.Resources;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nikki.torrents.R;
import com.nikki.torrents.databaseModels.TorrentModel;
import com.nikki.torrents.enums.DownloadFinish;
import com.nikki.torrents.enums.TorrentHandleState;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;
import com.nikki.torrents.interfaces.RefreshInterface;
import com.nikki.torrents.utils.Constant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Nishi Sahlot on 3/6/2016.
 */
public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    public List<TorrentModel> torrentModels;
    public Map<String, Pair<Integer, TorrentModel>> torrentModelMap;
    OnItemClickRecyclerListener onItemClickRecyclerListener;
    Resources resources;
    DownloadFinish downloadFinish;



    public QueueAdapter(List<TorrentModel> torrentModels,
                        OnItemClickRecyclerListener onItemClickRecyclerListener, DownloadFinish downloadFinish) {
        setTorrentModels(torrentModels);
        this.onItemClickRecyclerListener = onItemClickRecyclerListener;
        this.downloadFinish = downloadFinish;
    }

    public void setTorrentModels(List<TorrentModel> torrentModels) {
        this.torrentModels = torrentModels;
        torrentModelMap = new HashMap<>();

        int index = 0;
        for (TorrentModel torrentModel : torrentModels) {
            torrentModel.initializeSpeeds();
            torrentModelMap.put(torrentModel.FilePath, new Pair<>(index, torrentModel));
            index++;
        }

    }

    public void replaceObject(TorrentModel torrentModel, int atIndex) {
        torrentModels.set(atIndex, torrentModel);
        torrentModelMap.put(torrentModel.FilePath, new Pair<>(atIndex, torrentModel));
    }

    public int addObject(TorrentModel torrentModel) {
        int index = 0;
        torrentModels.add(index, torrentModel);
        setTorrentModels(torrentModels);
        return index;
    }

    public int removeObject(TorrentModel torrentModel) {
        Pair<Integer, TorrentModel> modelPair = torrentModelMap.get(torrentModel.FilePath);
        if (modelPair != null) {
            int index = modelPair.first;
            torrentModels.remove(index);
            torrentModelMap.remove(torrentModel.FilePath);
            torrentModel.refreshInterface=null;
            return index;
        }
        return -1;

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (resources == null)
            resources = parent.getContext().getResources();

        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.queue_adapter_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setupView(position);
    }

    @Override
    public int getItemCount() {
        return torrentModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        int position;
        @InjectView(R.id.pauseResume)
        FloatingActionButton pauseResume;
        @InjectView(R.id.name)
        TextView name;
        @InjectView(R.id.progressBar)
        ProgressBar progressBar;
        @InjectView(R.id.percentage)
        TextView percentage;
        @InjectView(R.id.pauseStatus)
        TextView pauseStatus;
        @InjectView(R.id.downloadedText)
        TextView downloadedText;
        @InjectView(R.id.downloadingSpeed)
        TextView downloadingSpeed;
        @InjectView(R.id.uploadingSpeed)
        TextView uploadingSpeed;
        @InjectView(R.id.moreMenu)
        View moreMenu;
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickRecyclerListener != null)
                    onItemClickRecyclerListener.onItemClicked(v, position);
            }
        };
        RefreshInterface refreshInterface=new RefreshInterface() {
            @Override
            public void refreshView() {
                try {
                    TorrentModel torrentModel = torrentModels.get(position);
                    writeView(torrentModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(onClickListener);
            pauseStatus.setVisibility(View.GONE);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemClickRecyclerListener != null)
                        onItemClickRecyclerListener.onItemClicked(moreMenu, position);
                    return false;
                }
            });
        }

        public void setupView(int position) {
            this.position = position;

            TorrentModel torrentModel = torrentModels.get(position);

            if(torrentModel.refreshInterface==null||torrentModel.refreshInterface!=refreshInterface)
                torrentModel.refreshInterface=refreshInterface;

            writeView(torrentModel);
        }

        private void writeView(TorrentModel torrentModel){
            name.setText(torrentModel.TorrentName);


            if (downloadFinish == DownloadFinish.FINISHED) {
                progressBar.setIndeterminate(false);
                setProgress(100);
            } else {
                if (torrentModel.indeterminateProgressBar){
                    if(!progressBar.isIndeterminate())
                        progressBar.setIndeterminate(true);
                }else{
                    progressBar.setIndeterminate(false);
                    if(torrentModel.getProgress() == 0&&torrentModel.FileSize!=0){
                        setProgress((int) (torrentModel.FileDownloaded * 100 / torrentModel.FileSize));
                    }else{
                        setProgress(torrentModel.getProgress());
                    }

                }

            }


            if (torrentModel.torrentHandleState == TorrentHandleState.PAUSE) {
                pauseStatus.setVisibility(View.VISIBLE);
                pauseResume.setImageDrawable(ResourcesCompat.getDrawable(resources,
                        android.R.drawable.ic_media_play, null));
            } else {
                pauseResume.setImageDrawable(ResourcesCompat.getDrawable(resources,
                        android.R.drawable.ic_media_pause, null));
            }

            downloadedText.setText(String.format("%s/%s", Constant.byteCountToDisplaySize(torrentModel.FileDownloaded),
                    Constant.byteCountToDisplaySize(torrentModel.FileSize)));

            downloadingSpeed.setText(torrentModel.downloadingSpeed);
            uploadingSpeed.setText(torrentModel.uploadingSpeed);
        }
        private void setProgress(int progress) {
            progressBar.setProgress(progress);
            percentage.setText(String.format("%d%%", progress));
        }


        @OnClick({R.id.pauseResume, R.id.moreMenu})
        public void onViewsClicked(View view) {
            if (onItemClickRecyclerListener != null)
                onItemClickRecyclerListener.onItemClicked(view, position);
        }

    }

}
