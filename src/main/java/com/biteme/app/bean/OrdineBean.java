package com.biteme.app.bean;

import java.util.List;

public class OrdineBean {

    private List<String> prodotti; // Nomi o ID dei prodotti
    private List<Integer> quantita; // Quantità di ogni prodotto

    // Getter e setter
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
