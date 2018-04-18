package com.example.rod.popularmovies_p2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
        //Log.v("RAG", "listMovies size():"+listMovies.size());

        recyclerAdapter = new RecyclerAdapter(listMovies, getBaseContext());

        recMainActivity.setAdapter(recyclerAdapter);
        urlJSON = String.format( getString(R.string.base_url_popular),getString(R.string.APIKEY));
        Log.v("RAG", urlJSON);

        try {

            if(isOnline()){

                //dbHelper = new MovieDbHelper(this);
                //mDb = dbHelper.getWritableDatabase();

                //if(hasDbRecords()){
                 if(false){
                    Log.v("RAG", "db has records");

                }else{

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
                    //Log.v("RAG","onStartLoading from cache() "+ mResult);
                    deliverResult(mResult);
                } else {

                    //Log.v("RAG","forceLoad()");
                    this.forceLoad();
                }

            }

            @Override
            public String loadInBackground() {

                try {

                    //Log.v("RAG", "loadInBackground():" + isOnline());

                    String searchQueryUrlString = args.getString(SEARCH_URL);

                    URL urlSearch = new URL(searchQueryUrlString);
                    String strJsonResult = Util.getResponseFromHttpUrl(urlSearch);
                    mResult = strJsonResult;

                    JSONObject jsonObject = new JSONObject(strJsonResult);
                    JSONArray array = jsonObject.getJSONArray("results");

                    listMovies.clear();

                    for (int i = 0; i < array.length(); i++) {

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

                        //long numID = addNewMovies(item);
                        //Log.v("RAG","pathToPosterFile:"+pathToPosterFile);

                    }

                }
                catch (IOException e1)
                {
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
            public void deliverResult(String githubJson) {

                //Log.v("RAG","deliverResult:"+githubJson);
                mResult = githubJson;
                super.deliverResult(githubJson);
            }

        };

    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {

        recMainActivity.setAdapter(recyclerAdapter);
        Log.v("RAG","onLoadFinished listMovies:"+listMovies.size());
        //Toast toast = Toast.makeText(getApplicationContext(), "finish loading movies:"+ loader.getId(),Toast.LENGTH_SHORT);
        //toast.show();
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

        Log.v("RAG", "onLoaderReset");
        //Toast toast = Toast.makeText(getApplicationContext(), "loader reseted",Toast.LENGTH_SHORT);
        //toast.show();
    }

    //
    //END LOADER
    //


    //not in use...
    public class loadDataInBackground extends  AsyncTask<URL, Void, String> {

        @Override
        protected void onPostExecute(String s) {

            recMainActivity.setAdapter(recyclerAdapter);
            //recyclerAdapter.notifyDataStateChanged();
            Log.v("RAG","onPostExecute"+s.toString());
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }


        @Override
        protected String doInBackground(URL... params) {

            Log.v("RAG","doInBackground");
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
                            id,"");

                    listMovies.add(item);

                    //long numID = addNewMovies(item);
                    //Log.v("RAG","numID:"+numID);

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


    //
    // DB functions
    //

    //add a new movie to the db
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
            cv.put(COLUMN_IMAGEPATH, movie.getPathtofile());


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


    //query for empty db
    private boolean hasDbRecords(){

        Boolean rowExists = false;

        try {

            Cursor mCursor = mDb.query(TABLE_NAME,
                                null,
                                null,
                                null,
                                null,
                                null,
                                COLUMN_TITULO
                    );


            if (mCursor.moveToFirst())
            {
                rowExists = true;
            }

        }
        catch (SQLException e) {
            Log.v("RAG","hasDbRecords:"+e.toString());
                //too bad :(
        }

        return rowExists;

    }

}
