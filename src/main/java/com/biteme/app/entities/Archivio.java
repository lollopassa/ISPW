package com.biteme.app.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Archivio {

    private int idOrdine;
    private List<ArchivioRiga> righe;         // ora contiene oggetti ArchivioRiga
    private BigDecimal totale;
    private LocalDateTime dataArchiviazione;

    public Archivio() {}

    public Archivio(int idOrdine, List<ArchivioRiga> righe, BigDecimal totale, LocalDateTime dataArchiviazione) {
        this.idOrdine = idOrdine;
        this.righe = righe;
        this.totale = totale;
        this.dataArchiviazione = dataArchiviazione;
    }

    public int getIdOrdine() { return idOrdine; }
    public void setIdOrdine(int idOrdine) { this.idOrdine = idOrdine; }

    public List<ArchivioRiga> getRighe() { return righe; }
    public void setRighe(List<ArchivioRiga> righe) { this.righe = righe; }

    public BigDecimal getTotale() { return totale; }
    public void setTotale(BigDecimal totale) { this.totale = totale; }

    public LocalDateTime getDataArchiviazione() { return dataArchiviazione; }
    public void setDataArchiviazione(LocalDateTime dataArchiviazione) {
        this.dataArchiviazione = dataArchiviazione;
    }

    @Override
    public String toString() {
        return "Archivio{" +
                "idOrdine=" + idOrdine +
                ", righe=" + righe +
                ", totale=" + totale +
                ", dataArchiviazione=" + dataArchiviazione +
                '}';
    }
}