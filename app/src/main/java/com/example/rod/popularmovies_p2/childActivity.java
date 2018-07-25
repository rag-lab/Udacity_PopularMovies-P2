package com.example.rod.popularmovies_p2;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.rod.popularmovies_p2.data.MovieContentPrivider;
import com.example.rod.popularmovies_p2.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.rod.popularmovies_p2.data.MovieContract.MovieListEntry.COLUMN_ANO;
import static com.example.rod.popularmovies_p2.data.MovieContract.MovieListEntry.COLUMN_DURACAO;
import static com.example.rod.popularmovies_p2.data.MovieContract.MovieListEntry.COLUMN_IDMOVIE;
import static com.example.rod.popularmovies_p2.data.MovieContract.MovieListEntry.COLUMN_IMAGEPATH;
import static com.example.rod.popularmovies_p2.data.MovieContract.MovieListEntry.COLUMN_RATING;
import static com.example.rod.popularmovies_p2.data.MovieContract.MovieListEntry.COLUMN_SINOPSE;
import static com.example.rod.popularmovies_p2.data.MovieContract.MovieListEntry.COLUMN_TITULO;
import static com.example.rod.popularmovies_p2.data.MovieContract.MovieListEntry.TABLE_NAME;

public class childActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {

    TextView titulo;
    TextView ano;
    TextView duracao;
    TextView sinopse;
    TextView rating;
    ImageView imgView;
    TextView reviewLabel;

    //http://api.themoviedb.org/3/movie/293660/reviews?api_key=8481813f6a52db086ab5d607c8c94667
    //http://api.themoviedb.org/3/movie/293660/videos?api_key=8481813f6a52db086ab5d607c8c94667

    //ImageView TrailerView;
    //TextView idReview;
    //TextView txtReview;
    //TextView authorReview;

    /* A constant to save and restore the URL do review e do trailer */
    private static final String REVIEW_URL = "REVIEW_URL";
    private static final String TRAILER_URL = "TRAILER_URL";

    private RecyclerView reviewRecyclerView;
    private ReviewAdapter reviewRecyclerAdapter;
    private List<Review> listReviews;

    private RecyclerView trailerRecyclerView;
    private TrailerAdapter trailerRecyclerAdapter;
    private List<Trailer> listTrailer;

    private ToggleButton favTogglebutton;

    private SQLiteDatabase mDb; //database
    private MovieDbHelper dbHelper;
    public Movies movieDetail = new Movies("","","", "","","","","");

    private String urlJSON="";
    private String movieID="";
    private String movieTrailer="";
    private String movieReview="";

    private static final int reviewLoaderID= 23;
    private static final int trailerLoaderID= 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //bt back

        titulo = (TextView) findViewById(R.id.titulo);
        imgView = (ImageView) findViewById(R.id.thumb);
        ano = (TextView) findViewById(R.id.ano);
        sinopse = (TextView) findViewById(R.id.sinopse);
        rating = (TextView) findViewById(R.id.rating);

        reviewLabel = (TextView) findViewById(R.id.labelReview);

        favTogglebutton = (ToggleButton) findViewById(R.id.toggleButton);

        Intent intent2 = getIntent();

        if(intent2.hasExtra("titulo")){
            String strTitulo = intent2.getStringExtra("titulo");
            titulo.setText(strTitulo);
            movieDetail.setTitulo(strTitulo);
        }else{
            titulo.setText("");
        }


        //id
        if(intent2.hasExtra("movieID")){

            movieID = intent2.getStringExtra("movieID");
            movieTrailer = String.format( getString(R.string.base_url_trailer), movieID, getString(R.string.APIKEY));
            movieReview = String.format( getString(R.string.base_url_review), movieID, getString(R.string.APIKEY));
            movieDetail.setId(movieID); //add to save later

        }else{

        }


        //poster
        if(intent2.hasExtra("thumb_path")){

            String posterPath = intent2.getStringExtra("thumb_path");

            Picasso.with(getBaseContext())
                    .load(posterPath)
                    .into(imgView);

            movieDetail.setPathtofile(posterPath);
        }else{
            titulo.setText("");
        }

        //ano
        if(intent2.hasExtra("ano")){
            String tmpData = intent2.getStringExtra("ano");
            //String[] parts = tmpData.split("-");
            //String tmpAno = parts[0];
            ano.setText(tmpData);
            movieDetail.setAno(tmpData);

        }else{
            ano.setText("");
        }

        //rating
        if(intent2.hasExtra("rating")){
            String ratingNumber = intent2.getStringExtra("rating");
            rating.setText(ratingNumber);
            movieDetail.setRating(ratingNumber);

        }else{
            rating.setText("");
            Log.v("RAG","no rating:");
        }

        //sinpose
        if(intent2.hasExtra("sinopse")){
            String tmpSinopse = intent2.getStringExtra("sinopse");
            sinopse.setText(tmpSinopse);
            movieDetail.setSinopse(tmpSinopse);
        }else{
            sinopse.setText("");
        }

