package com.biteme.app.controller;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.util.mapper.BeanEntityMapperFactory;

import java.util.List;

public class OrdinazioneController {

    private final OrdinazioneDao ordinazioneDao;
    private final BeanEntityMapperFactory mapperFactory = BeanEntityMapperFactory.getInstance();

    public OrdinazioneController() {
        this.ordinazioneDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdinazioneDao();
    }

    /* ---------- CRUD ---------- */

    public void creaOrdine(OrdinazioneBean bean) throws OrdinazioneException {
        try {
            Ordinazione entity = mapperFactory.toEntity(bean, OrdinazioneBean.class);
            int id = ordinazioneDao.create(entity);   // <-- ora ritorna l'id
            bean.setId(id);
        } catch (Exception e) {
            throw new OrdinazioneException("Errore nella creazione...", e);
        }
    }


    public List<OrdinazioneBean> getOrdini() {
        return ordinazioneDao.getAll().stream()
                .map(entity -> mapperFactory.toBean(entity, OrdinazioneBean.class))
                .toList();
    }

    public void eliminaOrdinazione(int id) throws OrdinazioneException {
        if (!ordinazioneDao.exists(id))
            throw new OrdinazioneException("L'ordinazione con ID " + id + " non esiste.");
        try {
            ordinazioneDao.delete(id);
        } catch (Exception e) {
            throw new OrdinazioneException(
                    "Errore nell'eliminazione dell'ordinazione: " + e.getMessage(), e);
        }
    }

    public void aggiornaStatoOrdinazione(int ordineId,
                                         StatoOrdinazione nuovoStato) throws OrdinazioneException {
        try {
            ordinazioneDao.aggiornaStato(ordineId, nuovoStato);
        } catch (Exception e) {
            throw new OrdinazioneException(
                    "Errore nell'aggiornamento dello stato dell'ordinazione: " + e.getMessage(), e);
        }
    }
}
