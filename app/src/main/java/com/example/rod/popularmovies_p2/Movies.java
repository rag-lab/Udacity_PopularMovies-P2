package com.example.rod.popularmovies_p2;

/**
 * Created by rod on 7/26/2017.
 */

public class Movies {

    private String titulo;
    private String urlCapa;
    private String sinopse;
    private String duracao;
    private String ano;
    private String rating;
    private String id;


    public Movies(String titulo, String urlCapa, String duracao, String ano, String sinopse, String rating, String id) {
        this.titulo = titulo;
        this.urlCapa = urlCapa;
        this.duracao = duracao;
        this.ano = ano;
        this.sinopse = sinopse;
        this.rating = rating;
        this.id = id;

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    public String getDuracao() {
        return duracao;
    }

    public void setDuracao(String duracao) {
        this.duracao = duracao;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getUrlCapa() {
        String param = "";
        return param + urlCapa;
    }

    public void setUrlCapa(String urlCapa) {
        this.urlCapa = urlCapa;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

}
