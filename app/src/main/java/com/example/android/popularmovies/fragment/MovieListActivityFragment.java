package com.example.android.popularmovies.fragment;

import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.view.menu.MenuPopupHelper;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.android.popularmovies.BuildConfig;
import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.adapter.MovieAdapter;
import com.example.android.popularmovies.activity.MovieDetailActivity;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieListActivityFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final int MOVIE_LIST_LOADER = 0;
    private static final String SELECTED_KEY = "selected_position";
    private MovieAdapter movieAdapter;
    private int mPosition = GridView.INVALID_POSITION;
    private  GridView mGridView;

    private Movie mMovie;

    private ArrayList<Movie> movieList = new ArrayList<Movie>();

    public MovieListActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);

        movieAdapter = new MovieAdapter(getActivity(), movieList);

        mGridView = (GridView) rootView.findViewById(R.id.grid_view_movies);
        mGridView.setAdapter(movieAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = movieAdapter.getItem(position);
                if( movie !=  null){
                    ((Callback) getActivity())
                            .onItemSelected(movie);
                }
                /*Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
                intent.putExtra(Movie.INTENT_EXTRA, movie);
                startActivity(intent);*/
                mPosition = position;
            }

        });

        if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)){
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    private void updateMovieAdapter(Movie[] movies) {
        if (movies != null) {
            movieAdapter.clear();

            for (Movie movie : movies) {
                movieAdapter.add(movie);
            }
        }
    }

    private void updateMovieList() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = sharedPrefs.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_popular));

        String pref_favorite = getString(R.string.pref_sort_favorite);

        if (pref_favorite.equals(sortBy)) {
            getLoaderManager().initLoader(MOVIE_LIST_LOADER, null, this);
        } else {
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                FetchMovieTask movieTask = new FetchMovieTask();
                movieTask.execute(sortBy);
            } else {
                Toast.makeText(getContext(), R.string.check_network, Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            movieList = savedInstanceState.getParcelableArrayList("movies");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if( mPosition != GridView.INVALID_POSITION ){
            outState.putInt(SELECTED_KEY, mPosition);
        }
        outState.putParcelableArrayList("movies", movieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
            MovieEntry._ID,
            MovieEntry.COLUMN_MOVIE_NAME,
            MovieEntry.COLUMN_MOVIE_POSTER,
            MovieEntry.COLUMN_MOVIE_OVERVIEW,
            MovieEntry.COLUMN_MOVIE_RATING,
            MovieEntry.COLUMN_MOVIE_RELEASE_DATE
        };

        return new CursorLoader(getContext(), MovieEntry.CONTENT_URI ,projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if( cursor == null || cursor.getCount() < 1 ){
            return;
        }

        Movie[] movies = new Movie[cursor.getCount()];
        int i = 0;
        int idColumnIndex = cursor.getColumnIndex(MovieEntry._ID);
        int titleColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_NAME);
        int posterColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_POSTER);
        int overviewColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_OVERVIEW);
        int ratingColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_RATING);
        int dateColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_RELEASE_DATE);

        while (cursor.moveToNext()){
            Movie movie = new Movie();
            movie.setId(cursor.getInt(idColumnIndex));
            movie.setTitle(cursor.getString(titleColumnIndex));
            movie.setPosterImage(cursor.getString(posterColumnIndex));
            movie.setOverview(cursor.getString(overviewColumnIndex));
            movie.setRating(cursor.getDouble(ratingColumnIndex));
            movie.setReleaseDate(cursor.getString(dateColumnIndex));
            movies[i] = movie;
            i++;
        }
        updateMovieAdapter(movies);

        if( mPosition != GridView.INVALID_POSITION){
            mGridView.smoothScrollToPosition(mPosition);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void updateDetailsFragment(Movie[] movies) {
        if( mPosition == GridView.INVALID_POSITION && movies != null && movies.length > 0){
            ((Callback) getActivity())
                    .initDetailFragment(movies[0]);
        }
    }

    public interface Callback {
        void onItemSelected(Movie movie);
        void initDetailFragment(Movie movie);
    }

    public class FetchMovieTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */

        private Movie[] getMovieDetailsFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_ID = "id";
            final String TMDB_TITLE = "title";
            final String TMDB_POSTER_PATH = "poster_path";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_RATING = "vote_average";
            final String TMDB_RELEASE_DATE = "release_date";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_RESULTS);
            Movie[] movies = new Movie[movieArray.length()];

            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movieResult = movieArray.getJSONObject(i);
                Movie movie = new Movie();
                movie.setId(movieResult.getInt(TMDB_ID));
                movie.setTitle(movieResult.getString(TMDB_TITLE));
                movie.setPosterImage(movieResult.getString(TMDB_POSTER_PATH));
                movie.setOverview(movieResult.getString(TMDB_OVERVIEW));
                movie.setRating(movieResult.getDouble(TMDB_RATING));
                movie.setReleaseDate(movieResult.getString(TMDB_RELEASE_DATE));
                movies[i] = movie;
            }

            return movies;
        }

        @Override
        protected Movie[] doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {

                final String TMDB_BASE_URL =
                        "http://api.themoviedb.org/3/movie/";
                final String TMDB_POPULAR_API = "popular?";
                final String TMDB_RATING_API = "top_rated?";

                final String API_KEY_PARAM = "api_key";

                String TMDB_API;
                String pref_rating = getString(R.string.pref_sort_rating);
                if (pref_rating.equals(params[0])) {
                    TMDB_API = TMDB_BASE_URL + TMDB_RATING_API;
                } else {
                    TMDB_API = TMDB_BASE_URL + TMDB_POPULAR_API;
                }

                Uri builtUri = Uri.parse(TMDB_API).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built movie URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDetailsFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
            updateMovieAdapter(movies);
            updateDetailsFragment(movies);
        }
    }

}
