package com.nikki.torrents.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.nikki.torrents.R;
import com.nikki.torrents.models.EmptyTorrentz;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 2/18/2016.
 */
public class EmptyTorrentzAdapter extends RecyclerView.Adapter<EmptyTorrentzAdapter.ViewHolder>{

    public final List<EmptyTorrentz> emptyTorrentzs;
    OnItemClickListener onItemClickListener;
    LoadPaging loadPaging;
    public interface LoadPaging{
        void loadMoreData();
    }
    public interface OnItemClickListener{
        void onItemClicked(View view,int position);
    }

    public EmptyTorrentzAdapter(List<EmptyTorrentz> emptyTorrentzs,OnItemClickListener onItemClickListener,LoadPaging loadPaging){
        this.emptyTorrentzs=emptyTorrentzs;
        this.onItemClickListener=onItemClickListener;
        this.loadPaging=loadPaging;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.empty_torrentz_adapter_view,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setupView(position);
    }

    @Override
    public int getItemCount() {
        return emptyTorrentzs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        int position;

        @InjectView(R.id.software)
        TextView software;
        @InjectView(R.id.time)
        TextView time;
        @InjectView(R.id.size)
        TextView size;
        @InjectView(R.id.seeders)
        TextView seeders;
        @InjectView(R.id.leechers)
        TextView leechers;
        @InjectView(R.id.ratingBar)
        RatingBar ratingBar;

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
            EmptyTorrentz emptyTorrentz=emptyTorrentzs.get(position);

            software.setText(emptyTorrentz.softwareName);
            time.setText(emptyTorrentz.date);
            size.setText(emptyTorrentz.size);
            seeders.setText(emptyTorrentz.seeders);
            leechers.setText(emptyTorrentz.leechers);

            try {
                int rating=Integer.parseInt(emptyTorrentz.rating);
                ratingBar.setRating(rating);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            int size=emptyTorrentzs.size();

            if(position>size-3)
                if(loadPaging!=null)
                    loadPaging.loadMoreData();
        }
    }

}
