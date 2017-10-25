package com.example.rod.popularmovies_p2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by rod on 7/26/2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.myViewHolder> {

    private int mNumberItems;

    List<Movies> listaFilmes;
    private String imagePathOnPhone="";
    private Context context;
    private int mCount;



    public RecyclerAdapter(List<Movies> listaFilmes, Context context){
        this.context = context;
        this.listaFilmes = listaFilmes;
        //this.mCount = count;
    }


    class myViewHolder extends RecyclerView.ViewHolder{

        //cria o item que esta no layout do xml
        public TextView titulo;
        public ImageView capaFilme;


        public myViewHolder(final View itemView)
        {
            super(itemView);
            capaFilme = (ImageView) itemView.findViewById(R.id.imageView1);
        }

    }


    public Bitmap loadBitmapFromView(View v) {
        //Log.v("RAG", "load bitmap from view");
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
                Bitmap.Config.ARGB_8888);
        b.setHasAlpha(true);
        Canvas c = new Canvas(b);
        v.draw(c);
        v.invalidate();
        return b;
    }



    public String saveBitmap(Bitmap bm, String name) throws Exception {

        String tempFilePath = Environment.getExternalStorageDirectory() + "/moviedb/" +  name;
        Log.v("RAG", "image path on phone:"+tempFilePath);

        File tempFile = new File(tempFilePath);
        if (!tempFile.exists()) {
            if (!tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }
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

        return tempFilePath;
    }



    @Override
    public myViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();

        int layoutIdForListItem = R.layout.item_thumb;

        LayoutInflater inflater = LayoutInflater.from(context); // cria inflater
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately); //

        myViewHolder viewHolder = new myViewHolder(view); //cria viewholder com a view para retornar

        return viewHolder;

    }


    @Override
    public void onBindViewHolder(final RecyclerAdapter.myViewHolder holder, int position) {

        Movies listItem = listaFilmes.get(position);

        final String r_titulo = listItem.getTitulo();
        final String r_thumbPath = listItem.getUrlCapa();
        final String r_ano = listItem.getAno();
        final String r_duracao= "200min"; //not used;
        final String r_rating = listItem.getRating();
        final String r_sinopse = listItem.getSinopse();
        final String r_movieID = listItem.getId();

        /*
        Log.v("RAG", ">>r_titulo:"+r_titulo);
        Log.v("RAG", ">>r_ano:"+r_ano);
        Log.v("RAG", ">>r_rating:"+r_rating);
        Log.v("RAG", ">>r_sinopse:"+r_sinopse);
        Log.v("RAG", ">>r_movieID:"+r_movieID);
        Log.v("RAG", ">>imagePathOnPhone:"+imagePathOnPhone);
        Log.v("RAG", ">>------------------------------");
        */

        //Picasso.with(context)
        //       .load(listItem.getUrlCapa())
        //       .into(holder.capaFilme);

        Picasso.with(context)
                .load(r_thumbPath)
                .into(holder.capaFilme, new Callback() {

                    @Override
                    public void onSuccess() {
                        //Log.d("RAG", "onSuccess:"+r_thumbPath);

                        try{

                            //Bitmap bmPosterImage = loadBitmapFromView(holder.capaFilme);
                            //String fileName = String.format("%d.png", System.currentTimeMillis());
                            //imagePathOnPhone = saveBitmap(bmPosterImage,fileName);
                            //Log.v("RAG", "saving image:"+imagePathOnPhone);
                            //Log.v("RAG", "path :"+aa);

                            /*
                            Log.v("RAG", "r_titulo:"+r_titulo);
                            Log.v("RAG", "r_ano:"+r_ano);
                            Log.v("RAG", "r_rating:"+r_rating);
                            Log.v("RAG", "r_sinopse:"+r_sinopse);
                            Log.v("RAG", "r_movieID:"+r_movieID);
                            Log.v("RAG", "imagePathOnPhone:"+imagePathOnPhone);
                            Log.v("RAG", "------------------------------");
                            */

                        }catch (Exception ex){
                            Log.v("RAG", "fail saving image:"+ex.toString());
                        }
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
                    }

                });


        holder.capaFilme.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(), childActivity.class);

                intent.putExtra("titulo", r_titulo); //titulo
                intent.putExtra("thumb_path", r_thumbPath); //thumb path
                intent.putExtra("ano", r_ano); //ano
                intent.putExtra("duracao",r_duracao); //duracao
                intent.putExtra("rating", r_rating); //rating
                intent.putExtra("sinopse", r_sinopse); //sinopse
                intent.putExtra("movieID", r_movieID); //sinopse

                view.getContext().startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {

        int a ;

        if(listaFilmes != null && !listaFilmes.isEmpty()) {
            a = listaFilmes.size();
        }
        else
        {
            a = 0;
        }

        //Log.v("RAG", "mcount:"+mCount);
        //Log.v("RAG", "a:"+a);


        return a;
    }


}
