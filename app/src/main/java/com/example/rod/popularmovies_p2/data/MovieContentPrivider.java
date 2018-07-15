package com.example.rod.popularmovies_p2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.example.rod.popularmovies_p2.data.MovieContract.MovieListEntry.*;

import com.example.rod.popularmovies_p2.MovieDbHelper;

public class MovieContentPrivider extends ContentProvider {


    // Define final integer constants for the directory of tasks and a single item.
    /// It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
    public static final int TASKS = 100;
    public static final int TASK_WITH_ID = 101;


    // CDeclare a static variable for the Uri matcher that you construct
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Define a static buildUriMatcher method that associates URI's with their int match
    /**
     Initialize a new matcher object without any matches,
     then use .addURI(String authority, String path, int match) to add matches
     */
    public static UriMatcher buildUriMatcher() {

        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the task directory and a single item by ID.
         */
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIE, TASKS);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIE + "/#", TASK_WITH_ID);

        return uriMatcher;
    }


    // Member variable for a TaskDbHelper that's initialized in the onCreate() method
    private MovieDbHelper movieDbHelper;


    @Override
    public boolean onCreate() {

        // Complete onCreate() and initialize a TaskDbhelper on startup
        // [Hint] Declare the DbHelper as a global variable

        Context context = getContext();
        movieDbHelper = new MovieDbHelper(context);
        return true;

    }

    // Implement query to handle requests for data by URI
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = movieDbHelper.getReadableDatabase();

        Cursor retCursor;

        retCursor =  db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        // Get access to the task database (to write new data to)
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks directory
        //int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned

        long id = db.insert(TABLE_NAME, null, values);

        if (id > 0) {
            returnUri = ContentUris.withAppendedId(MovieContract.MovieListEntry.CONTENT_URI, id);
            // Notify the resolver if the uri has been changed, and return the newly inserted URI
            getContext().getContentResolver().notifyChange(uri, null);
            // Return constructed uri (this points to the newly inserted row of data)
            return returnUri;

        } else {
            throw new android.database.SQLException("Failed to insert row into " + uri);
        }

    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {

        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();

        int count = 0;
        count = db.delete(TABLE_NAME, s, strings);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;

    }


    @Override
    //not used
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }


}
