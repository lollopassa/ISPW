package com.biteme.app.bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ArchivioBean {

    private int idOrdine;
    private List<ArchivioRigaBean> righe;
    private BigDecimal totale;
    private LocalDateTime dataArchiviazione;

    public int getIdOrdine() { return idOrdine; }
    public void setIdOrdine(int idOrdine) { this.idOrdine = idOrdine; }

    public List<ArchivioRigaBean> getRighe() { return righe; }
    public void setRighe(List<ArchivioRigaBean> righe) { this.righe = righe; }

    public BigDecimal getTotale() { return totale; }
    public void setTotale(BigDecimal totale) { this.totale = totale; }

    public LocalDateTime getDataArchiviazione() { return dataArchiviazione; }
    public void setDataArchiviazione(LocalDateTime dataArchiviazione) {
        this.dataArchiviazione = dataArchiviazione;
    }

    public void validate() {
        if (righe == null || righe.isEmpty()) {
            throw new IllegalArgumentException("Deve esserci almeno una riga d'archivio.");
        }
        for (ArchivioRigaBean r : righe) {
            if (r.getProdottoBean() == null || r.getQuantita() == null || r.getQuantita() <= 0) {
                throw new IllegalArgumentException("Riga non valida in archivio.");
            }
        }
        if (totale == null || totale.signum() < 0) {
            throw new IllegalArgumentException("Totale non valido.");
        }
        if (dataArchiviazione == null) {
            throw new IllegalArgumentException("Data archiviazione mancante.");
        }
    }
}