package com.biteme.app.controller;

import com.biteme.app.bean.EmailBean;
import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.exception.ValidationException;
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

    public void creaPrenotazione(String nomeCliente, String orarioStr, LocalDate data, String email, String note, String copertiStr) {
        validazioneCampi(nomeCliente, orarioStr, data, email, copertiStr);

        LocalTime orario = parseOrario(orarioStr);
        int coperti = parseCoperti(copertiStr);

        PrenotazioneBean bean = new PrenotazioneBean();
        bean.setNomeCliente(nomeCliente);
        bean.setOrario(orario);
        bean.setData(data);
        bean.setEmail(email);
        bean.setNote(note);
        bean.setCoperti(coperti);

        Prenotazione entity = convertToEntity(bean);
        prenotazioneDao.store(entity);
        bean.setId(entity.getId());

        if(email != null && !email.isEmpty()) {
            inviaEmailConferma(bean);
        }
    }

    public List<PrenotazioneBean> getPrenotazioniByData(LocalDate data) {
        return prenotazioneDao.getByData(data).stream()
                .map(this::convertToBean)
                .toList();
    }

    public PrenotazioneBean modificaPrenotazione(int id, String nomeCliente, String orarioStr, LocalDate data, String email, String note, String copertiStr) {
        validazioneCampi(nomeCliente, orarioStr, data, email, copertiStr);

        LocalTime orario = parseOrario(orarioStr);
        int coperti = parseCoperti(copertiStr);

        PrenotazioneBean bean = new PrenotazioneBean();
        bean.setId(id);
        bean.setNomeCliente(nomeCliente);
        bean.setOrario(orario);
        bean.setData(data);
        bean.setEmail(email);
        bean.setNote(note);
        bean.setCoperti(coperti);

        prenotazioneDao.update(convertToEntity(bean));

        if(email != null && !email.isEmpty()) {
            inviaEmailConferma(bean);
        }
        return bean;
    }

    private void validazioneCampi(String nomeCliente, String orarioStr, LocalDate data, String email, String copertiStr) {
        if (nomeCliente == null || nomeCliente.trim().isEmpty()) {
            throw new ValidationException("Il nome del cliente non può essere vuoto.");
        }
        if (data == null) {
            throw new ValidationException("Seleziona una data valida.");
        }
        if (orarioStr == null || orarioStr.trim().isEmpty()) {
            throw new ValidationException("Inserisci un orario valido.");
        }
        if (copertiStr == null || copertiStr.trim().isEmpty()) {
            throw new ValidationException("Inserisci il numero di coperti.");
        }
        if (email != null && !email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,7}$")) {
            throw new ValidationException("Formato email non valido.");
        }
    }

    private LocalTime parseOrario(String orarioStr) {
        try {
            return LocalTime.parse(orarioStr.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException("Formato orario non valido. Usa 'HH:mm'.");
        }
    }

    private int parseCoperti(String copertiStr) {
        try {
            int coperti = Integer.parseInt(copertiStr.trim());
            if (coperti <= 0) throw new ValidationException("I coperti devono essere maggiori di 0.");
            return coperti;
        } catch (NumberFormatException e) {
            throw new ValidationException("Numero coperti non valido.");
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
