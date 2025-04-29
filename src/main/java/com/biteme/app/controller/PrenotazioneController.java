package com.biteme.app.controller;

import com.biteme.app.bean.EmailBean;
import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.entities.Prenotazione;
import com.biteme.app.exception.PrenotationValidationException;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.persistence.PrenotazioneDao;

import java.time.LocalDate;
import java.util.List;

public class PrenotazioneController {

    private final PrenotazioneDao prenotazioneDao;
    private final EmailController emailController;

    public PrenotazioneController() {
        this.prenotazioneDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getPrenotazioneDao();
        this.emailController = new EmailController();
    }

    public void creaPrenotazione(PrenotazioneBean bean) {
        Prenotazione entity = convertToEntity(bean);
        if (prenotazioneDao.existsDuplicate(entity)) {
            throw new PrenotationValidationException(
                    "Esiste già una prenotazione identica (stesso nome, data, orario e numero di coperti)."
            );
        }
        prenotazioneDao.store(entity);
        bean.setId(entity.getId());
        if (bean.getEmail() != null && !bean.getEmail().isEmpty()) {
            inviaEmailConferma(bean);
        }
    }

    public List<PrenotazioneBean> getPrenotazioniByData(LocalDate data) {
        return prenotazioneDao.getByData(data).stream()
                .map(this::convertToBean)
                .toList();
    }

    public PrenotazioneBean modificaPrenotazione(PrenotazioneBean bean) {
        Prenotazione entity = convertToEntity(bean);
        if (prenotazioneDao.existsDuplicate(entity)) {
            throw new PrenotationValidationException(
                    "Modifica non consentita: esiste già una prenotazione identica."
            );
        }
        prenotazioneDao.update(entity);
        if (bean.getEmail() != null && !bean.getEmail().isEmpty()) {
            inviaEmailConferma(bean);
        }
        return bean;
    }

    public void eliminaPrenotazione(int id) {
        if (prenotazioneDao.exists(id)) {
            prenotazioneDao.delete(id);
        } else {
            throw new IllegalArgumentException("La prenotazione con ID " + id + " non esiste.");
        }
    }

    private Prenotazione convertToEntity(PrenotazioneBean b) {
        return new Prenotazione(
                b.getId(),
                b.getNomeCliente(),
                b.getOrario(),
                b.getData(),
                b.getNote(),
                b.getEmail(),
                b.getCoperti()
        );
    }

    private PrenotazioneBean convertToBean(Prenotazione e) {
        PrenotazioneBean b = new PrenotazioneBean();
        b.setId(e.getId());
        b.setNomeCliente(e.getNomeCliente());
        b.setData(e.getData());
        b.setOrario(e.getOrario());
        b.setNote(e.getNote());
        b.setEmail(e.getEmail());
        b.setCoperti(e.getCoperti());
        return b;
    }

    private void inviaEmailConferma(PrenotazioneBean bean) {
        try {
            EmailBean emailBean = emailController.composeEmailFromPrenotazione(bean);
            emailBean.setDestinatario(bean.getEmail());
            emailController.sendEmail(emailBean);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Errore durante l'invio dell'email di conferma: " + e.getMessage(), e
            );
        }
    }
}
