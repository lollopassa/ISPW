package com.biteme.app.entity;

import java.util.List;

public class Ordine {

    private int id;
    private List<String> prodotti;
    private List<Integer> quantita;

    // Costruttore
    public Ordine(int id, List<String> prodotti, List<Integer> quantita) {
        this.id = id;
        this.prodotti = prodotti;
        this.quantita = quantita;
    }

    // Getter e setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getProdotti() {
        return prodotti;
    }

    public void setProdotti(List<String> prodotti) {
        this.prodotti = prodotti;
    }

    public List<Integer> getQuantita() {
        return quantita;
    }

    public void setQuantita(List<Integer> quantita) {
        this.quantita = quantita;
    }
}
