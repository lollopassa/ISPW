package com.biteme.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Archivio {

    private int idOrdine;
    private List<String> prodotti;
    private List<Integer> quantita;
    private BigDecimal totale;
    private LocalDateTime dataArchiviazione;

    // Costruttore vuoto (opzionale, utile per alcune librerie/framework)
    public Archivio() {}

    // Costruttore completo per inizializzazione rapida
    public Archivio(int idOrdine, List<String> prodotti, List<Integer> quantita, BigDecimal totale, LocalDateTime dataArchiviazione) {
        this.idOrdine = idOrdine;
        this.prodotti = prodotti;
        this.quantita = quantita;
        this.totale = totale;
        this.dataArchiviazione = dataArchiviazione;
    }

    // Getter e Setter per ciascun campo
    public int getIdOrdine() {
        return idOrdine;
    }

    public void setIdOrdine(int idOrdine) {
        this.idOrdine = idOrdine;
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

    public BigDecimal getTotale() {
        return totale;
    }

    public void setTotale(BigDecimal totale) {
        this.totale = totale;
    }

    public LocalDateTime getDataArchiviazione() {
        return dataArchiviazione;
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