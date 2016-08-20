package com.example.android.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Intent intent = getActivity().getIntent();
        Movie movie = intent.getExtras().getParcelable(Movie.INTENT_EXTRA);

        ImageView moviePoster = (ImageView) rootView.findViewById(R.id.movie_poster);
        Picasso.with(getContext())
                .load(movie.getPosterUrl())
                .into(moviePoster);

        TextView movieRating = (TextView) rootView.findViewById(R.id.movie_rating);
        movieRating.setText(movie.getRating().toString());

        TextView movieTitle = (TextView) rootView.findViewById(R.id.movie_title);
        movieTitle.setText(movie.getTitle());

        TextView movieReleaseDate = (TextView) rootView.findViewById(R.id.movie_release_date);
        movieReleaseDate.setText(movie.getReleaseDate());

        TextView movieOverview = (TextView) rootView.findViewById(R.id.movie_overview);
        movieOverview.setText(movie.getOverview());

        return rootView;
    }
}
