package com.biteme.app.bean;

import java.util.List;

public class OrdineBean {

    private int id;
    private List<String> prodotti;
    private List<Integer> quantita;

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
