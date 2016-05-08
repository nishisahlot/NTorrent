package com.nikki.torrents.fragments;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.transition.Slide;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.nikki.torrents.R;
import com.nikki.torrents.activities.BaseActivitySecond;
import com.nikki.torrents.customViews.ObservableScrollView;
import com.nikki.torrents.customViews.ScrollViewListener;
import com.nikki.torrents.databaseModels.MovieDescription;
import com.nikki.torrents.databaseModels.MovieModel;
import com.nikki.torrents.enums.MovieTv;
import com.nikki.torrents.models.Review;
import com.nikki.torrents.okhttp.OkHttpAsyncStringRequest;
import com.nikki.torrents.urls.Apis;
import com.nikki.torrents.utils.Constant;
import com.nikki.torrents.utils.GUIUtils;
import com.nikki.torrents.utils.TransitionUtils;
import com.nikki.torrents.youtube.PlayerViewDemoActivity;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;

/**
 * Created by Nishi Sahlot on 4/5/2016.
 */
public class MovieDetailFragment extends Fragment implements ScrollViewListener {

    public MovieDetailFragment() {
    }

    public static Fragment newInstance(Bundle bundle) {
        MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
        if (bundle != null)
            movieDetailFragment.setArguments(bundle);

        return movieDetailFragment;
    }

    Context context;
    boolean mIsTablet;

    private static final int VIDEO_BUTTON = 0;
    private static final int CONFIRMATION = 4;
    private static final int HOMEPAGE = 1;
    private static final int COMPANY = 2;
    private static final int DESCRIPTION_HEADER = 1;
    private static final int REVIEWS_HEADER = 2;
    private static final int DESCRIPTION = 0;
    private static final int TAGLINE_HEADER = 0;
    private static final int TAGLINE = 3;
    private int mReviewsColor = -1;
    private int mReviewsAuthorColor = -1;
    MovieDescription movieDescription;
    @InjectView(R.id.activity_detail_scroll)
    ObservableScrollView mObservableScrollView;
    @InjectView(R.id.item_movie_cover)
    ImageView mCoverImageView;
    @InjectView(R.id.activity_detail_fab)
    FloatingActionButton mFabButton;
    @InjectView(R.id.activity_detail_title)
    TextView mTitle;
    @InjectView(R.id.buttonTextContainer)
    View buttonTextContainer;
    @InjectView(R.id.activity_detail_book_info)
    LinearLayout mMovieDescriptionContainer;
    @InjectView(R.id.activity_detail_conf_container)
    FrameLayout mConfirmationContainer;
    @InjectView(R.id.activity_detail_container)
    View mInformationContainer;
    @InjectView(R.id.activity_detail_conf_image)
    ImageView mConfirmationView;
    @InjectViews({
            R.id.activity_detail_content,
            R.id.activity_detail_homepage,
            R.id.activity_detail_company,
            R.id.activity_detail_tagline,
            R.id.activity_detail_confirmation_text,
    })
    List<TextView> mMovieInfoTextViews;
    @InjectViews({
            R.id.activity_detail_header_tagline,
            R.id.activity_detail_header_description,
            R.id.activity_detail_header_reviews
    })
    List<TextView> movieHeaders;
    @InjectViews({
            R.id.video,
            R.id.share,
            R.id.torrentz
    })
    List<Button> buttons;

    TextView reviewTextView;

    private Palette.Swatch mBrightSwatch;
    MovieModel movieModel;
    GetPosterImage getPosterImage;
    AppCompatActivity appCompatActivity;


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

        View fragmentView = inflater.inflate(R.layout.movie_detail_fragment, container, false);
        ButterKnife.inject(this, fragmentView);
        mIsTablet = context.getResources().getBoolean(
                R.bool.is_tablet);


        initializeStartAnimation();
        setImageAndPalette();

