package com.nikki.torrents.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frostwire.jlibtorrent.AnnounceEntry;
import com.nikki.torrents.R;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/13/2016.
 */
public class TrackerAdapter extends RecyclerView.Adapter<TrackerAdapter.ViewHolder>{
    List<AnnounceEntry> announceEntries;

    public TrackerAdapter(List<AnnounceEntry> announceEntries){
        this.announceEntries=announceEntries;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.tracker_adapter, parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setupView(position);
    }

    @Override
    public int getItemCount() {
        return announceEntries.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        int position;
        @InjectView(R.id.announceName)
        TextView announceName;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this,itemView);
        }
        public void setupView(int position){
            this.position=position;
            try {
                AnnounceEntry announceEntry=announceEntries.get(position);
                announceName.setText(announceEntry.getUrl());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
