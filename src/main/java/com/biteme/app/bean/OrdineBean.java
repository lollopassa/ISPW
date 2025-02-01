package com.biteme.app.bean;

import java.util.List;

public class OrdineBean {

    private int id; // ID univoco dell'ordine
    private List<String> prodotti; // Nomi o ID dei prodotti
    private List<Integer> quantita; // Quantità di ogni prodotto

    // Getter e setter per il campo id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter e setter per il campo prodotti
    public List<String> getProdotti() {
        return prodotti;
    }

    public void setProdotti(List<String> prodotti) {
        this.prodotti = prodotti;
    }

    // Getter e setter per il campo quantita
    public List<Integer> getQuantita() {
        return quantita;
    }

    public void setQuantita(List<Integer> quantita) {
        this.quantita = quantita;
    }
}