        setData();
        return fragmentView;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getPosterImage != null)
            getPosterImage.cancel(false);
    }

    private void setData() {
        Bundle bundle = getArguments();
        if (bundle != null)
            movieModel = (MovieModel) bundle.getSerializable("movieModel");
        if (movieModel != null) {
            mTitle.setText(movieModel.movieName);
//            if(movieModel.notMajorData){
//                animateElementsByScale();
//            }
            if (movieModel.movieTv == MovieTv.BOOKMARKS) {
                movieDescription = MovieDescription.getDescriptionFromParent(movieModel);
            }

            if (movieDescription == null) {
                String url = Apis.BASE_URL_TMDB + movieModel.MovieId + "-" + movieModel.movieName.replaceAll("[^\\w]+", "-").toLowerCase();
                new OkHttpAsyncStringRequest(url, new OkHttpAsyncStringRequest.SetCallbacks<String>() {
                    @Override
                    public void onError() {

                    }

                    @Override
                    public void onSuccess(String response) {
                        try {
                            movieDescription = MovieDescription.getMovieDescription(response, movieModel.movieTv);
                            setMovieDescription();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, Constant.getMovieHeaders(), null);
            } else {
                setMovieDescription();
            }


        }

    }


    private void setMovieDescription() {
        try {
            animateElementsByScale();
            if (movieDescription != null) {
                if (!TextUtils.isEmpty(movieDescription.Overview)) {
                    movieHeaders.get(DESCRIPTION_HEADER).setVisibility(View.VISIBLE);
                    mMovieInfoTextViews.get(DESCRIPTION).setVisibility(View.VISIBLE);
                    mMovieInfoTextViews.get(DESCRIPTION).setText(movieDescription.Overview);
                }
                if (movieDescription.review != null) {
                    reviewTextView = new TextView(context);
                    reviewTextView.setTextAppearance(context, R.style
                            .MaterialMoviesReviewTextView);
                    movieHeaders.get(REVIEWS_HEADER).setVisibility(View.VISIBLE);
                    if (mReviewsColor != -1)
                        reviewTextView.setTextColor(mReviewsColor);


                    Review result = movieDescription.review;
                    // Configure the review text
                    String reviewCredit = /*getString(R.string.review_written_by)+" " + */result.author;

                    String reviewText = String.format("%s - %s",
                            reviewCredit, result.content);

                    Spannable spanColorString = new SpannableString(reviewText);
                    spanColorString.setSpan(new ForegroundColorSpan(mReviewsAuthorColor),
                            0, reviewCredit.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    reviewTextView.setText(spanColorString);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    final int reviewMarginTop = getResources().getDimensionPixelOffset(
                            R.dimen.view_margin_medium);
                    layoutParams.setMargins(0, reviewMarginTop, 0, 0);
                    mMovieDescriptionContainer.addView(reviewTextView, layoutParams);
                }

                if (!TextUtils.isEmpty(movieDescription.Tagline)) {
                    movieHeaders.get(TAGLINE_HEADER).setVisibility(View.VISIBLE);
                    mMovieInfoTextViews.get(TAGLINE).setVisibility(View.VISIBLE);
                    mMovieInfoTextViews.get(TAGLINE).setText(movieDescription.Tagline);
                }
                int size;
                if ((size = movieDescription.videos.size()) != 0) {
                    buttons.get(VIDEO_BUTTON).setText(String.format("%d %s", size, getString(R.string.videos)));
                } else {
                    buttons.get(VIDEO_BUTTON).setVisibility(View.GONE);
                }
                if (movieModel.notMajorData && !TextUtils.isEmpty(movieDescription.posterPath)) {
                    new GetPosterImage(movieDescription.posterPath).
                            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class GetPosterImage extends AsyncTask<Void, Void, Bitmap> {
        String imagePath;

        GetPosterImage(String imagePath) {
            this.imagePath = imagePath;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                return BitmapFactory.decodeStream(new URL(imagePath).openStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            try {
                if (!isCancelled() && bitmap != null) {
                    MoviesFragment.sPhotoCache.put(0, bitmap);
                    setImageAndPalette();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void bookMarkEntity() {
        if (movieModel != null) {
            if(movieModel.poster.size()==0&&movieDescription!=null&&!TextUtils.isEmpty(movieDescription.posterPath))
                movieModel.poster.add(movieDescription.posterPath);

            movieModel.serializeImages();
            movieModel.save();
            if (movieDescription != null) {
                if (movieModel.getId() != null && movieModel.getId() != -1)
                    movieDescription.movieModel = movieModel;
                movieDescription.serialize();
                if(movieDescription.movieModel!=null)
                    movieDescription.save();
            }

            //Toast.makeText(context, getString(R.string.bookmarked), Toast.LENGTH_SHORT).show();
        }
    }


    @OnClick({R.id.activity_detail_fab, R.id.video, R.id.torrentz, R.id.share})
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.activity_detail_fab) {
            bookMarkEntity();
            showConfirmationView();
        } else if (id == R.id.video) {
            int size;
            if (movieDescription != null && (size = movieDescription.videos.size()) != 0) {
                List<String> strings = new ArrayList<>();

                for (int i = 0; i < size; i++) {
                    strings.add(getString(R.string.trailer) + " " + (i + 1) + " (>> Click Here >>)");
                }
                new AlertDialog.Builder(context).setTitle(R.string.select).setAdapter(new ArrayAdapter<>(context,
                        android.R.layout.simple_dropdown_item_1line,
                        strings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String video = movieDescription.videos.get(which);
                            Pattern pattern = Pattern.compile("v=.*");
                            Matcher matcher = pattern.matcher(video);
                            if (matcher.find()) {
                                video = matcher.group().replaceAll("v=", "");
                            }
                            Intent intent = new Intent(context, PlayerViewDemoActivity.class);
                            intent.putExtra("video", video);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, null).create().show();
            }

        } else if (id == R.id.torrentz) {
            Intent intent = new Intent(context, BaseActivitySecond.class);
            intent.putExtra("movieModel", movieModel);
            startActivity(intent);

        } else if (id == R.id.share) {
            try {
                if (movieDescription != null) {
                    String videoLink = "";
                    if (movieDescription.videos.size() > 0) {
                        videoLink += "Check out videos\n\n";
                        for (String video : movieDescription.videos) {
                            videoLink += video + "\n";
                        }
                    }
                    videoLink += (!TextUtils.isEmpty(videoLink) ? "\n\n" : "") +
                            getString(R.string.download_app) + "\n\n" + Constant.getDownloadLink(context);

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, movieModel.movieName);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, videoLink);
                    context.startActivity(Intent.createChooser(shareIntent, getString(R.string.favourite_network)));
                } else {
                    Toast.makeText(context, getString(R.string.no_share),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    public void showConfirmationView() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            GUIUtils.showViewByRevealEffect(mConfirmationContainer,
                    mFabButton, GUIUtils.getWindowWidth(appCompatActivity));

        else
            GUIUtils.startScaleAnimationFromPivot(
                    (int) mFabButton.getX(), (int) mFabButton.getY(),
                    mConfirmationContainer, null);

        animateConfirmationView();
        startClosingConfirmationView();
    }


    public void startClosingConfirmationView() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            appCompatActivity.getWindow().setReturnTransition(new Slide());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                mObservableScrollView.setVisibility(View.GONE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    appCompatActivity.finishAfterTransition();

                else {


                    GUIUtils.hideViewByScaleY(mConfirmationContainer, new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            appCompatActivity.finish();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });

                }

            }

        }, 1500);
    }

    public void animateConfirmationView() {

        Drawable drawable = mConfirmationView.getDrawable();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (drawable instanceof Animatable)
                ((Animatable) drawable).start();

        } else {

            mConfirmationView.startAnimation(AnimationUtils.loadAnimation(appCompatActivity,
                    R.anim.appear_rotate));
        }
    }


    private void setImageAndPalette() {
        try {
            if (MoviesFragment.sPhotoCache.size() > 0 && !MoviesFragment.sPhotoCache.get(0).isRecycled()) {
                Bitmap bitmap = MoviesFragment.sPhotoCache.get(0);

                mCoverImageView.setImageBitmap(MoviesFragment.sPhotoCache.get(0));
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        try {
                            if (palette != null) {

                                final Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
                                final Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();
                                final Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
                                final Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
                                final Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();

                                final Palette.Swatch backgroundAndContentColors = (darkVibrantSwatch != null)
                                        ? darkVibrantSwatch : darkMutedSwatch;

                                final Palette.Swatch titleAndFabColors = (darkVibrantSwatch != null)
                                        ? lightVibrantSwatch : lightMutedSwatch;

                                setBackgroundAndFabContentColors(backgroundAndContentColors);

                                setHeadersTitlColors(titleAndFabColors);

                                setVibrantElements(vibrantSwatch);
                            }
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

    private void setVibrantElements(Palette.Swatch vibrantSwatch) {

        mFabButton.getBackground().setColorFilter(vibrantSwatch.getRgb(),
                PorterDuff.Mode.MULTIPLY);


    }

    public void setHeadersTitlColors(Palette.Swatch swatch) {

        if (swatch != null) {

            mBrightSwatch = swatch;
            mTitle.setBackgroundColor(
                    mBrightSwatch.getRgb());
            buttonTextContainer.setBackgroundColor(
                    mBrightSwatch.getRgb());

            mReviewsAuthorColor = swatch.getRgb();

            mMovieInfoTextViews.get(CONFIRMATION).setTextColor(
                    swatch.getRgb());

//            GUIUtils.tintAndSetCompoundDrawable(appCompatActivity, R.mipmap.ic_domain_white_24dp,
//                    swatch.getRgb(), mMovieInfoTextViews.get(HOMEPAGE));
//
//            GUIUtils.tintAndSetCompoundDrawable(appCompatActivity, R.mipmap.ic_public_white_24dp,
//                    swatch.getRgb(), mMovieInfoTextViews.get(COMPANY));

            ButterKnife.apply(movieHeaders, GUIUtils.setter,
                    swatch.getRgb());
//            ButterKnife.apply(buttons,GUIUtils.setter,
//                    swatch.getRgb());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                Drawable drawable = mConfirmationView.getDrawable();
                drawable.setColorFilter(swatch.getRgb(),
                        PorterDuff.Mode.MULTIPLY);

            } else {

                mConfirmationView.setColorFilter(swatch.getRgb(),
                        PorterDuff.Mode.MULTIPLY);
            }
        }
    }

    public void setBackgroundAndFabContentColors(Palette.Swatch swatch) throws Exception {

        if (swatch != null) {

            mReviewsColor = swatch.getTitleTextColor();
            ButterKnife.apply(buttons, GUIUtils.setter,
                    mReviewsColor);
            if(reviewTextView!=null&&mReviewsColor!=-1)
                reviewTextView.setTextColor(mReviewsColor);
            mInformationContainer.setBackgroundColor(swatch.getRgb());
            mConfirmationContainer.setBackgroundColor(swatch.getRgb());
            ButterKnife.apply(buttons, new ButterKnife.Setter<Button, Integer>() {
                        @Override
                        public void set(Button view, Integer value, int index) {
                            view.getBackground().setColorFilter(value,
                                    PorterDuff.Mode.MULTIPLY);
                        }
                    },
                    swatch.getRgb());
            mTitle.setTextColor(swatch.getRgb());

            ButterKnife.apply(mMovieInfoTextViews, GUIUtils.setter,
                    swatch.getTitleTextColor());
        }
    }

    private void initializeStartAnimation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (!mIsTablet) {

                GUIUtils.makeTheStatusbarTranslucent(appCompatActivity);
                mObservableScrollView.setScrollViewListener(this);
            }

            configureEnterTransition();

        }   /*else {
            animateElementsByScale();
        }*/
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void configureEnterTransition() {

        appCompatActivity.getWindow().setSharedElementEnterTransition(
                TransitionUtils.makeSharedElementEnterTransition(appCompatActivity));

        appCompatActivity.postponeEnterTransition();
        int moviePosition = 0;
        if (getArguments() != null)
            moviePosition = getArguments().getInt(
                    MoviesFragment.EXTRA_MOVIE_POSITION, 0);

        mCoverImageView.setTransitionName(MoviesFragment.SHARED_ELEMENT_COVER + moviePosition);
        mObservableScrollView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {

                        mObservableScrollView.getViewTreeObserver()
                                .removeOnPreDrawListener(this);
                        if (appCompatActivity != null)
                            appCompatActivity.startPostponedEnterTransition();
                        return true;
                    }
                }
        );

//        appCompatActivity.getWindow().getSharedElementEnterTransition().addListener(
//                new Transition.TransitionListener() {
//                    @Override
//                    public void onTransitionStart(Transition transition) {
//
//                    }
//
//                    @Override
//                    public void onTransitionEnd(Transition transition) {
//                        animateElementsByScale();
//                    }
//
//                    @Override
//                    public void onTransitionCancel(Transition transition) {
//
//                    }
//
//                    @Override
//                    public void onTransitionPause(Transition transition) {
//
//                    }
//
//                    @Override
//                    public void onTransitionResume(Transition transition) {
//
//                    }
//                }
//        );
    }

    private void animateElementsByScale() {

        GUIUtils.showViewByScale(mFabButton);
        GUIUtils.showViewByScaleY(buttonTextContainer, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                GUIUtils.showViewByScale(mMovieDescriptionContainer);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    boolean isTranslucent = false;

    @Override
    public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {

        try {
            if (y > mCoverImageView.getHeight()) {

                buttonTextContainer.setTranslationY(
                        y - mCoverImageView.getHeight());

                if (!isTranslucent) {

                    isTranslucent = true;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                        GUIUtils.setTheStatusbarNotTranslucent(appCompatActivity);
                        appCompatActivity.getWindow().setStatusBarColor(mBrightSwatch.getRgb());
                    }
                }
            }

            if (y < mCoverImageView.getHeight() && isTranslucent) {

                buttonTextContainer.setTranslationY(0);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    GUIUtils.makeTheStatusbarTranslucent(appCompatActivity);
                    isTranslucent = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
