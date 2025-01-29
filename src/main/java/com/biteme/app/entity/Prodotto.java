package com.biteme.app.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Prodotto {

    private int id;
    private String nome;
    private int quantita;
    private BigDecimal prezzo;
    private String categoria;
    private LocalDate dataScadenza;
    private boolean disponibile;

    public Prodotto() {
    }

    public Prodotto(int id, String nome, int quantita, BigDecimal prezzo, String categoria, LocalDate dataScadenza, boolean disponibile) {
        this.id = id;
        this.nome = nome;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.categoria = categoria;
        this.dataScadenza = dataScadenza;
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

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public LocalDate getDataScadenza() {
        return dataScadenza;
    }

    public void setDataScadenza(LocalDate dataScadenza) {
        this.dataScadenza = dataScadenza;
    }

    public boolean isDisponibile() {
        return disponibile;
    }

    public void setDisponibile(boolean disponibile) {
        this.disponibile = disponibile;
    }
}