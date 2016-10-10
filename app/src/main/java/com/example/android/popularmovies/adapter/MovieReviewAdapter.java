package com.example.android.popularmovies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.model.MovieReview;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by mahme4 on 10/3/2016.
 */
public class MovieReviewAdapter extends ArrayAdapter<MovieReview> {

    public MovieReviewAdapter(Context context, List<MovieReview> movieReviewList) {
        super(context, 0, movieReviewList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MovieReview movieReview = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.movie_review_list_item, parent, false);
        }

        TextView textViewAuthor = (TextView) convertView.findViewById(R.id.movie_review_author);
        TextView textViewContent = (TextView) convertView.findViewById(R.id.movie_review_content);

        textViewAuthor.setText(movieReview.getAuthor());
        textViewContent.setText(movieReview.getContent());

        return convertView;
    }
}
