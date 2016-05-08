package com.nikki.torrents.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nikki.torrents.R;
import com.nikki.torrents.models.Torrentz;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 2/21/2016.
 */
public class TorrentzListAdapter  extends RecyclerView.Adapter<TorrentzListAdapter.ViewHolder>{

    public List<Torrentz> torrentzs;
    OnItemClickListener onItemClickListener;

    public interface OnItemClickListener{
        void onItemClicked(View view,int position);
    }

    public TorrentzListAdapter(List<Torrentz> torrentzs,OnItemClickListener onItemClickListener){
        this.torrentzs = torrentzs;
        this.onItemClickListener=onItemClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.torrentz_list_adapter,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setupView(position);
    }

    @Override
    public int getItemCount() {
        return torrentzs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        int position;

        @InjectView(R.id.torrentz)
        TextView torrentz;
        @InjectView(R.id.torrentzName)
        TextView torrentzName;
        @InjectView(R.id.time)
        TextView time;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickListener!=null)
                        onItemClickListener.onItemClicked(v,position);
                }
            });
        }
        public void setupView(int position){
            this.position=position;
            Torrentz emptyTorrentz= torrentzs.get(position);
            torrentz.setText(emptyTorrentz.torrentzSiteName);
            torrentzName.setText(emptyTorrentz.movieName);
            time.setText(emptyTorrentz.time);

        }
    }

}

