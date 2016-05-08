package com.nikki.torrents.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.frostwire.jlibtorrent.Priority;
import com.nikki.torrents.R;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;
import com.nikki.torrents.models.FileMetadata;
import com.nikki.torrents.utils.Constant;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/26/2016.
 */
public class FilesSelectAdapter extends RecyclerView.Adapter<FilesSelectAdapter.ViewHolder>{
    List<FileMetadata> fileMetadataList;
    OnItemClickRecyclerListener onItemClickRecyclerListener;
    public FilesSelectAdapter(List<FileMetadata> fileMetadataList,OnItemClickRecyclerListener onItemClickRecyclerListener){
        this.fileMetadataList=fileMetadataList;
        this.onItemClickRecyclerListener=onItemClickRecyclerListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.file_select_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setupView(position);
    }

    @Override
    public int getItemCount() {
        return fileMetadataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        int position;
        @InjectView(R.id.checkBox)
        CheckBox checkBox;
        @InjectView(R.id.fileName)
        TextView fileName;
        @InjectView(R.id.fileSize)
        TextView fileSize;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickRecyclerListener != null)
                        onItemClickRecyclerListener.onItemClicked(v, position);

                }
            });
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickRecyclerListener != null)
                        onItemClickRecyclerListener.onItemClicked(v, position);
                }
            });

        }
        public void setupView(int position){
            this.position=position;
            FileMetadata fileMetadata=fileMetadataList.get(position);
            fileName.setText(fileMetadata.fileName);
            fileSize.setText(Constant.byteCountToDisplaySize(fileMetadata.size));
            if(fileMetadata.priority!= Priority.IGNORE)
                checkBox.setChecked(true);
            else
                checkBox.setChecked(false);
        }
    }
}
