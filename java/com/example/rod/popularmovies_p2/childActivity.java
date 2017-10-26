package com.example.rod.popularmovies_p2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rod.popularmovies_p2.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class childActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {

    TextView titulo;
    TextView ano;
    TextView duracao;
    TextView sinopse;
    TextView rating;
    ImageView imgView;

    //http://api.themoviedb.org/3/movie/293660/reviews?api_key=8481813f6a52db086ab5d607c8c94667
    //http://api.themoviedb.org/3/movie/293660/videos?api_key=8481813f6a52db086ab5d607c8c94667

    //ImageView TrailerView;
    //TextView idReview;
    //TextView txtReview;
    //TextView authorReview;

    /* A constant to save and restore the URL do review e do trailer */
    private static final String REVIEW_URL = "";
    private static final String TRAILER_URL = "";

    private RecyclerView reviewRecyclerView;
    private ReviewAdapter reviewRecyclerAdapter;

    private List<Review> listReviews;
    private List<Trailer> listTrailer;

    private String urlJSON="";
    private String movieID="";
    private String pathReview="";
    private String movieReview="";

    private static final int reviewLoaderID= 23;
    private static final int trailerLoaderID= 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        titulo = (TextView) findViewById(R.id.titulo);
        imgView = (ImageView) findViewById(R.id.thumb);
        ano = (TextView) findViewById(R.id.ano);
        //duracao = (TextView) findViewById(R.id.duracao);
        sinopse = (TextView) findViewById(R.id.sinopse);
        rating = (TextView) findViewById(R.id.rating);

        Intent intent2 = getIntent();


        /*
        if(intent2.hasExtra("titulo")){
            titulo.setText(intent2.getStringExtra("titulo"));
        }else{
            titulo.setText("");
        }
        */

        //id
        if(intent2.hasExtra("movieID")){

            movieID = intent2.getStringExtra("movieID");

            pathReview = String.format( getString(R.string.base_url_trailer), movieID, getString(R.string.APIKEY));
            movieReview = String.format( getString(R.string.base_url_review), movieID, getString(R.string.APIKEY));
            //Log.v("RAG","pathReview:"+pathReview);
            //Log.v("RAG","movieReview:"+movieReview);

        }else{
            Log.v("RAG","no movie id");
        }


        //poster
        if(intent2.hasExtra("thumb_path")){

            String posterPath = intent2.getStringExtra("thumb_path");

            Picasso.with(getBaseContext())
                    .load(posterPath)
                    .into(imgView);

        }else{
            titulo.setText("");
        }


        if(intent2.hasExtra("ano")){
            String tmpData = intent2.getStringExtra("ano");
            //String[] parts = tmpData.split("-");
            //String tmpAno = parts[0];
            ano.setText(tmpData);
        }else{
            ano.setText("");
        }


        if(intent2.hasExtra("rating")){
            String ratingNumber = intent2.getStringExtra("rating") + " / 10";
            rating.setText(ratingNumber);
        }else{
            rating.setText("");
        }


        if(intent2.hasExtra("sinopse")){
            String aa = intent2.getStringExtra("sinopse");
            sinopse.setText(aa);
        }else{
            sinopse.setText("");
        }


        //create/configure recyclerview
        reviewRecyclerView = (RecyclerView) findViewById(R.id.recyclerviewReview);
        reviewRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        reviewRecyclerView.setLayoutManager(linearLayoutManager);

        listReviews = new ArrayList<>();

        reviewRecyclerAdapter = new ReviewAdapter(listReviews, getBaseContext());
        reviewRecyclerView.setAdapter(reviewRecyclerAdapter);



        try {

            if(isOnline()){

                Bundle queryBundle = new Bundle();
                queryBundle.putString(REVIEW_URL, movieReview.toString());

                LoaderManager loaderManager = getSupportLoaderManager();

                Loader<String> reviewLoader = loaderManager.getLoader(reviewLoaderID);

                // COMPLETED (23) If the Loader was null, initialize it. Else, restart it.
                if (reviewLoader == null) {
                    Log.v("RAG", "loader review inicializado");
                    loaderManager.initLoader(reviewLoaderID, queryBundle, this);
                } else {
                    Log.v("RAG", "loader review re-inicializado");
                    loaderManager.restartLoader(reviewLoaderID, queryBundle, this);
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

            String jsonReviewResult;

            @Override
            protected void onStartLoading() {

                /* If no arguments were passed, we don't have a query to perform. Simply return. */
                if (args == null) {
                    Toast toast = Toast.makeText(getApplicationContext(), "no argumento to load review",
                            Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                //pega do cache ou carrega
                if (jsonReviewResult != null) {
                    Log.v("RAG","onStartLoading review() "+ jsonReviewResult);
                    deliverResult(jsonReviewResult);
                } else {

                    Toast toast = Toast.makeText(getApplicationContext(), "start loading, force load2()",
                            Toast.LENGTH_SHORT);
                    toast.show();
                    Log.v("RAG","forceLoad2()");

                    this.forceLoad();
                }

            }

            @Override
            public String loadInBackground() {


                try {

                    Log.v("RAG","loadInBackground()2");
                    String searchQueryUrlString = args.getString(REVIEW_URL);

                    URL urlSearch = new URL(searchQueryUrlString);
                    String strJsonResult="";

                    strJsonResult = Util.getResponseFromHttpUrl(urlSearch);

                    JSONObject jsonObject = new JSONObject(strJsonResult);
                    JSONArray array = jsonObject.getJSONArray("results");

                    listReviews.clear();

                    for(int i = 0;i<array.length();i++)
                    {
                        JSONObject o = array.getJSONObject(i);
                        String id = o.getString("id");
                        String author = o.getString("author");
                        String content = o.getString("content");
                        String link = o.getString("url");

                        Review item = new Review(id, author, content, link);
                        listReviews.add(item);
                        //Log.v("RAG", "author:"+author);
                        //Log.v("RAG", "content:"+content);
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

                Log.v("RAG", "deliveryResult2:"+githubJson);
                //Toast toast = Toast.makeText(getApplicationContext(), "deliverResult",
                //        Toast.LENGTH_SHORT);
                //toast.show();

                jsonReviewResult = githubJson;
                super.deliverResult(githubJson);
            }

        };
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<String> loader, String data) {

        //Log.v("RAG", "e");
        //recMainActivity.setAdapter(recyclerAdapter);

        Toast toast = Toast.makeText(getApplicationContext(), "onLoadFinished2",
                Toast.LENGTH_SHORT);
        toast.show();

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<String> loader) {

        Toast toast = Toast.makeText(getApplicationContext(), "onLoaderReset()",
                Toast.LENGTH_SHORT);
        toast.show();
    }

    //
    //END LOADER
    //


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }




    private Cursor getMovie(String movieID) {

        /*
       SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
                KEY_NAME, KEY_PH_NO }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2));
        // return contact
        return contact;
    }
         */

        MovieDbHelper dbHelper = new MovieDbHelper(this);
        SQLiteDatabase mDB = dbHelper.getReadableDatabase();

        String selection = MovieContract.MovieListEntry.COLUMN_IDMOVIE + "=?";
        String[] selectionArgs = {movieID};

        Cursor cursor = mDB.query(
                MovieContract.MovieListEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                MovieContract.MovieListEntry.COLUMN_TITULO,
                "1"
        );

        return cursor;

    }


}
