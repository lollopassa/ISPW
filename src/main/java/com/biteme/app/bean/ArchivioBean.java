package com.biteme.app.bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ArchivioBean {
    private int idOrdine;
    private List<String> prodotti;
    private List<Integer> quantita;
    private BigDecimal totale;
    private LocalDateTime dataArchiviazione;


    public void validate() {
        if (prodotti == null || quantita == null ||
                prodotti.size() != quantita.size()) {
            throw new IllegalArgumentException(
                    "Prodotti e quantitÃ  devono essere non null e di uguale lunghezza.");
        }
        if (totale == null || totale.signum() < 0) {
            throw new IllegalArgumentException("Totale non valido.");
        }
        if (dataArchiviazione == null) {
            throw new IllegalArgumentException("Data archiviazione mancante.");
        }
    }


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
}