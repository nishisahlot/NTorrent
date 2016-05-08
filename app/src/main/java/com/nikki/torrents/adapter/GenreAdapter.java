package com.nikki.torrents.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.nikki.torrents.R;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;
import com.nikki.torrents.models.GenreModel;

import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 4/5/2016.
 */
public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder>{

    public Set<String> selectedGenres;
    public List<GenreModel> genreModels;
    OnItemClickRecyclerListener onItemClickRecyclerListener;
    public GenreAdapter(Set<String> selectedGenres,List<GenreModel> genreModels,OnItemClickRecyclerListener onItemClickRecyclerListener){
        this.selectedGenres=selectedGenres;
        this.genreModels=genreModels;
        this.onItemClickRecyclerListener=onItemClickRecyclerListener;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.genre_adapter_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setupView(position);
    }

    @Override
    public int getItemCount() {
        return genreModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        int position;
        @InjectView(R.id.checkedTextView)
        CheckedTextView checkedTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            checkedTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickRecyclerListener!=null)
                        onItemClickRecyclerListener.onItemClicked(v,position);
                }
            });
        }

        void setupView(int position){
            this.position=position;
            GenreModel genreModel=genreModels.get(position);
            checkedTextView.setText(genreModel.genreText);
            if(selectedGenres.contains(genreModel.genreId))
                checkedTextView.setChecked(true);
            else
                checkedTextView.setChecked(false);
        }
    }
}
