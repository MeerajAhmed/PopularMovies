package com.example.android.popularmovies.adapter;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.common.Constants;
import com.example.android.popularmovies.model.MovieVideo;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by mahme4 on 10/5/2016.
 */
public class MovieVideoAdapter extends ArrayAdapter<MovieVideo> {

    public MovieVideoAdapter(Context context, List<MovieVideo> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieVideo movieVideo = getItem(position);

        if( convertView == null ){
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.movie_video_list_item, parent, false);
        }

        if(movieVideo != null){
            ImageView imageViewThumbNail = (ImageView) convertView.findViewById(R.id.movie_video_img);
            Picasso.with(getContext())
                    .load(String.format(Constants.YOUTUBE_THUMBNAIL_URL, movieVideo.getKey()))
                    .into(imageViewThumbNail);
            TextView textViewMovieName = (TextView) convertView.findViewById(R.id.movie_video_name);
            textViewMovieName.setText(movieVideo.getName());
        }
        return convertView;

    }
}
