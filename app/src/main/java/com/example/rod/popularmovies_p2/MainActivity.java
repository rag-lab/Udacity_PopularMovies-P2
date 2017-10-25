package com.example.rod.popularmovies_p2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.rod.popularmovies_p2.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.rod.popularmovies_p2.data.MovieContract.MovieListEntry.*;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {


    /* A constant to save and restore the URL that is being displayed */
    private static final String SEARCH_URL = "";

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

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getBaseContext(), 2);
        recMainActivity.setLayoutManager(gridLayoutManager);

        //Cursor cursor = getAllMovies();
        recyclerAdapter = new RecyclerAdapter(listMovies, getBaseContext());

        recMainActivity.setAdapter(recyclerAdapter);
        urlJSON = String.format( getString(R.string.base_url_popular),getString(R.string.APIKEY));

        try {

            if(isOnline()){

                // Create a DB helper (this will create the DB if run for the first time)
                dbHelper = new MovieDbHelper(this);

                mDb = dbHelper.getWritableDatabase();

                Bundle queryBundle = new Bundle();
                queryBundle.putString(SEARCH_URL, urlJSON);

                LoaderManager loaderManager = getSupportLoaderManager();
                Loader<String> thumbsLoader = loaderManager.getLoader(thumbLoaderID);

                // COMPLETED (23) If the Loader was null, initialize it. Else, restart it.
                if (thumbsLoader == null) {
                    Log.v("RAG", "loader inicializado");
                    loaderManager.initLoader(thumbLoaderID, queryBundle, this);
                } else {
                    Log.v("RAG", "loader re-inicializado");
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


    //
    //LOADER
    //
    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {


        return new AsyncTaskLoader<String>(this){

            String mResult;

            @Override
            protected void onStartLoading() {

                // COMPLETED (6) If args is null, return.
                /* If no arguments were passed, we don't have a query to perform. Simply return. */
                if (args == null) return;

                //pega do cache ou carrega
                if (mResult != null) {
                    Log.v("RAG","onStartLoading() "+ mResult);
                    deliverResult(mResult);
                } else {

                    Toast toast = Toast.makeText(getApplicationContext(), "start loading, force load()",
                            Toast.LENGTH_SHORT);
                    toast.show();
                    Log.v("RAG","forceLoad()");

                    this.forceLoad();
                }

            }

            @Override
            public String loadInBackground() {

                try {

                    Log.v("RAG","loadInBackground()");
                    String searchQueryUrlString = args.getString(SEARCH_URL);

                    URL urlSearch = new URL(searchQueryUrlString);
                    String strJsonResult = Util.getResponseFromHttpUrl(urlSearch);

                    JSONObject jsonObject = new JSONObject(strJsonResult);
                    JSONArray array = jsonObject.getJSONArray("results");


                    listMovies.clear();

                    for(int i = 0;i<array.length();i++)
                    {
                        JSONObject o = array.getJSONObject(i);

                        //get base poster path
                        String poster_path = getString(R.string.base_url_poster);
                        poster_path += o.getString("poster_path");

                        String titulo = o.getString("original_title");
                        String ano = o.getString("release_date");
                        String duracao = o.getString("vote_count");
                        String sinopse = o.getString("overview");
                        String rating = o.getString("vote_average");
                        String id = o.getString("id");

                        Movies item = new Movies(titulo,
                                poster_path,
                                duracao,
                                ano,
                                sinopse,
                                rating,
                                id);

                        listMovies.add(item);

                        long numID = addNewMovies(item);
                        Log.v("RAG","numID:"+numID);

                    }

                } catch (IOException e1) {
                    e1.printStackTrace();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            public void deliverResult(String githubJson) {

                Log.v("RAG", "deliveryResult:"+githubJson);
                //Toast toast = Toast.makeText(getApplicationContext(), "deliverResult",
                //        Toast.LENGTH_SHORT);
                //toast.show();

                mResult = githubJson;
                super.deliverResult(githubJson);
            }

        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {

        //Log.v("RAG", "e");
        recMainActivity.setAdapter(recyclerAdapter);

        Toast toast = Toast.makeText(getApplicationContext(), "finish loading",
                Toast.LENGTH_SHORT);
        toast.show();

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        Toast toast = Toast.makeText(getApplicationContext(), "reset loading",
                Toast.LENGTH_SHORT);
        toast.show();

    }

    //
    //END LOADER
    //


    public class loadDataInBackground extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPostExecute(String s) {

            recMainActivity.setAdapter(recyclerAdapter);
            //recyclerAdapter.notifyDataStateChanged();
            //Log.v("RAG",s.toString());
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }


        @Override
        protected String doInBackground(URL... params) {

            URL urlSearch = params[0];
            String aa="";

            try {

                aa = Util.getResponseFromHttpUrl(urlSearch);

                JSONObject jsonObject = new JSONObject(aa);
                JSONArray array = jsonObject.getJSONArray("results");

                listMovies.clear();

                for(int i = 0;i<array.length();i++)
                {
                    JSONObject o = array.getJSONObject(i);

                    //get base poster path
                    String poster_path = getString(R.string.base_url_poster);
                    poster_path += o.getString("poster_path");

                    String titulo = o.getString("original_title");
                    String ano = o.getString("release_date");
                    String duracao = o.getString("vote_count");
                    String sinopse = o.getString("overview");
                    String rating = o.getString("vote_average");
                    String id = o.getString("id");

                    Movies item = new Movies(titulo,
                            poster_path,
                            duracao,
                            ano,
                            sinopse,
                            rating,
                            id);

                    listMovies.add(item);

                    //Log.v("RAG", "path:"+poster_path);
                    //Log.v("RAG", "id:"+id);

                }

                recyclerAdapter = new RecyclerAdapter(listMovies, getApplicationContext());


            } catch (IOException e1) {
                e1.printStackTrace();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return aa;
        }


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


        if(menuId==R.id.menuit1){
            Intent startSettings = new Intent(this, SettingsActivity.class);
            startActivity(startSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);

        /*
        if(menuId==R.id.menuit1){
            urlJSON = String.format( getString(R.string.base_url_popular),getString(R.string.APIKEY));
        }else{
            urlJSON = String.format( getString(R.string.base_url_toprated),getString(R.string.APIKEY));
        }

        try {

            if(isOnline()){

                new loadDataInBackground().execute(new URL(urlJSON));

            }else{

                Toast toast = Toast.makeText(getApplicationContext(), R.string.NOINTERNET,
                        Toast.LENGTH_SHORT);
                toast.show();
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        */


    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    //
    // DB functions
    //

    private long addNewMovies(Movies movie){

        long idMovieRecord = 0;
        try
        {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_TITULO, movie.getTitulo());
            cv.put(COLUMN_IDMOVIE, movie.getId());
            cv.put(COLUMN_ANO, movie.getAno());
            cv.put(COLUMN_SINOPSE, movie.getSinopse());
            cv.put(COLUMN_RATING, movie.getRating());
            //cv.put(COLUMN_FAVORITOS, movie.getTitulo());
            cv.put(COLUMN_IMAGEPATH, movie.getUrlCapa());


            mDb.beginTransaction();

            idMovieRecord =  mDb.insert(TABLE_NAME, null, cv);

            mDb.setTransactionSuccessful();
        }
        catch (SQLException e) {
            Log.v("RAG",e.toString());
            //too bad :(
        }

        finally
        {
            mDb.endTransaction();
        }

        return idMovieRecord;

    }



}
