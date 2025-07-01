package com.biteme.app.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Archivio {

    /** <code>null</code> (o 0) se non collegato a un ordine vero e proprio */
    private Integer idOrdine;

    private List<ArchivioRiga> righe;
    private BigDecimal         totale;
    private LocalDateTime      dataArchiviazione;

    public Archivio() {}

    public Archivio(Integer idOrdine,
                    List<ArchivioRiga> righe,
                    BigDecimal totale,
                    LocalDateTime dataArchiviazione) {
        this.idOrdine          = idOrdine;
        this.righe             = righe;
        this.totale            = totale;
        this.dataArchiviazione = dataArchiviazione;
    }

    /* ===== getter / setter ===== */
    public Integer getIdOrdine()                           { return idOrdine; }
    public void    setIdOrdine(Integer idOrdine)           { this.idOrdine = idOrdine; }

    public List<ArchivioRiga> getRighe()                   { return righe; }
    public void setRighe(List<ArchivioRiga> righe)         { this.righe = righe; }

    public BigDecimal getTotale()                          { return totale; }
    public void       setTotale(BigDecimal totale)         { this.totale = totale; }

    public LocalDateTime getDataArchiviazione()            { return dataArchiviazione; }
    public void          setDataArchiviazione(LocalDateTime d) { this.dataArchiviazione = d; }

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
