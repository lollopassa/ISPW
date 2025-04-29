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

    public void validate() {
        if (prodotti == null || quantita == null) {
            throw new IllegalArgumentException("Prodotti e quantità non possono essere nulli.");
        }
        if (prodotti.isEmpty()) {
            throw new IllegalArgumentException("Nessun prodotto da archiviare.");
        }
        if (prodotti.size() != quantita.size()) {
            throw new IllegalArgumentException("Il numero di prodotti non corrisponde alle quantità.");
        }
        for (Integer q : quantita) {
            if (q == null || q <= 0) {
                throw new IllegalArgumentException("Quantità non valide: " + q);
            }
        }
    }
}