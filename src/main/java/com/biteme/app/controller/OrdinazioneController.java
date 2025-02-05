package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.boundary.OrdinazioneBoundary;
import com.biteme.app.entity.Ordinazione;
import com.biteme.app.entity.StatoOrdine;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.util.Configuration;
import com.biteme.app.util.SceneLoader;

import java.util.ArrayList;
import java.util.List;

public class OrdinazioneController {

    private final OrdinazioneDao ordinazioneDao;
    private final OrdineController ordineController; // Aggiunto per chiamare salvaOrdine

    public OrdinazioneController() {
        this.ordinazioneDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdinazioneDao();
        this.ordineController = new OrdineController();
    }

    public void creaOrdine(OrdinazioneBean ordinazioneBean) {
        // Step 1: Crea oggetto Ordinazione e setta lo stato iniziale a StatoOrdine.NUOVO
        Ordinazione ordinazione = new Ordinazione(
                0, // ID assegnato automaticamente dal DAO
                ordinazioneBean.getNome(),
                ordinazioneBean.getNumeroClienti(),
                ordinazioneBean.getTipoOrdine(),
                ordinazioneBean.getInfoTavolo(),
                StatoOrdine.NUOVO, // Usa direttamente l'enum StatoOrdine
                ordinazioneBean.getOrarioCreazione()
        );

        // Salva l'ordinazione nel database
        ordinazioneDao.store(ordinazione);

        // Step 2: Crea OrdineBean correlato utilizzando l'ID dell'ordinazione
        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setId(ordinazione.getId()); // Assegna l'ID dell'ordinazione all'ordine
        ordineBean.setProdotti(new ArrayList<>()); // Lista prodotti inizialmente vuota
        ordineBean.setQuantita(new ArrayList<>()); // Quantità inizialmente vuota

        // Step 3: Salva l'ordine nel database, collegandolo all'ordinazione
        ordineController.salvaOrdine(ordineBean, ordinazione.getId()); // Ora utilizza 2 parametri
    }

    public List<Ordinazione> getOrdini() {
        // Recupera tutti gli ordini tramite il DAO
        return ordinazioneDao.getAll();
    }

    public void eliminaOrdine(int id) {
        if (ordinazioneDao.exists(id)) {
            ordinazioneDao.delete(id); // Cancella l'ordine dal database
        } else {
            throw new IllegalArgumentException("L'ordine con ID " + id + " non esiste.");
        }
    }

    // Metodo per ottenere l'ID dell'ordine selezionato
    public int getIdOrdineSelezionato() {
        OrdinazioneBean ordinazioneBean = OrdinazioneBoundary.getOrdineSelezionato();
        return ordinazioneBean.getId();
    }

    // Metodo per aggiornare lo stato dell'ordinazione
    public void aggiornaStatoOrdinazione(int ordineId, StatoOrdine nuovoStato) {
        // Usa OrdinazioneDao per aggiornare lo stato nel database
        ordinazioneDao.aggiornaStato(ordineId, nuovoStato);
    }

    // Metodo per cambiare scena e tornare alla schermata ordine
    public void cambiaASchermataOrdinazione() {
        SceneLoader.loadScene("/com/biteme/app/ordinazione.fxml", "Torna a Ordinazione");
    }

    public boolean isValidTime(String time) {
        // Verifica il formato HH:mm tramite una regex
        String timePattern = "([01]\\d|2[0-3]):([0-5]\\d)";
        if (!time.matches(timePattern)) {
            return false;
        }

        // Validazione aggiuntiva: verifica se è parsabile come LocalTime
        try {
            java.time.LocalTime.parse(time);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void eliminaOrdinazione(Integer id) {
        // Cancella un'ordinazione
        ordinazioneDao.delete(id);
    }
}