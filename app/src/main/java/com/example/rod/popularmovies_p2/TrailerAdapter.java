package com.example.rod.popularmovies_p2;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.myViewHolder3>{

    private int mNumberItems;
    List<Trailer> listaTrailers;
    private Context context;


    public TrailerAdapter(List<Trailer> listaTrailes, Context context) {
        this.context = context;
        this.listaTrailers = listaTrailes;
    }

    class myViewHolder3 extends RecyclerView.ViewHolder{

        //cria o item que esta no layout do xml
        public TextView titulo;
        public ImageButton youtubeImgBt;
        //public TextView comentario;
        //public CardView cv;
        //public ImageView capaFilme;


        public myViewHolder3(final View itemView)
        {
            super(itemView);
            titulo = (TextView) itemView.findViewById(R.id.txtTitulo);
            youtubeImgBt = (ImageButton) itemView.findViewById(R.id.imageButton);
        }

    }


    @Override
    public myViewHolder3 onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();

        int layoutIdForListItem = R.layout.trailer_thumb;

        LayoutInflater inflater = LayoutInflater.from(context); // cria inflater
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately); //
        myViewHolder3 viewHolder = new myViewHolder3(view); //cria viewholder com a view para retornar

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(TrailerAdapter.myViewHolder3 holderTrailer, int position) {

        Trailer listItem = listaTrailers.get(position);

        final String tr_id = listItem.getId();
        final String tr_name = listItem.getName();
        final String tr_key = listItem.getKey();

        holderTrailer.titulo.setText(tr_name);

        //onclick
        holderTrailer.youtubeImgBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String url_youtube = context.getString(R.string.base_url_youtube) + tr_key;
                Log.v("RAG","url_youtube:"+ url_youtube);

                Uri webpage = Uri.parse(url_youtube);


                //intent paa abrir app
                Intent intentApp = new Intent(Intent.ACTION_VIEW, webpage);
                intentApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentApp.setPackage("com.google.android.youtube");

                //paraa abrir no browser
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);

                try {
                    view.getContext().startActivity(intentApp);
                } catch (ActivityNotFoundException ex) {
                    view.getContext().startActivity(webIntent);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        int a ;

        if(listaTrailers != null && !listaTrailers.isEmpty()) {
            a = listaTrailers.size();
        }
        else
        {
            a = 0;
        }
        return a;
    }
}
