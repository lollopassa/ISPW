package com.biteme.app.controller;

import com.biteme.app.bean.EmailBean;
import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.exception.PrenotationValidationException;
import com.biteme.app.entities.Prenotazione;
import com.biteme.app.persistence.PrenotazioneDao;
import com.biteme.app.persistence.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
        validazioneCampi(bean);

        LocalTime orario = parseOrario(bean.getOrarioStr());
        int coperti = parseCoperti(bean.getCopertiStr());

                bean.setOrario(orario);
        bean.setCoperti(coperti);

        Prenotazione entity = convertToEntity(bean);
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
        validazioneCampi(bean);

        LocalTime orario = parseOrario(bean.getOrarioStr());
        int coperti = parseCoperti(bean.getCopertiStr());

        bean.setOrario(orario);
        bean.setCoperti(coperti);

        prenotazioneDao.update(convertToEntity(bean));

        if (bean.getEmail() != null && !bean.getEmail().isEmpty()) {
            inviaEmailConferma(bean);
        }
        return bean;
    }

    private void validazioneCampi(PrenotazioneBean bean) {
        if (bean.getNomeCliente() == null || bean.getNomeCliente().trim().isEmpty()) {
            throw new PrenotationValidationException("Il nome del cliente non può essere vuoto.");
        }
        if (bean.getData() == null) {
            throw new PrenotationValidationException("Seleziona una data valida.");
        }
        if (bean.getOrarioStr() == null || bean.getOrarioStr().trim().isEmpty()) {
            throw new PrenotationValidationException("Inserisci un orario valido.");
        }
        if (bean.getCopertiStr() == null || bean.getCopertiStr().trim().isEmpty()) {
            throw new PrenotationValidationException("Inserisci il numero di coperti.");
        }
        if (bean.getEmail() != null && !bean.getEmail().isEmpty() &&
                !bean.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,7}$")) {
            throw new PrenotationValidationException("Formato email non valido.");
        }
    }

    private LocalTime parseOrario(String orarioStr) {
        try {
            return LocalTime.parse(orarioStr.trim());
        } catch (DateTimeParseException e) {
            throw new PrenotationValidationException("Formato orario non valido. Usa 'HH:mm'.");
        }
    }

    private int parseCoperti(String copertiStr) {
        try {
            int coperti = Integer.parseInt(copertiStr.trim());
            if (coperti <= 0) {
                throw new PrenotationValidationException("I coperti devono essere maggiori di 0.");
            }
            return coperti;
        } catch (NumberFormatException e) {
            throw new PrenotationValidationException("Numero coperti non valido.");
        }
    }

    private Prenotazione convertToEntity(PrenotazioneBean bean) {
        return new Prenotazione(
                bean.getId(),
                bean.getNomeCliente(),
                bean.getOrario(),
                bean.getData(),
                bean.getNote(),
                bean.getEmail(),
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
        bean.setEmail(entity.getEmail());
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

    private void inviaEmailConferma(PrenotazioneBean bean) {
        try {
            EmailBean emailBean = emailController.composeEmailFromPrenotazione(bean);
            emailBean.setDestinatario(bean.getEmail());
            emailController.sendEmail(emailBean);
        } catch (Exception e) {
            throw new IllegalArgumentException("Errore durante l'invio dell'email di conferma: " + e.getMessage());
        }
    }
}
