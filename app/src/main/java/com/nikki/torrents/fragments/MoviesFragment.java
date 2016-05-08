package com.nikki.torrents.fragments;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;
import com.nikki.torrents.R;
import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.activities.MovieDetailActivity;
import com.nikki.torrents.adapter.GenreAdapter;
import com.nikki.torrents.adapter.MovieAdapter;
import com.nikki.torrents.databaseModels.MovieModel;
import com.nikki.torrents.enums.MovieTv;
import com.nikki.torrents.enums.SearchEnumNormal;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;
import com.nikki.torrents.models.GenreModel;
import com.nikki.torrents.models.MovieModelWrapper;
import com.nikki.torrents.models.TmdbMovieModel;
import com.nikki.torrents.models.YearModel;
import com.nikki.torrents.okhttp.OkHttpAsyncStringRequest;
import com.nikki.torrents.urls.Apis;
import com.nikki.torrents.utils.CheckConnection;
import com.nikki.torrents.utils.Constant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/13/2016.
 */
public class MoviesFragment extends FragmentWithBackButton {
    public MoviesFragment() {
    }

    public static Fragment newInstance(Bundle bundle) {
        MoviesFragment moviesFragment = new MoviesFragment();
        if (bundle != null)
            moviesFragment.setArguments(bundle);

        return moviesFragment;
    }

