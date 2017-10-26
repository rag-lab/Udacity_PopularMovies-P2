package com.example.rod.popularmovies_p2.data;

import android.provider.BaseColumns;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by rodrigo on 10/20/2017.
 */

public class MovieContract {


    public static final class MovieListEntry implements BaseColumns {

        // COMPLETED (2) Inside create a static final members for the table name and each of the db columns
        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_TITULO = "titulo";
        public static final String COLUMN_IDMOVIE = "idmovie";
        public static final String COLUMN_ANO = "ano";
        public static final String COLUMN_DURACAO = "duracao";
        public static final String COLUMN_SINOPSE = "sinopse";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_FAVORITOS = "favoritos";
        public static final String COLUMN_IMAGEPATH = "image";


    }



}
