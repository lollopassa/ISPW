package com.biteme.app.bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ArchivioBean {

    /** può essere {@code null} quando si archivia un movimento “libero” */
    private Integer idOrdine;

    private List<ArchivioRigaBean> righe;
    private BigDecimal             totale;
    private LocalDateTime          dataArchiviazione;

    /* ===== getter / setter ===== */
    public Integer getIdOrdine()                            { return idOrdine; }
    public void    setIdOrdine(Integer idOrdine)            { this.idOrdine = idOrdine; }

    public List<ArchivioRigaBean> getRighe()                { return righe; }
    public void setRighe(List<ArchivioRigaBean> righe)      { this.righe = righe; }

    public BigDecimal getTotale()                           { return totale; }
    public void setTotale(BigDecimal totale)                { this.totale = totale; }

    public LocalDateTime getDataArchiviazione()             { return dataArchiviazione; }
    public void setDataArchiviazione(LocalDateTime data)    { this.dataArchiviazione = data; }

    /* ===== validazione ===== */
    public void validate() {

        if (righe == null || righe.isEmpty())
            throw new IllegalArgumentException("Deve esserci almeno una riga d'archivio.");

        for (ArchivioRigaBean r : righe) {
            if (r.getProdottoBean() == null ||
                    r.getQuantita()    == null ||
                    r.getQuantita()    <= 0)
                throw new IllegalArgumentException("Riga non valida in archivio.");
        }

        if (totale == null || totale.signum() < 0)
            throw new IllegalArgumentException("Totale non valido.");

        if (dataArchiviazione == null)
            throw new IllegalArgumentException("Data archiviazione mancante.");
    }
}
