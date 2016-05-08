package com.nikki.torrents.adapter;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.nikki.torrents.R;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;
import com.nikki.torrents.databaseModels.MovieModel;
import com.nikki.torrents.utils.CustomNetworkImageView;
import com.nikki.torrents.volley.MySingleton;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/13/2016.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>{

    final public List<MovieModel> movieModels;
    ImageLoader imageLoader;
    OnItemClickRecyclerListener onItemClickRecyclerListener;
    LoadPaging loadPaging;
    public interface LoadPaging{
        void loadMoreData();
    }
    public MovieAdapter(List<MovieModel> movieModels,OnItemClickRecyclerListener onItemClickRecyclerListener,LoadPaging loadPaging){
        this.movieModels=movieModels;
        this.onItemClickRecyclerListener=onItemClickRecyclerListener;
        this.loadPaging=loadPaging;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(imageLoader==null)
            imageLoader= MySingleton.getInstance(parent.getContext()).getImageLoader();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.movie_adapter_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setupView(position);
    }

    @Override
    public int getItemCount() {
        return movieModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        int position;
        @InjectView(R.id.movieName)
        TextView movieName;
        @InjectView(R.id.movieCategory)
        TextView movieCategory;
        @InjectView(R.id.year)
        TextView year;
        @InjectView(R.id.poster)
        CustomNetworkImageView poster;
        @InjectView(R.id.ratingBar)
        RatingBar ratingBar;
        @InjectView(R.id.ratingText)
        TextView ratingText;
        @InjectView(R.id.footerView)
        View footerView;
        @InjectView(R.id.container)
        View container;
        @InjectView(R.id.deleteBookmarked)
        ImageButton deleteBookmarked;
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
            container.setOnClickListener(onClickListener);
            deleteBookmarked.setOnClickListener(onClickListener);
            poster.imageLoadListener=new CustomNetworkImageView.ImageLoadListener() {

                @Override
                public void onImageLoaded(Bitmap myBitmap) {
                    try {

                        MovieModel movieModel=movieModels.get(position);
                        if (movieModel.palette==null && myBitmap != null && !myBitmap.isRecycled()) {
                            Palette.from(myBitmap).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    try {
                                        MovieModel movieModel=movieModels.get(position);
                                        movieModel.palette=palette;
                                        setPalette(movieModel);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        }
        public void setupView(int position){
            this.position=position;
            MovieModel movieModel=movieModels.get(position);

            movieName.setText(movieModel.movieName);
            movieCategory.setText(movieModel.movieCategory);
            year.setText(movieModel.movieReleaseYear);

            int size;
            if((size=movieModel.poster.size())!=0)
                poster.setImageUrl(movieModel.poster.get(size==3?1:0), imageLoader);

            poster.setDefaultImageResId(R.mipmap.default_image);
            poster.setErrorImageResId(R.mipmap.default_image);

            ratingText.setText(movieModel.movieRating);
            ratingBar.setRating(1);

            setPalette(movieModel);

            if(movieModel.getId()!=null)
                deleteBookmarked.setVisibility(View.VISIBLE);
            else
                deleteBookmarked.setVisibility(View.GONE);



            size=movieModels.size();

            if(position>size-3)
                if(loadPaging!=null)
                    loadPaging.loadMoreData();
        }

        private void setPalette(MovieModel movieModel){
            try {
                if(movieModel.palette!=null){
                    Palette.Swatch darkVibrantSwatch    = movieModel.palette.getDarkVibrantSwatch();
                    Palette.Swatch darkMutedSwatch      = movieModel.palette.getDarkMutedSwatch();
                    Palette.Swatch lightVibrantSwatch   = movieModel.palette.getLightVibrantSwatch();
                    Palette.Swatch lightMutedSwatch     = movieModel.palette.getLightMutedSwatch();
                    Palette.Swatch backgroundAndContentColors = (darkVibrantSwatch != null)
                            ? darkVibrantSwatch : darkMutedSwatch;
                    Palette.Swatch titleAndFabColors = (darkVibrantSwatch != null)
                            ? lightVibrantSwatch : lightMutedSwatch;

                    if(backgroundAndContentColors!=null){
                        footerView.setBackgroundColor(backgroundAndContentColors.getRgb());
                        movieName.setTextColor(backgroundAndContentColors.getTitleTextColor());
                        movieCategory.setTextColor(backgroundAndContentColors.getTitleTextColor());
                        year.setTextColor(backgroundAndContentColors.getTitleTextColor());
                        ratingText.setTextColor(backgroundAndContentColors.getTitleTextColor());
                        deleteBookmarked.setColorFilter(backgroundAndContentColors.getTitleTextColor(),
                                PorterDuff.Mode.MULTIPLY);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
