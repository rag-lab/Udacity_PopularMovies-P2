package com.example.rod.popularmovies_p2;

/**
 * Created by rodrigo.augusto on 24/10/2017.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.rod.popularmovies_p2.data.MovieContract.*;

public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movies.db";
    public static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIELIST_TABLE = "CREATE TABLE " + MovieListEntry.TABLE_NAME + " (" +
                MovieListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieListEntry.COLUMN_TITULO + " TEXT NOT NULL, " +
                MovieListEntry.COLUMN_IDMOVIE + " TEXT NOT NULL, " +
                MovieListEntry.COLUMN_ANO + " TEXT, " +
                MovieListEntry.COLUMN_DURACAO + " TEXT, " +
                MovieListEntry.COLUMN_RATING + " TEXT, " +
                MovieListEntry.COLUMN_SINOPSE + " TEXT, " +
                MovieListEntry.COLUMN_FAVORITOS + " INT, " +
                MovieListEntry.COLUMN_IMAGEPATH + " TEXT " +
                "); ";

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieListEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
