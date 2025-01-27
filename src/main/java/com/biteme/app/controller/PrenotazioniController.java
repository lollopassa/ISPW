package com.biteme.app.controller;

import com.biteme.app.bean.PrenotazioniBean;
import com.biteme.app.entity.Prenotazione;
import com.biteme.app.persistence.PrenotazioneDao;
import com.biteme.app.util.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class PrenotazioniController {

    private final PrenotazioneDao prenotazioneDao;

    public PrenotazioniController() {
        this.prenotazioneDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getPrenotazioneDao();
    }

    /**
     * Crea una prenotazione utilizzando i dati forniti tramite il bean.
     */
    public void creaPrenotazione(PrenotazioniBean prenotazioniBean) {
        // Controlla se la data è specificata, altrimenti usa la data corrente
        LocalDate dataPrenotazione = prenotazioniBean.getData() != null
                ? prenotazioniBean.getData()
                : LocalDate.now();

        Prenotazione prenotazione = new Prenotazione(
                0, // ID sarà generato automaticamente
                prenotazioniBean.getNomeCliente(),
                prenotazioniBean.getOrario(),    // Orario della prenotazione
                dataPrenotazione,               // Aggiunto il campo data
                prenotazioniBean.getNote(),     // Note (sostituisce dettagli)
                prenotazioniBean.getTelefono(), // Numero di telefono del cliente
                prenotazioniBean.getCoperti()   // Nuovo campo: numero coperti
        );

        prenotazioneDao.store(prenotazione); // Salva la prenotazione nel database
    }

    /**
     * Recupera prenotazioni filtrate per orario.
     */
    public List<Prenotazione> getPrenotazioniByOrario(LocalTime orario) {
        return prenotazioneDao.getByOrario(orario);
    }

    /**
     * Recupera prenotazioni filtrate per data.
     */
    public List<Prenotazione> getPrenotazioniByData(LocalDate data) {
        return prenotazioneDao.getByData(data); // Usa il DAO per filtrare in base alla data
    }

    /**
     * Elimina una prenotazione specifica per ID.
     */
    public void eliminaPrenotazione(int id) {
        if (prenotazioneDao.exists(id)) {
            prenotazioneDao.delete(id); // Cancella la prenotazione
        } else {
            throw new IllegalArgumentException("La prenotazione con ID " + id + " non esiste.");
        }
    }
}