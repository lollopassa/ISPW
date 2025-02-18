package com.biteme.app.controller;

import com.biteme.app.bean.EmailBean;
import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.exception.GoogleAuthException;
import com.biteme.app.googleapi.GmailEmailSender;
import com.biteme.app.googleapi.GoogleAuthUtility;
import javax.mail.MessagingException;
import java.io.IOException;


public class EmailController {

    public void sendEmail(EmailBean emailBean) throws MessagingException, IOException {
        String accessToken = null;
        String fromEmail = null;

        try {
            // Ottieni l'access token tramite GoogleAuthUtility
            accessToken = GoogleAuthUtility.authenticate();

            // Ottieni i dati dell'utente autenticato utilizzando il token
            GoogleAuthUtility.GoogleUserData userData = GoogleAuthUtility.getGoogleUserData(accessToken);
            if (userData != null) {
                fromEmail = userData.getEmail();
            } else {
                throw new IllegalArgumentException("I dati dell'utente autenticato non sono disponibili.");
            }
        } catch (InterruptedException | GoogleAuthException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Il processo di autenticazione è stato interrotto", e);
        }

        // Esegui i controlli sui valori recuperati
        if (accessToken == null) {
            throw new IllegalStateException("Access token non disponibile. Controlla l'autenticazione con Google.");
        }

        if (fromEmail == null || fromEmail.isEmpty()) {
            throw new IllegalStateException("Indirizzo email del mittente non disponibile.");
        }

        // Invia l'email utilizzando l'access token e l'indirizzo email del mittente
        try {
            GmailEmailSender.sendEmail(accessToken, fromEmail,
                    emailBean.getDestinatario(),
                    emailBean.getSubject(),
                    emailBean.getBody());
        } catch (MessagingException e) {
            throw new MessagingException("Errore durante l'invio dell'email: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalStateException("Errore generico durante l'invio dell'email", e);

        }
    }


    public EmailBean composeEmailFromPrenotazione(PrenotazioneBean bean) {
        String subject = "Conferma Prenotazione per " + bean.getNomeCliente();
        StringBuilder body = new StringBuilder();
        body.append("Gentile ").append(bean.getNomeCliente()).append(",\n\n");
        body.append("La tua prenotazione è stata registrata con i seguenti dettagli:\n");
        body.append("Data: ").append(bean.getData()).append("\n");
        body.append("Orario: ").append(bean.getOrario()).append("\n");
        body.append("Coperti: ").append(bean.getCoperti()).append("\n");
        body.append("Note: ").append(bean.getNote() != null && !bean.getNote().isEmpty() ? bean.getNote() : "Nessuna").append("\n\n");
        body.append("Grazie per aver prenotato con noi!");
        return new EmailBean(null, subject, body.toString());
    }
}
