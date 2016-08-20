package com.example.android.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mahme4 on 8/13/2016.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {

    MovieAdapter(Activity context, List<Movie> movieList) {
        super(context, 0, movieList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Movie movie = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.movie_list_item, parent, false);
        }

        ImageView movieImageView = (ImageView) convertView.findViewById(R.id.movie_image);

        Picasso.with(getContext())
                .load(movie.getPosterUrl())
                .into(movieImageView);

        return convertView;
    }
}
