package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.model.Ordinazione;
import com.biteme.app.model.StatoOrdine;
import com.biteme.app.model.TipoOrdine;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.util.Configuration;
import com.biteme.app.util.SceneLoader;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.view.OrdinazioneView;
import java.util.ArrayList;
import java.util.List;

public class OrdinazioneController {

    private final OrdinazioneDao ordinazioneDao;
    private final OrdineController ordineController; // Per salvare l'ordine collegato

    public OrdinazioneController() {
        this.ordinazioneDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdinazioneDao();
        this.ordineController = new OrdineController();
    }

    public void creaOrdine(OrdinazioneBean ordinazioneBean) {
        try {
            Ordinazione ordinazione = convertToModel(ordinazioneBean);
            ordinazione.setStatoOrdine(StatoOrdine.NUOVO);
            ordinazioneDao.store(ordinazione);
            ordinazioneBean.setId(ordinazione.getId());
            OrdineBean ordineBean = new OrdineBean();
            ordineBean.setId(ordinazione.getId());
            ordineBean.setProdotti(new ArrayList<>());
            ordineBean.setQuantita(new ArrayList<>());
            ordineController.salvaOrdine(ordineBean, ordinazione.getId());
        } catch (Exception e) {
            throw new OrdinazioneException("Errore nella creazione dell'ordinazione: " + e.getMessage(), e);
        }
    }

    public List<OrdinazioneBean> getOrdini() {
        List<Ordinazione> listaModel = ordinazioneDao.getAll();
        return listaModel.stream()
                .map(this::convertToBean)
                .toList();
    }

    public void eliminaOrdinazione(int id) {
        if (!ordinazioneDao.exists(id)) {
            throw new OrdinazioneException("L'ordinazione con ID " + id + " non esiste.");
        }
        try {
            ordinazioneDao.delete(id);
        } catch (Exception e) {
            throw new OrdinazioneException("Errore nell'eliminazione dell'ordinazione: " + e.getMessage(), e);
        }
    }

    // Metodo per ottenere l'ID dell'ordinazione selezionata dalla view
    public int getIdOrdineSelezionato() {
        OrdinazioneBean ordinazioneBean = OrdinazioneView.getOrdineSelezionato();
        if (ordinazioneBean == null) {
            throw new OrdinazioneException("Nessuna ordinazione selezionata.");
        }
        return ordinazioneBean.getId();
    }

    // Metodo per aggiornare lo stato dell'ordinazione
    public void aggiornaStatoOrdinazione(int ordineId, StatoOrdine nuovoStato) {
        try {
            ordinazioneDao.aggiornaStato(ordineId, nuovoStato);
        } catch (Exception e) {
            throw new OrdinazioneException("Errore nell'aggiornamento dello stato dell'ordinazione: " + e.getMessage(), e);
        }
    }

    public void cambiaASchermataOrdinazione() {
        SceneLoader.loadScene("/com/biteme/app/ordinazione.fxml", "Torna a Ordinazione");
    }

    public boolean isValidTime(String time) {
        String timePattern = "([01]\\d|2[0-3]):([0-5]\\d)";
        if (!time.matches(timePattern)) {
            return false;
        }
        try {
            java.time.LocalTime.parse(time);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // --- Private helper methods ---
    private Ordinazione convertToModel(OrdinazioneBean bean) {
        TipoOrdine tipoOrdine;
        if ("Al Tavolo".equals(bean.getTipoOrdine())) {
            tipoOrdine = TipoOrdine.AL_TAVOLO;
        } else if ("Asporto".equals(bean.getTipoOrdine())) {
            tipoOrdine = TipoOrdine.ASPORTO;
        } else {
            throw new IllegalArgumentException("Tipo Ordine non valido: " + bean.getTipoOrdine());
        }
        StatoOrdine statoOrdine = StatoOrdine.NUOVO;
        return new Ordinazione(
                bean.getId(),
                bean.getNome(),
                bean.getNumeroClienti(),
                tipoOrdine,
                bean.getInfoTavolo(),
                statoOrdine,
                bean.getOrarioCreazione()
        );
    }

    private OrdinazioneBean convertToBean(Ordinazione model) {
        OrdinazioneBean bean = new OrdinazioneBean();
        bean.setId(model.getId());
        bean.setNome(model.getNomeCliente());
        bean.setNumeroClienti(model.getNumeroClienti());
        bean.setTipoOrdine(model.getTipoOrdine().toString());
        bean.setInfoTavolo(model.getInfoTavolo());
        bean.setStatoOrdine(model.getStatoOrdine().toString());
        bean.setOrarioCreazione(model.getOrarioCreazione());
        return bean;
    }
}
