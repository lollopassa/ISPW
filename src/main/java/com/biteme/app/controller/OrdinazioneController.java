package com.biteme.app.controller;


import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.StatoOrdinazione;

import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.persistence.Configuration;

import java.util.List;

public class OrdinazioneController { // Controller che gestisce la logica di alto livello sulle ordinazioni

    private final OrdinazioneDao ordinazioneDao;

    public OrdinazioneController() {                    // costruttore
        this.ordinazioneDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdinazioneDao();                   // ottiene DAO da factory
    }

    public void creaOrdine(OrdinazioneBean bean) throws OrdinazioneException { // salva nuova ordinazione
        try {
            Ordinazione ord = bean.toEntity();          // mapping DTO → entity
            ordinazioneDao.store(ord);                  // persiste entity
            bean.setId(ord.getId());                   // aggiorna id sul DTO
        } catch (Exception e) {                         // gestisce errori
            throw new OrdinazioneException(
                    "Errore nella creazione dell'ordinazione: " + e.getMessage(), e);
        }
    }

    public List<OrdinazioneBean> getOrdini() {          // restituisce tutte le ordinazioni
        return ordinazioneDao.getAll()
                .stream()
                .map(OrdinazioneBean::fromEntity)       // mapping entity → DTO
                .toList();
    }

    public void eliminaOrdinazione(int id) throws OrdinazioneException { // elimina per id
        if (!ordinazioneDao.exists(id))                 // verifica esistenza
            throw new OrdinazioneException("L'ordinazione con ID " + id + " non esiste.");
        try {
            ordinazioneDao.delete(id);                  // cancella dal DAO
        } catch (Exception e) {                         // errori delete
            throw new OrdinazioneException("Errore nell'eliminazione dell'ordinazione: " + e.getMessage(), e);
        }
    }

    public void aggiornaStatoOrdinazione(int ordineId, StatoOrdinazione nuovoStato) throws OrdinazioneException { // update stato
        try {
            ordinazioneDao.aggiornaStato(ordineId, nuovoStato); // delega al DAO
        } catch (Exception e) {                                 // gestisce errori update
            throw new OrdinazioneException("Errore nell'aggiornamento dello stato dell'ordinazione: " + e.getMessage(), e);
        }
    }
}