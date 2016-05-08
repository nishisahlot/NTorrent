package com.nikki.torrents.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.nikki.torrents.R;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 4/15/2016.
 */
public class DirectoryExplorerAdapter extends RecyclerView.Adapter<DirectoryExplorerAdapter.ViewHolder>{

    public File[] file;
    Resources resources;
    OnItemClickRecyclerListener onItemClickRecyclerListener;
    public DirectoryExplorerAdapter(File[] file,OnItemClickRecyclerListener onItemClickRecyclerListener){
        this.file=file;
        this.onItemClickRecyclerListener=onItemClickRecyclerListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(resources==null)
            resources=parent.getContext().getResources();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.directory_explorer_adapter_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setupView(position);
    }

    @Override
    public int getItemCount() {
        return file.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        int position;
        @InjectView(R.id.fileText)
        TextView fileText;
        @InjectView(R.id.button)
        Button button;
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickRecyclerListener!=null)
                    onItemClickRecyclerListener.onItemClicked(v,position);
            }
        };
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(onClickListener);
            button.setOnClickListener(onClickListener);
        }
        public void setupView(int position){
            this.position=position;
            File fileLocal=file[position];
            fileText.setText(fileLocal.getName());

            if(fileLocal.isDirectory())
                button.setText(resources.getString(R.string.directory));
            else
                button.setText(resources.getString(R.string.file));
        }
    }
}
