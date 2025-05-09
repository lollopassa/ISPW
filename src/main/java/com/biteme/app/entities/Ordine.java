package com.biteme.app.entities;

import java.math.BigDecimal;
import java.util.List;

public class Ordine {

    private int id;
    private List<String> prodotti;
    private List<Integer> quantita;
    private List<BigDecimal> prezzi;


    public Ordine(int id, List<String> prodotti, List<Integer> quantita, List<BigDecimal> prezzi) {
        this.id = id;
        this.prodotti = prodotti;
        this.quantita = quantita;
        this.prezzi = prezzi;
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

    public List<BigDecimal> getPrezzi() { return prezzi; }
    public void setPrezzi(List<BigDecimal> prezzi) { this.prezzi = prezzi; }

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