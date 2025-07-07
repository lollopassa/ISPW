package com.biteme.app.bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public class ArchivioBean {

    private Integer idOrdine;
    private List<ProdottoBean> prodotti;
    private List<Integer> quantita;
    private BigDecimal totale;
    private LocalDateTime dataArchiviazione;

    /* ===== getter / setter ===== */
    public Integer getIdOrdine() { return idOrdine; }
    public void setIdOrdine(Integer idOrdine) { this.idOrdine = idOrdine; }

    public List<ProdottoBean> getProdotti() { return prodotti; }
    public void setProdotti(List<ProdottoBean> prodotti) { this.prodotti = prodotti; }

    public List<Integer> getQuantita() { return quantita; }
    public void setQuantita(List<Integer> quantita) { this.quantita = quantita; }

    public BigDecimal getTotale() { return totale; }
    public void setTotale(BigDecimal totale) { this.totale = totale; }

    public LocalDateTime getDataArchiviazione() { return dataArchiviazione; }
    public void setDataArchiviazione(LocalDateTime dataArchiviazione) {
        this.dataArchiviazione = dataArchiviazione;
    }

    /* ===== validazione ===== */
    public void validate() {
        if (prodotti == null || quantita == null ||
                prodotti.isEmpty() || prodotti.size() != quantita.size())
        {
            throw new IllegalArgumentException("Liste prodotti/quantità non valide.");
        }

        for (int i = 0; i < prodotti.size(); i++) {
            if (prodotti.get(i) == null ||
                    quantita.get(i) == null ||
                    quantita.get(i) <= 0)
            {
                throw new IllegalArgumentException("Riga d'archivio non valida all'indice " + i);
            }
        }

        if (totale == null || totale.signum() < 0)
            throw new IllegalArgumentException("Totale non valido.");
        if (dataArchiviazione == null)
            throw new IllegalArgumentException("Data archiviazione mancante.");
    }
}
