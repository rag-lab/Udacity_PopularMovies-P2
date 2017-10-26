package com.example.rod.popularmovies_p2;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by rodrigo on 9/30/2017.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.myViewHolder2>{


    private int mNumberItems;
    List<Review> listaReviews;
    private Context context;


    public ReviewAdapter(List<Review> listaReviews, Context context){
        this.context = context;
        this.listaReviews = listaReviews;
    }


    class myViewHolder2 extends RecyclerView.ViewHolder{

        //cria o item que esta no layout do xml
        public TextView nome;
        public TextView comentario;
        public CardView cv;
        //public ImageView capaFilme;


        public myViewHolder2(final View itemView)
        {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            nome = (TextView) itemView.findViewById(R.id.cv_name);
            comentario = (TextView) itemView.findViewById(R.id.cv_review);
        }

    }


    @Override
    public myViewHolder2 onCreateViewHolder(ViewGroup viewGroup, int viewType) {


        Context context = viewGroup.getContext();

        int layoutIdForListItem = R.layout.cardview_review;

        LayoutInflater inflater = LayoutInflater.from(context); // cria inflater
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately); //

        myViewHolder2 viewHolder = new myViewHolder2(view); //cria viewholder com a view para retornar

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(ReviewAdapter.myViewHolder2 holderReview, int position) {


        Review listItem = listaReviews.get(position);

        final String rev_id = listItem.getId();
        final String rev_author = listItem.getAuthor();
        final String rev_comentario = listItem.getContent();
        final String rev_url = listItem.getUrl();

        //holderReview.comentario.setText("rev_comentario");
        //holderReview.nome.setText("rev_author");

        holderReview.comentario.setText(rev_comentario);
        holderReview.nome.setText(rev_author);


    }

    @Override
    public int getItemCount() {

        int a ;

        if(listaReviews != null && !listaReviews.isEmpty()) {
            a = listaReviews.size();
        }
        else
        {
            a = 0;
        }
        return a;
    }


}
