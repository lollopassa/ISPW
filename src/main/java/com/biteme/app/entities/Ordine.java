package com.biteme.app.entities;

import java.util.List;

public class Ordine {

    private int id;
    private List<String> prodotti;
    private List<Integer> quantita;

        public Ordine(int id,  List<String> prodotti, List<Integer> quantita) {
        this.id = id;
        this.prodotti = prodotti;
        this.quantita = quantita;
    }

    public List<Integer> getQuantita() {
        return quantita;
    }

    public List<String> getProdotti() {
        return prodotti;
    }

    public int getId() {
        return id;
    }



    public void setQuantita(List<Integer> quantita) {
        this.quantita = quantita;
    }

    public void setProdotti(List<String> prodotti) {
        this.prodotti = prodotti;
    }

    public void setId(int id) {
        this.id = id;
    }
}