        //checa se esta na lista de favoritos e troca o bt;
        //
        //
        Boolean movieExist = hasMovie(movieDetail.getId());

        if(movieExist) {
            favTogglebutton.toggle();
        };

        //review
        //create/configure recyclerview
        reviewRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewReview);
        reviewRecyclerView.setHasFixedSize(true);

        //set layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        reviewRecyclerView.setLayoutManager(linearLayoutManager);

        listReviews = new ArrayList<>();

        //recycleadapter review
        reviewRecyclerAdapter = new ReviewAdapter(listReviews, getBaseContext());
        //----fim review-----


        //trailer

        //create/configure recyclerview
        trailerRecyclerView = (RecyclerView) findViewById(R.id.recyclerviewTrailer);
        trailerRecyclerView.setHasFixedSize(true);

        //set layout
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        trailerRecyclerView.setLayoutManager(linearLayoutManager2);

        listTrailer = new ArrayList<>();

        //recycleadapter review
        trailerRecyclerAdapter = new TrailerAdapter(listTrailer, getBaseContext());
        //----fim review-----


        try {

            if(isOnline()){

                Bundle queryBundle = new Bundle();
                queryBundle.putString("REVIEW_URL", movieReview);
                queryBundle.putString("TRAILER_URL", movieTrailer);

                LoaderManager loaderManager = getSupportLoaderManager();
                Loader<String> reviewLoader = loaderManager.getLoader(reviewLoaderID);
                Loader<String> trailerLoader = loaderManager.getLoader(trailerLoaderID);


                // COMPLETED (23) If the Loader was null, initialize it. Else, restart it.
                if (reviewLoader == null) {
                    loaderManager.initLoader(reviewLoaderID, queryBundle, this);
                } else {
                    loaderManager.restartLoader(reviewLoaderID, queryBundle, this);
                }


                if (trailerLoader == null) {
                    loaderManager.initLoader(trailerLoaderID, queryBundle, this);
                } else {
                    loaderManager.restartLoader(trailerLoaderID, queryBundle, this);
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

        switch(id)
        {

            //load review
            case 23:
                return new AsyncTaskLoader<String>(this) {

                    String jsonReviewResult;

                    @Override
                    protected void onStartLoading() {

                        /* If no arguments were passed, we don't have a query to perform. Simply return. */
                        if (args == null) {
                            Toast toast = Toast.makeText(getApplicationContext(), "no argumento to load review",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            return;

                        }else {

                            //pega do cache ou carrega
                            String aa = (jsonReviewResult != null) ? "pega cache" : "pega internet";

                            //Log.v("RAG", "onStartLoading review() " + aa + " / " + jsonReviewResult);

                            if (jsonReviewResult != null) {
                                deliverResult(jsonReviewResult);
                            } else {
                                this.forceLoad();
                            }
                        }
                    }

                    @Override
                    public String loadInBackground() {


                        try {

                            //Log.v("RAG","loadInBackground review():"+REVIEW_URL);
                            String searchQueryUrlString = args.getString(REVIEW_URL);

                            URL urlSearch = new URL(searchQueryUrlString);
                            String strJsonResult = "";

                            strJsonResult = Util.getResponseFromHttpUrl(urlSearch);
                            jsonReviewResult = strJsonResult;

                            JSONObject jsonObject = new JSONObject(strJsonResult);
                            JSONArray array = jsonObject.getJSONArray("results");

                            //Log.v("RAG",strJsonResult);

                            listReviews.clear();
                            if (array.length() > 0) {

                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject o1 = array.getJSONObject(i);
                                    String id = o1.getString("id");
                                    String author = o1.getString("author");
                                    String content = o1.getString("content");
                                    String link = o1.getString("url");

                                    Review item = new Review(id, author, content, link);
                                    if(listReviews.size() <4) listReviews.add(item);
                                    //Log.v("RAG", "author:"+author);
                                    //Log.v("RAG", "content:"+content);
                                }

                                if (array.length() == 1) {
									runOnUiThread(new Runnable() {
										public void run() {
											reviewLabel.setText("Review:");
										}
									});                                    
                                }

                            } else {
									runOnUiThread(new Runnable() {
										public void run() {
											reviewLabel.setText("No reviews yet");
										}
									});

                            }

                        } catch (IOException e1) {
                            e1.printStackTrace();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return jsonReviewResult;
                    }

                    @Override
                    public void deliverResult(String githubJson) {

                        //Log.v("RAG", "deliveryResult loader review:"+githubJson);
                        //Toast toast = Toast.makeText(getApplicationContext(), "deliverResult",
                        //        Toast.LENGTH_SHORT);
                        //toast.show();

                        jsonReviewResult = githubJson;
                        super.deliverResult(githubJson);
                    }

                };

            //load trailer
            case 24:
                return new AsyncTaskLoader<String>(this) {
                    String jsonTrailerResult;

                    @Override
                    protected void onStartLoading() {

                        /* If no arguments were passed, we don't have a query to perform. Simply return. */
                        if (args == null) {
                            Toast toast = Toast.makeText(getApplicationContext(), "no argumento to load trailer",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }

                        //pega do cache ou carrega
                        String aa = (jsonTrailerResult != null) ? "pega cache" : "pega internet";

                        //Log.v("RAG", "onStartLoading trailer() " + aa + " / " + jsonTrailerResult);

                        if (jsonTrailerResult != null) {
                            deliverResult(jsonTrailerResult);
                        } else {

                            //Log.v("RAG","forceLoad review()");
                            this.forceLoad();
                        }

                    }

                    @Override
                    public String loadInBackground() {


                        try {

                            //Log.v("RAG","loadInBackground trailer():"+args.getString(TRAILER_URL));
                            String searchQueryUrlString = args.getString(TRAILER_URL);

                            URL urlSearch = new URL(searchQueryUrlString);
                            String strJsonResult = "";

                            strJsonResult = Util.getResponseFromHttpUrl(urlSearch);
                            jsonTrailerResult = strJsonResult;

                            JSONObject jsonObject = new JSONObject(strJsonResult);
                            JSONArray array = jsonObject.getJSONArray("results");

                            //Log.v("RAG","trailer length:"+array.length());

                            listTrailer.clear();
                            if (array.length() > 0) {

                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject o1 = array.getJSONObject(i);
                                    String id = o1.getString("id");
                                    String name = o1.getString("name");
                                    String key = o1.getString("key");

                                    Trailer item = new Trailer(id, name, key);

                                    if (listTrailer.size()<2) listTrailer.add(item);

                                    //Log.v("RAG", "name:"+name);
                                    //Log.v("RAG", "content:"+content);
                                }

                                if (array.length() == 1) {
                                    //reviewLabel.setText("Review:");
                                }

                            } else {
                                //reviewLabel.setText("No reviews yet");
                            }

                            //Log.v("RAG", "listTrailer size:" + listTrailer.size());

                        } catch (IOException e1) {
                            e1.printStackTrace();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return jsonTrailerResult;
                    }

                    @Override
                    public void deliverResult(String githubJson) {

                        //Log.v("RAG", "deliveryResult loader review:"+githubJson);
                        //Toast toast = Toast.makeText(getApplicationContext(), "deliverResult",
                        //        Toast.LENGTH_SHORT);
                        //toast.show();

                        jsonTrailerResult = githubJson;
                        super.deliverResult(githubJson);
                    }

                };

            default:
                return null;

        }

    };


    @Override
    public void onLoadFinished(Loader<String> loader, String data) {

        switch(loader.getId())
        {

            case 23:
                reviewRecyclerView.setAdapter(reviewRecyclerAdapter);

            case 24:
                trailerRecyclerView.setAdapter(trailerRecyclerAdapter);

            default:
        }

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    /*
    //END LOADER
    */


    //bt favorites
    public void toggleclick(View v) {

        String movieid = movieDetail.getId();

        //add to fav
        if (favTogglebutton.isChecked()) {

            if(!hasMovie(movieid)){
                addNewMovieToTable(movieDetail);
            }else{

            }

        //remove from fav
        }else{
            int x = deleteMovie(movieid);
        }
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

    //add a new movie to the db
    private void addNewMovieToTable(Movies movie){

        long idMovieRecord = 0;
        try
        {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_TITULO, movie.getTitulo());
            cv.put(COLUMN_IDMOVIE, movie.getId());
            cv.put(COLUMN_ANO, movie.getAno());
            cv.put(COLUMN_SINOPSE, movie.getSinopse());
            cv.put(COLUMN_RATING, movie.getRating());
            cv.put(COLUMN_DURACAO, movie.getDuracao());
            cv.put(COLUMN_IMAGEPATH, movie.getPathtofile());

            Uri uri = getContentResolver().insert(
                    MovieContract.MovieListEntry.CONTENT_URI, cv);

            //Toast.makeText(getBaseContext(),uri.toString(), Toast.LENGTH_LONG).show();

        }
        catch (SQLException e) {
            Log.v("SQLException ",e.toString());
            //too bad :(
        }

    }

    //delete movieID
    private int deleteMovie(String movieID)
    {
        int count = 0;
        try
        {

            count = getContentResolver().delete(
                    MovieContract.MovieListEntry.CONTENT_URI,
                    "idmovie = ?",
                    new String[]{movieID});

            return count;
        }
        catch (SQLException e) {
            Log.v("RAG",e.toString());
            return count;
        }

    }


    public boolean hasMovie(String id) {

        boolean hasMovie=false;

        String[] projection = new String[] { COLUMN_IMAGEPATH,
                COLUMN_DURACAO,
                COLUMN_ANO,
                COLUMN_SINOPSE,
                COLUMN_RATING,
                COLUMN_IDMOVIE,
                COLUMN_TITULO};

        Cursor movieCursor = getContentResolver().query(MovieContract.MovieListEntry.CONTENT_URI,
                projection,
                COLUMN_IDMOVIE+" = ?",
                new String[]{id},
                MovieContract.MovieListEntry._ID);

         hasMovie = movieCursor.moveToFirst();

         return hasMovie;
    }


}
