package com.example.android.popularmovies.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by mahme4 on 9/29/2016.
 */
public final class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVORITES = "favorites";

    //Prevent someone from accidentally instantiating the contract class
    private MovieContract(){}

    public static final class MovieEntry implements BaseColumns {

        /** The content URI to access the favorite movie data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FAVORITES);


        /*
        * The MIME type of the {@Link #CONTENT_URI} for a list of movies.
        * */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;

        /**
         * The MIME type of the {@Link #CONTENT_URI} for a movie.
         * */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;

        /** Name of the database table for favorite movies */
        public static final String TABLE_NAME = "favorites";

        /** Unique ID number for the Movie */

        public final static String _ID = BaseColumns._ID;

        /** Name of the movie*/
        public final static String COLUMN_MOVIE_NAME = "name";

        public final static String COLUMN_MOVIE_POSTER = "poster";

        public final static String COLUMN_MOVIE_OVERVIEW = "overview";

        public final static String COLUMN_MOVIE_RATING = "rating";

        public final static String COLUMN_MOVIE_RELEASE_DATE = "release";


    }
}
