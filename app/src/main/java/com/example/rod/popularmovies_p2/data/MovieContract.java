package com.example.rod.popularmovies_p2.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by rodrigo on 10/20/2017.
 */

public class MovieContract {

    // The authority, which is how your code knows which Content Provider to access
    public static final String AUTHORITY = "com.example.rod.popularmovies__p2";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "tasks" directory
    public static final String PATH_MOVIE = "movie";


    public static final class MovieListEntry implements BaseColumns {

        // TaskEntry content URI = base content URI + path
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        // COMPLETED (2) Inside create a static final members for the table name and each of the db columns
        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_TITULO = "titulo";
        public static final String COLUMN_IDMOVIE = "idmovie";
        public static final String COLUMN_ANO = "ano";
        public static final String COLUMN_DURACAO = "duracao";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_SINOPSE = "sinopse";
        public static final String COLUMN_FAVORITOS = "favoritos";
        public static final String COLUMN_IMAGEPATH = "image";


    }



}
