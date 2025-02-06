package com.biteme.app.controller;

import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.model.Prenotazione;
import com.biteme.app.persistence.PrenotazioneDao;
import com.biteme.app.util.Configuration;

import java.time.LocalDate;
import java.util.List;

public class PrenotazioneController {

    private final PrenotazioneDao prenotazioneDao;

    public PrenotazioneController() {
        this.prenotazioneDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getPrenotazioneDao();
    }

    public void creaPrenotazione(PrenotazioneBean bean) {
        Prenotazione prenotazione = convertToEntity(bean);
        prenotazioneDao.store(prenotazione);
    }

    /**
     * Restituisce i bean delle prenotazioni filtrate per data.
     */
    public List<PrenotazioneBean> getPrenotazioniByData(LocalDate data) {
        return prenotazioneDao.getByData(data).stream()
                .map(this::convertToBean)
                .toList();
    }

    public void modificaPrenotazione(PrenotazioneBean bean) {
        Prenotazione prenotazione = convertToEntity(bean);
        prenotazioneDao.update(prenotazione);
    }

    private Prenotazione convertToEntity(PrenotazioneBean bean) {
        return new Prenotazione(
                bean.getId(),
                bean.getNomeCliente(),
                bean.getOrario(),
                bean.getData(),
                bean.getNote(),
                bean.getTelefono(),
                bean.getCoperti()
        );
    }

    private PrenotazioneBean convertToBean(Prenotazione entity) {
        PrenotazioneBean bean = new PrenotazioneBean();
        bean.setId(entity.getId());
        bean.setNomeCliente(entity.getNomeCliente());
        bean.setData(entity.getData());
        bean.setOrario(entity.getOrario());
        bean.setNote(entity.getNote());
        bean.setTelefono(entity.getTelefono());
        bean.setCoperti(entity.getCoperti());
        return bean;
    }

    public void eliminaPrenotazione(int id) {
        if (prenotazioneDao.exists(id)) {
            prenotazioneDao.delete(id);
        } else {
            throw new IllegalArgumentException("La prenotazione con ID " + id + " non esiste.");
        }
    }
}
