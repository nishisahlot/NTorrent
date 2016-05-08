package com.nikki.torrents.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nikki.torrents.R;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/28/2016.
 */
public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder>{
    String[] items;
    OnItemClickRecyclerListener onItemClickRecyclerListener;
    public SettingsAdapter(Context context,OnItemClickRecyclerListener onItemClickRecyclerListener){
        items=context.getResources().getStringArray(R.array.settings);
        this.onItemClickRecyclerListener=onItemClickRecyclerListener;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_adapter,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setupView(position);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        int position;
        @InjectView(R.id.textView)
        TextView textView;
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
            textView.setText(items[position]);
        }
    }
}
