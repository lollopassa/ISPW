package com.biteme.app.controller;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.entity.Ordinazione;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.util.Configuration;

import java.util.List;

public class OrdinazioneController {

    private final OrdinazioneDao ordinazioneDao;

    public OrdinazioneController() {
        this.ordinazioneDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdinazioneDao();
    }

    public void creaOrdine(OrdinazioneBean ordinazioneBean) {
        Ordinazione ordinazione = new Ordinazione(
                0, // ID sarà generato automaticamente dal DAO
                ordinazioneBean.getNome(),
                ordinazioneBean.getNumeroClienti(),
                ordinazioneBean.getTipoOrdine(),
                ordinazioneBean.getInfoTavolo(),
                "Nuovo",
                ordinazioneBean.getOrarioCreazione()
        );

        ordinazioneDao.store(ordinazione); // Salva l'ordine nel database
    }


    public List<Ordinazione> getOrdini() {
        return ordinazioneDao.getAll(); // Ritorna tutti gli ordini tramite il DAO
    }


    public void eliminaOrdine(int id) {
        if (ordinazioneDao.exists(id)) {
            ordinazioneDao.delete(id); // Cancella l'ordine
        } else {
            throw new IllegalArgumentException("L'ordine con ID " + id + " non esiste.");
        }
    }

    public boolean isValidTime(String time) {
        // Controlla che il formato rispetti HH:mm utilizzando una regex
        String timePattern = "([01]\\d|2[0-3]):([0-5]\\d)";
        if (!time.matches(timePattern)) {
            return false;
        }

        // Tenta di convertire l'orario in un oggetto LocalTime per ulteriore validità
        try {
            java.time.LocalTime.parse(time);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void eliminaOrdinazione(Integer id) {
        ordinazioneDao.delete(id);
    }
}