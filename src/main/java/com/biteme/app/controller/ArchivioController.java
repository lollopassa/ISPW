package com.biteme.app.controller;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.entity.Archivio;
import com.biteme.app.persistence.ArchivioDao;
import com.biteme.app.util.Configuration;

public class ArchivioController {
    private ArchivioDao archivioDao;

    public ArchivioController() {
        this.archivioDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getArchivioDao();
    }

    public void salvaInArchivio(Archivio archivio) {
        archivioDao.store(archivio);
    }

    public void archiviaOrdine(ArchivioBean archivioBean) {
        // Converte ArchivioBean in Archivio
        Archivio archivio = new Archivio();
        archivio.setIdOrdine(archivioBean.getIdOrdine());
        archivio.setProdotti(archivioBean.getProdotti());
        archivio.setQuantita(archivioBean.getQuantita());
        archivio.setTotale(archivioBean.getTotale());
        archivio.setDataArchiviazione(archivioBean.getDataArchiviazione());

        // Salva l'archivio utilizzando il DAO
        salvaInArchivio(archivio);
    }
}