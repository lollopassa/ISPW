package com.biteme.app.boundary;

import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.bean.EmailBean;
import com.biteme.app.controller.PrenotazioneController;
import com.biteme.app.controller.EmailController;
import com.biteme.app.exception.EmailSendingException;
import com.biteme.app.exception.PrenotationValidationException;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class PrenotazioneBoundary {
    private final PrenotazioneController prenotazioneController;
    private final EmailController emailController;

    public PrenotazioneBoundary() {
        this.prenotazioneController = new PrenotazioneController();
        this.emailController = new EmailController();
    }


    public void creaPrenotazione(PrenotazioneBean bean) throws PrenotationValidationException {
        bean.validate();
        prenotazioneController.creaPrenotazione(bean);
    }


    public PrenotazioneBean modificaPrenotazione(PrenotazioneBean bean) throws PrenotationValidationException {
        bean.validate();
        return prenotazioneController.modificaPrenotazione(bean);
    }


    public void eliminaPrenotazione(int id) {
        prenotazioneController.eliminaPrenotazione(id);
    }


    public List<PrenotazioneBean> getPrenotazioniByData(LocalDate data) {
        return prenotazioneController.getPrenotazioniByData(data);
    }


    public void inviaEmail(PrenotazioneBean bean, String destinatario) throws EmailSendingException {
        EmailBean email = emailController.composeEmailFromPrenotazione(bean);
        email.setDestinatario(destinatario);
        try {
            emailController.sendEmail(email);
        } catch (MessagingException | IOException e) {
            throw new EmailSendingException("Errore durante l'invio dell'email di conferma", e);
        }
    }
}