    Spinner spinner;
    MovieTv movieTv = MovieTv.MOVIE;
    SearchEnumNormal searchEnumNormal;
    public final static String EXTRA_MOVIE_POSITION = "movie_position";
    public static SparseArray<Bitmap> sPhotoCache = new SparseArray<>(1);
    public final static String EXTRA_MOVIE_LOCATION = "view_location";
    public final static String SHARED_ELEMENT_COVER = "cover";
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.errorText)
    TextView errorText;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.searchView)
    SearchView searchView;
    SearchAdapter searchAdapter;
    List<SearchItem> typeAheadData = new ArrayList<>();
    List<MovieModel> movieModels;
    MovieAdapter movieAdapter;
    AppCompatActivity appCompatActivity;
    Context context;
    int page = 1;
    boolean noMoreDataAvailable;
    boolean executingRequest;
    List<YearModel> yearModels;
    MovieModelWrapper movieModelWrapper;
    final Set<String> selectedGenres = new HashSet<>();
    boolean queryNotSubmit;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        appCompatActivity = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.movies_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        if (movieAdapter != null)
            recyclerView.setAdapter(movieAdapter);
        Bundle bundle = getArguments();
        if (bundle != null) {
            movieTv = (MovieTv) bundle.getSerializable("movieTv");
            searchEnumNormal = (SearchEnumNormal) bundle.getSerializable("searchEnumNormal");
        }


        if (searchEnumNormal == null) {
            setTitle((movieTv == MovieTv.MOVIE) ? getString(R.string.movies) :
                    ((movieTv == MovieTv.BOOKMARKS) ? getString(R.string.bookmars) :
                    getString(R.string.tv_shows)));
        } else {
            toolbar.setVisibility(View.GONE);
            if (getParentFragment() instanceof MovieFragmentBase) {
                MovieFragmentBase movieFragmentBase = (MovieFragmentBase) getParentFragment();
                searchView = movieFragmentBase.searchView;
            }
        }
        setSearchView();
        getData(false);
        setAdapter();
        appCompatActivity.invalidateOptionsMenu();
    }


    private void setSearchView() {
        int theme = SearchView.THEME_LIGHT;

        searchView.setStyle(SearchView.STYLE_CLASSIC);
        searchView.setTheme(theme);
        searchView.setDivider(false);
        searchView.setHint(R.string.search_movie);
        searchView.setDivider(false);
        searchAdapter = new SearchAdapter(context, new ArrayList<SearchItem>(), typeAheadData, theme);
        searchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                try {

                    TmdbMovieModel tmdbMovieModel = searchAdapter.typeAheadData.get(position).tmdbMovieModel;
                    if (tmdbMovieModel != null && !TextUtils.isEmpty(tmdbMovieModel.media_type) &&
                            (tmdbMovieModel.media_type.contains("movie") ||
                                    tmdbMovieModel.media_type.contains("tv"))) {
                        Intent movieDetailActivityIntent = new Intent(
                                appCompatActivity, MovieDetailActivity.class);
                        MovieModel movieModel = new MovieModel();
                        movieModel.movieName = tmdbMovieModel.name;

                        movieModel.movieTv = tmdbMovieModel.media_type.equals("movie") ? MovieTv.MOVIE : MovieTv.TV;
                        if (movieModel.movieTv == MovieTv.TV)
                            movieModel.MovieId = "/tv/" + tmdbMovieModel.id;
                        else
                            movieModel.MovieId = "/movie/" + tmdbMovieModel.id;
                        movieModel.notMajorData = true;
                        sPhotoCache.put(0, BitmapFactory.decodeResource(getResources(), R.mipmap.default_image));
                        movieDetailActivityIntent.putExtra("movieModel", movieModel);
                        startActivity(movieDetailActivityIntent);
                    }
                    closeSearch();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        searchView.setAdapter(searchAdapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                closeSearch();
                Bundle bundle = new Bundle();
                bundle.putString("query", query);
                MainActivity.bringFragment(appCompatActivity, MovieFragmentBase.newInstance(bundle), true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!TextUtils.isEmpty(newText)) {
                    searchMovies(newText);

                }

                return false;
            }
        });
        searchView.setOnSearchViewListener(new SearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                if (searchEnumNormal != null)
                    searchView.setQuery(searchEnumNormal.query, false);
            }

            @Override
            public void onSearchViewClosed() {
                typeAheadData.clear();
            }
        });
    }

    private void searchMovies(String query) {
        String url = Apis.searchMovies + query;
        if (CheckConnection.checkConnection(context)) {
            if (!queryNotSubmit) {


                new OkHttpAsyncStringRequest(url, new OkHttpAsyncStringRequest.SetCallbacks<String>() {
                    @Override
                    public void onError() {
                        queryNotSubmit = false;
                    }

                    @Override
                    public void onSuccess(String response) {
                        queryNotSubmit = false;
                        if (response != null) {
                            List<TmdbMovieModel> tmdbMovieModels = new Gson().fromJson(response, new
                                    TypeToken<List<TmdbMovieModel>>() {
                                    }.getType());

                            if (tmdbMovieModels != null && tmdbMovieModels.size() > 0) {
                                typeAheadData.clear();
                                for (TmdbMovieModel tmdbMovieModel : tmdbMovieModels) {
                                    try {
                                        SearchItem searchItem = new SearchItem(R.drawable.search_ic_search_black_24dp,
                                                tmdbMovieModel.name + " " + tmdbMovieModel.getReleaseDate() + " " +
                                                        tmdbMovieModel.media_type);
                                        searchItem.tmdbMovieModel = tmdbMovieModel;
                                        typeAheadData.add(searchItem);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                searchView.startFilter(null);
                            }

                        }
                    }
                }, Constant.getMovieHeaders(), null);
                queryNotSubmit = true;
            }


        } else {
            Toast.makeText(context, getString(R.string.internet_message), Toast.LENGTH_SHORT).show();
        }
    }


    private void closeSearch() {
        searchView.closeSearch(true);

    }

    private void showSearchView() {

        searchView.showSearch(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.movie_menu, menu);
        spinner = (Spinner) MenuItemCompat.getActionView(menu.
                findItem(R.id.yearSpinner));
        setYearSpinner();

        if (searchEnumNormal != null || movieTv == MovieTv.BOOKMARKS) {
            menu.findItem(R.id.genres).setVisible(false);
            menu.findItem(R.id.yearSpinner).setVisible(false);
        }
    }


    private void setTitle(String title) {
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.getSupportActionBar().setTitle(title);
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getData(boolean force) {
        try {


            errorText.setVisibility(View.GONE);

            if (movieTv == MovieTv.BOOKMARKS) {
                List<MovieModel> movieModels = MovieModel.getAll();
                if (movieModels != null && movieModels.size() > 0) {
                    noMoreDataAvailable=true;
                    if (this.movieModels != null)
                        this.movieModels.clear();
                    setMovieModels(movieModels);
                    setAdapter();
                }else{
                    showErrorText(getString(R.string.not_bookMarked));
                }

            } else {
                if (CheckConnection.checkConnection(context)) {
                    if (!executingRequest) {
                        if (movieModels == null || force) {
                            String url = null;
                            if (searchEnumNormal == null) {
                                url = (movieTv == MovieTv.MOVIE) ? Apis.movieListApi : Apis.tvListApi;
                            } else {
                                String query = "?query=" + searchEnumNormal.query;
                                if (searchEnumNormal == SearchEnumNormal.SEARCH_MOVIE) {
                                    url = Apis.movieSearch + query;
                                } else if (searchEnumNormal == SearchEnumNormal.SEARCH_TV) {
                                    url = Apis.tvSearch + query;
                                }
                            }
                            if (searchEnumNormal == null) {
                                if (movieModelWrapper != null && !TextUtils.isEmpty(movieModelWrapper.selectedGenreKey) &&
                                        selectedGenres.size() > 0) {
                                    // url+="/remote";
                                    int index = 0;
                                    for (String genreId : selectedGenres) {
                                        if (index == 0) {
                                            url += "?";
                                        } else {
                                            url += "&";
                                        }
                                        url += movieModelWrapper.selectedGenreKey + "=" + genreId;
                                        index++;
                                    }
                                }

                            }

                            if (page != 1) {
                                if (url.contains("?")) {
                                    url += "&";
                                } else {
                                    url += "?";
                                }
                                url += "page=" + page;
                                try {
                                    Snackbar.make(getView(), getString(R.string.loading_data), Snackbar.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                if (searchEnumNormal == null) {
                                    if (movieModelWrapper != null) {
                                        if (url.contains("?"))
                                            url += "&";
                                        else
                                            url += "?";

                                        url += movieModelWrapper.selectedYearKey + "=" + movieModelWrapper.yearModels.
                                                get(movieModelWrapper.selecteYear)
                                                .yearValue;


                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            new OkHttpAsyncStringRequest(url, new OkHttpAsyncStringRequest.SetCallbacks<String>() {
                                @Override
                                public void onError() {
                                    try {
                                        executingRequest = false;
                                        invisibleProgressBar();
                                        noMoreDataAvailable = true;
                                        showErrorText(getString(R.string.technical_difficulty));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }


                                @Override
                                public void onSuccess(String response) {

                                    try {
                                        page++;
                                        executingRequest = false;
                                        invisibleProgressBar();
                                        if (response != null) {
                                            MovieModelWrapper movieModelWrapper = MovieModelWrapper.parseMovieResults(response);
                                            if (movieModelWrapper != null)
                                                MoviesFragment.this.movieModelWrapper = movieModelWrapper;
                                            List<MovieModel> localMovieModels = null;

                                            if (movieModelWrapper != null) {
                                                localMovieModels = movieModelWrapper.movieModels;
                                                setYearSpinner();
                                            }


                                            if (localMovieModels == null || localMovieModels.size() == 0) {
                                                noMoreDataAvailable = true;
                                                showErrorText(getString(R.string.no_more_data));
                                            } else {
                                                setMovieModels(localMovieModels);
                                            }

                                            setAdapter();

                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                }
                            }, Constant.getMovieHeaders(), null);
                            executingRequest = true;
                            visibleProgressBar();
                        }
                    }


                } else {
                    showErrorText(getString(R.string.internet_message));
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void setMovieModels(List<MovieModel> localMovieModels) {
        if (movieModels == null)
            movieModels = localMovieModels;
        else
            movieModels.addAll(localMovieModels);
    }

    private void setYearSpinner() {
        try {
            if (movieModelWrapper != null && movieModelWrapper.yearModels != null) {

                yearModels = movieModelWrapper.yearModels;
                int size = yearModels.size();
                String[] year = new String[size];
                for (int i = 0; i < size; i++) {
                    year[i] = yearModels.get(i).yearText;
                }

                spinner.setAdapter(new ArrayAdapter<>(
                        context,
                        R.layout.spinner_adapter, year));
                spinner.setSelection(movieModelWrapper.selecteYear);
                spinner.setOnItemSelectedListener(onYearSelecteListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearList() {
        if (movieModels != null) {
            movieModels.clear();
            if (movieAdapter != null)
                movieAdapter.notifyDataSetChanged();
            page = 1;
            noMoreDataAvailable = false;
        }

    }

    AdapterView.OnItemSelectedListener onYearSelecteListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (CheckConnection.checkConnection(context)) {
                if (movieModelWrapper != null && movieModelWrapper.selecteYear != i) {
                    movieModelWrapper.selecteYear = i;
                    clearList();
                    selectedGenres.clear();
                    getData(true);
                }
            } else {
                showErrorText(getString(R.string.internet_message));

            }


        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private void showErrorText(String message) {
        try {
            if (movieModels == null || movieModels.size() == 0) {
                errorText.setVisibility(View.VISIBLE);
                errorText.setText(message);
            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAdapter() {
        try {
            if (movieModels != null) {
                if (movieAdapter == null) {
                    movieAdapter = new MovieAdapter(movieModels, new OnItemClickRecyclerListener() {
                        @Override
                        public void onItemClicked(View view, int position) {
                            int id=view.getId();
                            if(id==R.id.deleteBookmarked){
                                if(movieTv==MovieTv.BOOKMARKS){
                                    MovieModel movieModel=movieAdapter.movieModels.get(position);
                                    if(movieModel.getId()!=null){
                                        int size;
                                        movieAdapter.movieModels.remove(position).delete();
                                        movieAdapter.notifyItemRemoved(position);
                                        movieAdapter.notifyItemRangeChanged(position,size=movieAdapter.movieModels.size());
                                        if(size==0){
                                            showErrorText(getString(R.string.not_bookMarked));
                                        }
                                    }
                                }
                            }else{
                                Intent movieDetailActivityIntent = new Intent(
                                        appCompatActivity, MovieDetailActivity.class);
                                movieDetailActivityIntent.putExtra(EXTRA_MOVIE_POSITION, position);

                                MovieModel movieModel = movieAdapter.movieModels.get(position);
                                movieModel.movieTv = MoviesFragment.this.movieTv;

                                movieDetailActivityIntent.putExtra("movieModel", movieModel);
                                View touchedView = view.findViewById(R.id.poster);
                                ImageView mCoverImage = (ImageView) touchedView;
                                BitmapDrawable bitmapDrawable = (BitmapDrawable) mCoverImage.getDrawable();
                                sPhotoCache.put(0, bitmapDrawable.getBitmap());

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    startDetailActivityBySharedElements(touchedView, position,
                                            movieDetailActivityIntent);
                                else
                                    startActivity(movieDetailActivityIntent);
                            }

                        }
                    }, new MovieAdapter.LoadPaging() {
                        @Override
                        public void loadMoreData() {
                            if (!noMoreDataAvailable)
                                getData(true);
                        }
                    });
                    recyclerView.setAdapter(movieAdapter);
                } else {
                    movieAdapter.notifyDataSetChanged();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startDetailActivityBySharedElements(View touchedView,
                                                     int moviePosition, Intent movieDetailActivityIntent) {

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                appCompatActivity, new Pair<>(touchedView, SHARED_ELEMENT_COVER + moviePosition));

        appCompatActivity.startActivity(movieDetailActivityIntent, options.toBundle());
    }

    private void startDetailActivityByAnimation(View touchedView,
                                                int touchedX, int touchedY, Intent movieDetailActivityIntent) {

        int[] touchedLocation = {touchedX, touchedY};
        int[] locationAtScreen = new int[2];
        touchedView.getLocationOnScreen(locationAtScreen);

        int finalLocationX = locationAtScreen[0] + touchedLocation[0];
        int finalLocationY = locationAtScreen[1] + touchedLocation[1];

        int[] finalLocation = {finalLocationX, finalLocationY};
        movieDetailActivityIntent.putExtra(EXTRA_MOVIE_LOCATION,
                finalLocation);

        startActivity(movieDetailActivityIntent);
    }

    private void visibleProgressBar() {
        try {
            if (movieModels == null || movieModels.size() == 0)
                progressBar.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void invisibleProgressBar() {
        try {
            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            MainActivity.goBack(appCompatActivity);
            return true;
        } else if (id == R.id.action_search) {
            showSearchView();
            return true;
        } else if (id == R.id.genres) {
            if (movieModelWrapper != null && movieModelWrapper.genreModels != null) {
                new ShowGenresDialog();
            } else {
                Toast.makeText(context, getString(R.string.no_data_available), Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    class ShowGenresDialog {
        @InjectView(R.id.recyclerView)
        RecyclerView recyclerView;
        View view;
        GenreAdapter genreAdapter;

        ShowGenresDialog() {
            view = LayoutInflater.from(context).inflate(R.layout.genre_dialog, null);
            ButterKnife.inject(this, view);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            show();
        }

        private void show() {
            recyclerView.setAdapter(genreAdapter = new GenreAdapter(selectedGenres, movieModelWrapper.genreModels,
                    new OnItemClickRecyclerListener() {
                        @Override
                        public void onItemClicked(View view, int position) {
                            GenreModel genreModel = genreAdapter.genreModels.get(position);
                            if (selectedGenres.contains(genreModel.genreId)) {
                                selectedGenres.remove(genreModel.genreId);
                            } else {
                                selectedGenres.add(genreModel.genreId);
                            }
                            genreAdapter.notifyItemChanged(position);
                        }
                    }));
            new AlertDialog.Builder(context).setTitle(getString(R.string.genres)).setView(view).
                    setPositiveButton(android.R.string.ok
                            , new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clearList();
                            getData(true);
                        }
                    }).setNegativeButton(android.R.string.cancel, null).
                    create().show();
        }
    }

    @Override
    public boolean onBackButtonPressed() {
        try {
            if (searchView.isSearchOpen()) {
                closeSearch();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
