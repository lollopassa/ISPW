package com.biteme.app.controller;

import com.biteme.app.bean.EmailBean;
import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.entities.Prenotazione;
import com.biteme.app.exception.PrenotationValidationException;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.persistence.PrenotazioneDao;
import com.biteme.app.util.mapper.BeanEntityMapperFactory;

import java.time.LocalDate;
import java.util.List;

public class PrenotazioneController {

    private final PrenotazioneDao prenotazioneDao;
    private final EmailController emailController;
    private final BeanEntityMapperFactory mapperFactory = BeanEntityMapperFactory.getInstance();

    public PrenotazioneController() {
        this.prenotazioneDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getPrenotazioneDao();
        this.emailController = new EmailController();
    }

    public void creaPrenotazione(PrenotazioneBean bean) {
        Prenotazione entity = mapperFactory.toEntity(bean, PrenotazioneBean.class);

        if (existsDuplicate(entity)) {
            throw new PrenotationValidationException(
                    "Esiste già una prenotazione identica (stesso nome, data, orario e numero di coperti)."
            );
        }

        prenotazioneDao.create(entity);
        bean.setId(entity.getId());

        if (bean.getEmail() != null && !bean.getEmail().isEmpty()) {
            inviaEmailConferma(bean);
        }
    }

    private boolean existsDuplicate(Prenotazione newPrenotazione) {
        List<Prenotazione> sameDateBookings = prenotazioneDao.getByData(newPrenotazione.getData());
        return sameDateBookings.stream().anyMatch(existing ->
                existing.getNomeCliente().equalsIgnoreCase(newPrenotazione.getNomeCliente()) &&
                        existing.getOrario().equals(newPrenotazione.getOrario()) &&
                        existing.getCoperti() == newPrenotazione.getCoperti() &&
                        existing.getId() != newPrenotazione.getId()
        );
    }

    public List<PrenotazioneBean> getPrenotazioniByData(LocalDate data) {
        return prenotazioneDao.getByData(data).stream()
                .map(entity -> mapperFactory.toBean(entity, PrenotazioneBean.class))
                .toList();
    }

    public PrenotazioneBean modificaPrenotazione(PrenotazioneBean bean) {
        Prenotazione entity = mapperFactory.toEntity(bean, PrenotazioneBean.class);

        if (existsDuplicate(entity)) {
            throw new PrenotationValidationException(
                    "Modifica non consentita: esiste già una prenotazione identica."
            );
        }

        prenotazioneDao.update(entity);

        if (bean.getEmail() != null && !bean.getEmail().isEmpty()) {
            inviaEmailConferma(bean);
        }

        return mapperFactory.toBean(entity, PrenotazioneBean.class);
    }

    public void eliminaPrenotazione(int id) {
        if (!prenotazioneExists(id)) {
            throw new IllegalArgumentException("La prenotazione con ID " + id + " non esiste.");
        }
        prenotazioneDao.delete(id);
    }

    private boolean prenotazioneExists(int id) {
        return prenotazioneDao.read(id).isPresent();
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