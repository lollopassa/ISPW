package com.biteme.app.entity;

import java.math.BigDecimal;

public class Prodotto {

    private int id;
    private String nome;
    private BigDecimal prezzo;
    private Categoria categoria;
    private boolean disponibile;

    // Costruttore
    public Prodotto(int id, String nome, BigDecimal prezzo, Categoria categoria, boolean disponibile) {
        this.id = id;
        this.nome = nome;
        this.prezzo = prezzo;
        this.categoria = categoria;
        this.disponibile = disponibile;
    }

    // Getter e Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public boolean isDisponibile() {
        return disponibile;
    }

    public void setDisponibile(boolean disponibile) {
        this.disponibile = disponibile;
    }
}