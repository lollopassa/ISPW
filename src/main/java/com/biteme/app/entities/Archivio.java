package com.biteme.app.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Archivio {

    private int idOrdine;
    private List<String> prodotti;
    private List<Integer> quantita;
    private BigDecimal totale;
    private LocalDateTime dataArchiviazione;

    public Archivio() {}

    // Costruttore completo per inizializzazione rapida
    public Archivio(int idOrdine, List<String> prodotti, List<Integer> quantita, BigDecimal totale, LocalDateTime dataArchiviazione) {
        this.idOrdine = idOrdine;
        this.prodotti = prodotti;
        this.quantita = quantita;
        this.totale = totale;
        this.dataArchiviazione = dataArchiviazione;
    }

    public int getIdOrdine() {
        return idOrdine;
    }

    public List<String> getProdotti() {
        return prodotti;
    }

    public List<Integer> getQuantita() {
        return quantita;
    }

    public BigDecimal getTotale() {
        return totale;
    }

    public LocalDateTime getDataArchiviazione() {
        return dataArchiviazione;
    }



    public void setIdOrdine(int idOrdine) {
        this.idOrdine = idOrdine;
    }

    public void setProdotti(List<String> prodotti) {
        this.prodotti = prodotti;
    }

    public void setQuantita(List<Integer> quantita) {
        this.quantita = quantita;
    }

    public void setTotale(BigDecimal totale) {
        this.totale = totale;
    }

    public void setDataArchiviazione(LocalDateTime dataArchiviazione) {
        this.dataArchiviazione = dataArchiviazione;
    }



    @Override
    public String toString() {
        return "Archivio{" +
                "idOrdine=" + idOrdine +
                ", prodotti=" + prodotti +
                ", quantita=" + quantita +
                ", totale=" + totale +
                ", dataArchiviazione=" + dataArchiviazione +
                '}';
    }
}