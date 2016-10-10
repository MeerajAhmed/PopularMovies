package com.example.android.popularmovies.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.BuildConfig;
import com.example.android.popularmovies.adapter.MovieVideoAdapter;
import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapter.MovieReviewAdapter;
import com.example.android.popularmovies.common.Constants;
import com.example.android.popularmovies.data.MovieContract.MovieEntry;
import com.example.android.popularmovies.model.MovieReview;
import com.example.android.popularmovies.model.MovieVideo;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */

public class MovieDetailActivityFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();
    private static final int MOVIE_DETAIL_LOADER = 1;
    private Movie mMovie;
    private CheckBox mCbFavorite;
    private Uri mMovieUri;
    private MovieReviewAdapter mMovieReviewAdapter;
    private ArrayList<MovieReview> mMovieReviewList = new ArrayList<MovieReview>();
    private MovieVideoAdapter mMovieVideoAdapter;
    private ArrayList<MovieVideo> mMovieVideoList = new ArrayList<MovieVideo>();

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        mMovieVideoAdapter = new MovieVideoAdapter(getActivity(), mMovieVideoList);
        ListView videoListView = (ListView) rootView.findViewById(R.id.movie_trailer);
        videoListView.setAdapter(mMovieVideoAdapter);
        final View movieEmptyView = rootView.findViewById(R.id.empty_movie_video);
        videoListView.setEmptyView(movieEmptyView);

        videoListView.setOnItemClickListener(new ListView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieVideo movieVideo = mMovieVideoAdapter.getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.YOUTUBE_VIDEO_URL + movieVideo.getKey()));
                startActivity(intent);
            }
        });


        mMovieReviewAdapter = new MovieReviewAdapter(getActivity(), mMovieReviewList);
        ListView reviewListView = (ListView) rootView.findViewById(R.id.movie_review);
        reviewListView.setAdapter(mMovieReviewAdapter);
        View reviewEmptyView = rootView.findViewById(R.id.empty_movie_review);
        reviewListView.setEmptyView(reviewEmptyView);

        Bundle arguments = getArguments();
        mMovie = arguments.getParcelable(Movie.INTENT_EXTRA);
        if (mMovie == null) {
            Intent intent = getActivity().getIntent();
            mMovie = intent.getExtras().getParcelable(Movie.INTENT_EXTRA);
        }

        ImageView moviePoster = (ImageView) rootView.findViewById(R.id.movie_poster);
        Picasso.with(getContext())
                .load(mMovie.getPosterUrl())
                .into(moviePoster);

        TextView movieRating = (TextView) rootView.findViewById(R.id.movie_rating);
        movieRating.setText(mMovie.getRating().toString());

        TextView movieTitle = (TextView) rootView.findViewById(R.id.movie_title);
        movieTitle.setText(mMovie.getTitle());

        TextView movieReleaseDate = (TextView) rootView.findViewById(R.id.movie_release_date);
        movieReleaseDate.setText(mMovie.getReleaseDate());

        TextView movieOverview = (TextView) rootView.findViewById(R.id.movie_overview);
        movieOverview.setText(mMovie.getOverview());

        mMovieUri = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, mMovie.getId());

        mCbFavorite = (CheckBox) rootView.findViewById(R.id.item_movie_vote_favorite_checkbox);

        mCbFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = ((CheckBox)view).isChecked();
                String displayMsg;

                Log.v(LOG_TAG, "mCbFavorite : OnClickListener : isChecked : " + isChecked);

                if( isChecked ){
                    ContentValues values = new ContentValues();
                    values.put(MovieEntry._ID, mMovie.getId());
                    values.put(MovieEntry.COLUMN_MOVIE_NAME, mMovie.getTitle());
                    values.put(MovieEntry.COLUMN_MOVIE_POSTER, mMovie.getPosterImage());
                    values.put(MovieEntry.COLUMN_MOVIE_OVERVIEW, mMovie.getOverview());
                    values.put(MovieEntry.COLUMN_MOVIE_RATING, mMovie.getRating());
                    values.put(MovieEntry.COLUMN_MOVIE_RELEASE_DATE, mMovie.getReleaseDate());

                    Uri uri = getActivity().getContentResolver().insert(mMovieUri, values);
                    if( uri == null ){
                        displayMsg = getString(R.string.db_insert_failure);
                    } else {
                        displayMsg = getString(R.string.db_insert_success);
                    }

                } else {
                    int rowsDeleted = getActivity().getContentResolver().delete(mMovieUri, null,null);

                    if( rowsDeleted == 0 ){
                        displayMsg = getString(R.string.db_delete_failure);
                    } else {
                        displayMsg = getString(R.string.db_delete_success);
                    }
                }

                Toast.makeText(getContext(), displayMsg, Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieInfo();
    }

    private void updateMovieInfo() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            MovieReviewTask movieReviewTask = new MovieReviewTask();
            movieReviewTask.execute();
            MovieVideoTask movieVideoTask = new MovieVideoTask();
            movieVideoTask.execute();
        } else {
            Toast.makeText(getContext(), getString(R.string.check_network), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if( savedInstanceState != null ){
            mMovieReviewList = savedInstanceState.getParcelableArrayList(Constants.MOVIE_REVIEWS_PARCEL_NAME);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Constants.MOVIE_REVIEWS_PARCEL_NAME, mMovieReviewList);
        super.onSaveInstanceState(outState);
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if( null != mMovieUri ){
            return new CursorLoader(getContext(), mMovieUri, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if( data == null || data.getCount() < 1 ){
            mCbFavorite.setChecked(false);
        } else {
            mCbFavorite.setChecked(true);
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCbFavorite.setChecked(false);
    }

    private class MovieReviewTask extends AsyncTask<String, Void, MovieReview[]> {

        private final String LOG_TAG = MovieReviewTask.class.getSimpleName();

        private MovieReview[] getMovieReviewsFromJson( String movieReviewStr) throws JSONException {
            final String TMDB_RESULTS = "results";
            final String TMDB_AUTHOR = "author";
            final String TMDB_CONTENT = "content";
            JSONObject movieReviewJson = new JSONObject(movieReviewStr);
            JSONArray movieReviewArray = movieReviewJson.getJSONArray(TMDB_RESULTS);
            MovieReview[] movieReviews = new MovieReview[movieReviewArray.length()];

            for(int i=0; i < movieReviewArray.length(); i++ ){
                JSONObject movieResult = movieReviewArray.getJSONObject(i);
                MovieReview movieReview = new MovieReview();
                movieReview.setAuthor( movieResult.getString(TMDB_AUTHOR));
                movieReview.setContent( movieResult.getString(TMDB_CONTENT));
                movieReviews[i] = movieReview;
            }

            return movieReviews;
        }

        @Override
        protected MovieReview[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieReviewJsonStr = null;


            try {
                String TMDB_API = Constants.TMDB_BASE_URL + mMovie.getId() + Constants.TMDB_REVIEWS_API;
                Uri builtUri = Uri.parse(TMDB_API).buildUpon()
                        .appendQueryParameter(Constants.TMDB_API_KEY_PARAM, BuildConfig.TMDB_API_KEY).build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built movie reviews URL " +  builtUri.toString());

                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null){
                    return  null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0){
                    return  null;
                }
                movieReviewJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if( urlConnection != null){
                    urlConnection.disconnect();
                }
                if( reader != null){
                    try {
                        reader.close();
                    } catch ( final IOException e){
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieReviewsFromJson(movieReviewJsonStr);
            } catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(MovieReview[] movieReviews) {
           if( movieReviews != null){
               mMovieReviewAdapter.clear();
               for( MovieReview movieReview : movieReviews){
                   mMovieReviewAdapter.add(movieReview);
               }
           }
        }
    }

    private class MovieVideoTask extends AsyncTask<String, Void, MovieVideo[]>{

        private final String LOG_TAG = MovieVideoTask.class.getSimpleName();

        private MovieVideo[] getMovieVideosFromJson(String movieVideoStr) throws JSONException {
            final String TMDB_RESULTS = "results";
            final String TMDB_ID = "id";
            final String TMDB_KEY = "key";
            final String TMDB_NAME = "name";
            JSONObject movieVideoJson = new JSONObject(movieVideoStr);
            JSONArray movieVideoArray = movieVideoJson.getJSONArray(TMDB_RESULTS);
            MovieVideo[] movieVideos = new MovieVideo[movieVideoArray.length()];

            for ( int i=0; i< movieVideoArray.length(); i++ ){
                JSONObject movieResult = movieVideoArray.getJSONObject(i);
                MovieVideo movieVideo = new MovieVideo();
                movieVideo.setId(movieResult.getString(TMDB_ID));
                movieVideo.setKey(movieResult.getString(TMDB_KEY));
                movieVideo.setName(movieResult.getString(TMDB_NAME));
                movieVideos[i] = movieVideo;
            }
            return movieVideos;
        }

        @Override
        protected MovieVideo[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieVideoJsonStr = null;

            try {
                String TMDB_API = Constants.TMDB_BASE_URL + mMovie.getId() + Constants.TMDB_VIDEOS_API;
                Uri builtUri = Uri.parse(TMDB_API).buildUpon()
                        .appendQueryParameter(Constants.TMDB_API_KEY_PARAM, BuildConfig.TMDB_API_KEY).build();
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built movie video URL " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if( inputStream == null ){
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0){
                    return null;
                }

                movieVideoJsonStr = buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if( urlConnection != null ){
                    urlConnection.disconnect();
                }
                if( reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieVideosFromJson(movieVideoJsonStr);
            } catch ( JSONException e ){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(MovieVideo[] movieVideos) {
            if( movieVideos != null ){
                mMovieVideoAdapter.clear();
                for( MovieVideo movieVideo : movieVideos ){
                    mMovieVideoAdapter.add(movieVideo);
                }
            }
        }
    }
}
