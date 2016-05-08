package com.nikki.torrents.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nikki.torrents.R;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;
import com.nikki.torrents.models.BaseFile;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/24/2016.
 */
public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.ViewHolder>{

    public List<BaseFile> baseFiles;
    OnItemClickRecyclerListener onItemClickRecyclerListener;
    public DirectoryAdapter(List<BaseFile> baseFiles,OnItemClickRecyclerListener onItemClickRecyclerListener){
        this.baseFiles=baseFiles;
        this.onItemClickRecyclerListener=onItemClickRecyclerListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.directory_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setupView(position);
    }

    @Override
    public int getItemCount() {
        return baseFiles.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        int position;
        @InjectView(R.id.directory)
        TextView directory;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickRecyclerListener!=null)
                        onItemClickRecyclerListener.onItemClicked(v,position);
                }
            });
        }

        public void setupView(int position){
            this.position=position;
            BaseFile baseFile=baseFiles.get(position);
            directory.setText(baseFile.name);
        }

    }
}
