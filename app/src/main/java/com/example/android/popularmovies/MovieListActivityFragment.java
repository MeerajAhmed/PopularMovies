package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
public class MovieListActivityFragment extends Fragment {

    private MovieAdapter movieAdapter;

    private ArrayList<Movie> movieList = new ArrayList<Movie>();

    public MovieListActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);

        movieAdapter = new MovieAdapter(getActivity(), movieList);

        // Get a reference to the ListView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.grid_view_movies);
        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = movieAdapter.getItem(position);
                //Toast.makeText(getActivity(), "Movie detail intent", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
                intent.putExtra(Movie.INTENT_EXTRA, movie);
                startActivity(intent);

               /* Bundle movieBundle = new Bundle();
                movieBundle.putParcelable(Movie.INTENT_EXTRA, movie);
                intent.putExtras(movieBundle);*/

            }

        });

        return rootView;
    }

    private void updateMovieList() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortBy = sharedPrefs.getString(
                    getString(R.string.pref_sort_key),
                    getString(R.string.pref_sort_popular));
            FetchMovieTask movieTask = new FetchMovieTask();
            movieTask.execute(sortBy);
        } else {
            Toast.makeText(getContext(), "Please check if you are connected to Internet", Toast.LENGTH_LONG).show();
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
        outState.putParcelableArrayList("movies", movieList);
        super.onSaveInstanceState(outState);
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
                if (getString(R.string.pref_sort_rating) == params[0]) {
                    TMDB_API = TMDB_BASE_URL + TMDB_RATING_API;
                } else {
                    TMDB_API = TMDB_BASE_URL + TMDB_POPULAR_API;
                }

                Uri builtUri = Uri.parse(TMDB_API).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

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
            if (movies != null) {
                movieAdapter.clear();

                for (Movie movie : movies) {
                    movieAdapter.add(movie);
                }

            }
        }
    }

}
