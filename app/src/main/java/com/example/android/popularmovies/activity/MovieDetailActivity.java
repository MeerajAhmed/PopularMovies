package com.example.android.popularmovies.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.fragment.MovieDetailActivityFragment;
import com.example.android.popularmovies.model.Movie;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        if( savedInstanceState == null ){
           Bundle arguments = new Bundle();
           arguments.putParcelable(Movie.INTENT_EXTRA, getIntent().getData());

           MovieDetailActivityFragment fragment = new MovieDetailActivityFragment();
           fragment.setArguments(arguments);

           getSupportFragmentManager().beginTransaction()
                   .add(R.id.movie_detail_container, fragment)
                   .commit();

       }

    }



}
