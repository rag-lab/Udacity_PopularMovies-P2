package com.example.rod.popularmovies_p2;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.rod.popularmovies_p2.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import static com.example.rod.popularmovies_p2.data.MovieContract.MovieListEntry.*;

public class MainActivity extends android.support.v7.app.AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {


    /* A constant to save and restore the URL that is being displayed */
    private static final String SEARCH_URL = "";
    private final String KEY_RECYCLER_STATE = "recycler_state";
    private final String KEY_QUERY_STATE = "url_query";  //chave urlQuery
    private static Bundle mBundleRVState;
    private Parcelable savedRecyclerLayoutState;

    private RecyclerView recMainActivity;
    private RecyclerAdapter recyclerAdapter;
    private List<Movies> listMovies = new ArrayList<>();
    private String urlJSON="";
    private SQLiteDatabase mDb; //database
    private MovieDbHelper dbHelper;

    private static final int thumbLoaderID= 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recMainActivity = (RecyclerView) findViewById(R.id.recMainActivity);
        recMainActivity.setHasFixedSize(true);

        int posterWidth = 600;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getBaseContext(), calculateBestSpanCount(posterWidth));

        recMainActivity.setLayoutManager(gridLayoutManager);
        recyclerAdapter = new RecyclerAdapter(listMovies, getBaseContext());
        recMainActivity.setAdapter(recyclerAdapter);

        //get the saved query
        if (savedInstanceState != null)
        {
            if(savedInstanceState.getSerializable(KEY_QUERY_STATE) != null)
            {
                urlJSON = (String)savedInstanceState.getSerializable(KEY_QUERY_STATE);
            }

            if(savedInstanceState.getParcelable(KEY_RECYCLER_STATE) != null)
            {
                savedRecyclerLayoutState = savedInstanceState.getParcelable(KEY_RECYCLER_STATE);
            }

        }
        else
        {
            urlJSON = String.format( getString(R.string.base_url_popular),getString(R.string.APIKEY));
        }


        try {

            if(isOnline()){

                dbHelper = new MovieDbHelper(this);
                mDb = dbHelper.getWritableDatabase();

                Bundle queryBundle = new Bundle();
                queryBundle.putString(SEARCH_URL, urlJSON);

                LoaderManager loaderManager = getSupportLoaderManager();
                Loader<String> thumbsLoader = loaderManager.getLoader(thumbLoaderID);

                if (thumbsLoader == null) {
                    loaderManager.initLoader(thumbLoaderID, queryBundle, this);
                } else {
                    loaderManager.restartLoader(thumbLoaderID, queryBundle, this);
                }


            }else{

                Toast toast = Toast.makeText(getApplicationContext(), R.string.NOINTERNET,
                        Toast.LENGTH_SHORT);
                toast.show();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        //tToast("onStart");
    }

    @Override
    public void onRestart() {

        super.onRestart();

        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_URL, urlJSON);

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> thumbsLoader = loaderManager.getLoader(thumbLoaderID);

        if (thumbsLoader == null) {
            loaderManager.initLoader(thumbLoaderID, queryBundle, this);
        } else {
            loaderManager.restartLoader(thumbLoaderID, queryBundle, this);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        mBundleRVState = new Bundle();
        Parcelable listState = recMainActivity.getLayoutManager().onSaveInstanceState();
        mBundleRVState.putParcelable(KEY_RECYCLER_STATE, listState);

    }



    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //salva states
        outState.putSerializable(KEY_QUERY_STATE, urlJSON);

        //salva recyclerView
        Parcelable listState = recMainActivity.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(KEY_RECYCLER_STATE, listState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    //
    //LOADER
    //
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {

        return new AsyncTaskLoader<Cursor>(this){

            Cursor mResult;

            @Override
            protected void onStartLoading() {

                if (args == null) return;

                //pega do cache ou carrega
                if (mResult != null) {
                    deliverResult(mResult);
                } else {
                    this.forceLoad();
                }

            }

            @Override
            public Cursor loadInBackground() {

                try {

                    String searchQueryUrlString = args.getString(SEARCH_URL);
                    Cursor cursor;

                    listMovies.clear();

                    if (searchQueryUrlString!="favorites") {

                        URL urlSearch = new URL(searchQueryUrlString);
                        String strJsonResult = Util.getResponseFromHttpUrl(urlSearch);

                        JSONObject jsonObject = new JSONObject(strJsonResult);
                        JSONArray array = jsonObject.getJSONArray("results");

                        for (int i = 0; i < array.length(); i++) {

                            JSONObject o = array.getJSONObject(i);

                            //get base poster path
                            String poster_path = getString(R.string.base_url_poster);
                            poster_path += o.optString("poster_path");

                            String titulo = o.optString("original_title");
                            String ano = o.optString("release_date");
                            String duracao = "";//o.optString("vote_count");
                            String sinopse = o.optString("overview");
                            String rating = o.optString("vote_average");
                            rating+= " / 10";
                            String id = o.optString("id");

                            //Bitmap poster = loadImageFromURL(poster_path);
                            //String pathToPosterFile = saveBitmap(poster, id + ".png");

                            Movies item = new Movies(titulo,
                                    poster_path,
                                    duracao,
                                    ano,
                                    sinopse,
                                    rating,
                                    id,
                                    "pathToPosterFile");

                            listMovies.add(item);

                        }

                    }
                    else
                    {

                        try {

                            String[] projection = new String[] { COLUMN_IMAGEPATH,
                                                                COLUMN_DURACAO,
                                                                COLUMN_ANO,
                                                                COLUMN_SINOPSE,
                                                                COLUMN_RATING,
                                                                COLUMN_IDMOVIE,
                                                                COLUMN_TITULO};

                            Cursor favCursor = getContentResolver().query(MovieContract.MovieListEntry.CONTENT_URI,
                                    projection,
                                    null,
                                    null,
                                    MovieContract.MovieListEntry._ID);

                            if (favCursor.moveToFirst())
                            {
                                while(!favCursor.isAfterLast()){

                                    String titulo = favCursor.getString(favCursor.getColumnIndex("titulo"));
                                    String posterPath = favCursor.getString(favCursor.getColumnIndex("image"));
                                    String ano = favCursor.getString(favCursor.getColumnIndex("ano"));
                                    String duracao = favCursor.getString(favCursor.getColumnIndex("duracao"));
                                    String sinopse = favCursor.getString(favCursor.getColumnIndex("sinopse"));
                                    String rating = favCursor.getString(favCursor.getColumnIndex("rating"));
                                    String id = favCursor.getString(favCursor.getColumnIndex("idmovie"));

                                    Movies item = new Movies(titulo,
                                            posterPath,
                                            duracao,
                                            ano,
                                            sinopse,
                                            rating,
                                            id,
                                            posterPath);

                                    listMovies.add(item);

                                    favCursor.moveToNext();
                                }
                            }else{

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "No favorites yet :(", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                            //mResult = favCursor;
                            favCursor.close();

                        } catch (Exception e) {
                            Log.e("RAG","exception:"+e.toString());
                            //tToast(e.toString());
                        }

                    }

                }
                catch (IOException e1)
                {
                    //Log.e("RAG", "Failed to asynchronously load data.");
                    e1.printStackTrace();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }

                return mResult;
            }

            @Override
            public void deliverResult(Cursor data) {
                mResult = data;
                super.deliverResult(data);
            }

        };

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        recMainActivity.setAdapter(recyclerAdapter);

        // restore RecyclerView state
        if (mBundleRVState != null) {
            Parcelable listState = mBundleRVState.getParcelable(KEY_RECYCLER_STATE);
            recMainActivity.getLayoutManager().onRestoreInstanceState(listState);
        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /*
    //END LOADER
    */


    private int calculateBestSpanCount(int posterWidth) {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float screenWidth = outMetrics.widthPixels;
        int dd = Math.round(screenWidth / posterWidth);
        //tToast(""+dd);
        return Math.round(dd);

    }


    private void tToast(String s) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, s, duration);
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int menuId = item.getItemId();


        //popular
        if(menuId==R.id.menuit1){
            urlJSON = String.format( getString(R.string.base_url_popular),getString(R.string.APIKEY));
        //highest rated
        }else if(menuId==R.id.menuit2)
        {
            urlJSON = String.format( getString(R.string.base_url_toprated),getString(R.string.APIKEY));
        }
        //favorites
        else if(menuId==R.id.menuit3)
        {
            urlJSON = "favorites";
        }

        try {

            if(isOnline()){

                Bundle queryBundle = new Bundle();
                queryBundle.putString(SEARCH_URL, urlJSON);

                LoaderManager loaderManager = getSupportLoaderManager();
                Loader<String> thumbsLoader = loaderManager.getLoader(thumbLoaderID);

                if (thumbsLoader == null) {
                    loaderManager.initLoader(thumbLoaderID, queryBundle, this);
                } else {
                    loaderManager.restartLoader(thumbLoaderID, queryBundle, this);
                }

            }else{

                Toast toast = Toast.makeText(getApplicationContext(), R.string.NOINTERNET,
                        Toast.LENGTH_SHORT);
                toast.show();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onOptionsItemSelected(item);

    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


}
