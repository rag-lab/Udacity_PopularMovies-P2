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
    private static Bundle mBundleRVState;

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

        //create/configure recyclerview
        recMainActivity = (RecyclerView) findViewById(R.id.recMainActivity);
        recMainActivity.setHasFixedSize(true);

        int posterWidth = 600;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getBaseContext(), calculateBestSpanCount(posterWidth));

        recMainActivity.setLayoutManager(gridLayoutManager);
        recyclerAdapter = new RecyclerAdapter(listMovies, getBaseContext());
        recMainActivity.setAdapter(recyclerAdapter);

        urlJSON = String.format( getString(R.string.base_url_popular),getString(R.string.APIKEY));
        Log.v("RAG", urlJSON);

        try {

            if(isOnline()){

                dbHelper = new MovieDbHelper(this);
                mDb = dbHelper.getWritableDatabase();

                Bundle queryBundle = new Bundle();
                queryBundle.putString(SEARCH_URL, urlJSON);

                LoaderManager loaderManager = getSupportLoaderManager();
                Loader<String> thumbsLoader = loaderManager.getLoader(thumbLoaderID);

                if (thumbsLoader == null) {
                    //Log.v("RAG", "loader inicializado");
                    loaderManager.initLoader(thumbLoaderID, queryBundle, this);
                } else {

                    //Log.v("RAG", "loader re-inicializado");
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

    }

    @Override
    public void onStart() {
        super.onStart();
        //tToast("onStart");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        //tToast("onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        //tToast("onResume");
    }

    @Override
    public void onPause() {
        super.onPause();

        // save RecyclerView state
        mBundleRVState = new Bundle();
        Parcelable listState = recMainActivity.getLayoutManager().onSaveInstanceState();
        mBundleRVState.putParcelable(KEY_RECYCLER_STATE, listState);
        tToast("onPause: listState saved");

    }



    @Override
    public void onStop() {
        super.onStop();
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

                // COMPLETED (6) If args is null, return.
                /* If no arguments were passed, we don't have a query to perform. Simply return. */
                if (args == null) return;

                //pega do cache ou carrega
                if (mResult != null) {
                    Log.v("RAG","onStartLoading from cache() "+ mResult);
                    deliverResult(mResult);
                } else {

                    Log.v("RAG","forceLoad()");
                    this.forceLoad();
                }

            }

            @Override
            public Cursor loadInBackground() {

                try {

                    //Log.v("RAG", "loadInBackground():" + isOnline());

                    String searchQueryUrlString = args.getString(SEARCH_URL);
                    Cursor cursor;

                    listMovies.clear();

                    if (searchQueryUrlString!="favorites") {

                        URL urlSearch = new URL(searchQueryUrlString);
                        String strJsonResult = Util.getResponseFromHttpUrl(urlSearch);

                        //mResult = strJsonResult;

                        JSONObject jsonObject = new JSONObject(strJsonResult);
                        JSONArray array = jsonObject.getJSONArray("results");
                        //Log.v("RAG","strJsonResult:"+strJsonResult);

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
                                Toast toast = Toast.makeText(getApplicationContext(), "No favorites yet",
                                        Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            //mResult = favCursor;
                            favCursor.close();

                        } catch (Exception e) {
                            tToast(e.toString());
                        }

                    }

                }
                catch (IOException e1)
                {
                    Log.e("RAG", "Failed to asynchronously load data.");
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

                //Log.v("RAG","deliverResult:"+githubJson);
                //mResult = data;
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
        Log.v("RAG", "onLoaderReset");
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
                    //Log.v("RAG", "loader inicializado");
                    loaderManager.initLoader(thumbLoaderID, queryBundle, this);
                } else {
                    //Log.v("RAG", "loader re-inicializado");
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



    public Bitmap loadImageFromURL(String src){

        String fileName = String.format("%d.png", System.currentTimeMillis());
        Bitmap myBitmap;
        //Log.v("RAG","loadImageFromURL:"+src.toString());

        try {

            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            myBitmap = BitmapFactory.decodeStream(input);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return myBitmap;

    }


    public String saveBitmap(Bitmap bm, String name) throws Exception {

        String tempFilePath = Environment.getExternalStorageDirectory() + "/moviedb/" +  name;
        Log.v("RAG", "saveBitmap(name):"+tempFilePath);

        File tempFile = new File(tempFilePath);

        //file not exist
        if (!tempFile.exists()) {

            //create dir if not exists
            if (!tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }

            tempFile.delete();
            tempFile.createNewFile();

            int quality = 100;
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);

            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            bm.compress(Bitmap.CompressFormat.JPEG, quality, bos);

            bos.flush();
            bos.close();

            bm.recycle();

        }else{
            Log.v("RAG", "  file already on phone:");
        }

        return tempFilePath;
    }




}
