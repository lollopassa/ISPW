package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.entities.TipoOrdinazione;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.util.SceneLoader;
import com.biteme.app.boundary.OrdinazioneBoundary;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class OrdinazioneController {

    private final OrdinazioneDao ordinazioneDao;
    private final OrdineController ordineController;
    public OrdinazioneController() {
        this.ordinazioneDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdinazioneDao();
        this.ordineController = new OrdineController();
    }


    public OrdinazioneBean processOrdineCreation(String nome, String tipoOrdine, String orario, String coperti, String tavolo) throws OrdinazioneException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new OrdinazioneException("Il campo Nome Cliente deve essere compilato.");
        }
        if (tipoOrdine == null || tipoOrdine.trim().isEmpty()) {
            throw new OrdinazioneException("Seleziona un tipo di ordine: 'Al Tavolo' o 'Asporto'.");
        }

        if ("Al Tavolo".equals(tipoOrdine)) {
            validateAlTavolo(coperti, tavolo);
                        orario = LocalTime.now().toString().substring(0, 5);
        } else if ("Asporto".equals(tipoOrdine)) {
            validateAsporto(orario);
                        coperti = "";
            tavolo = "";
        } else {
            throw new OrdinazioneException("Tipo di ordine non valido: " + tipoOrdine);
        }

        OrdinazioneBean bean = new OrdinazioneBean();
        bean.setNome(nome);
        bean.setTipoOrdine(tipoOrdine);
        bean.setOrarioCreazione(orario);
        bean.setNumeroClienti(coperti);
        bean.setInfoTavolo(tavolo);
        return bean;
    }

    private void validateAlTavolo(String coperti, String tavolo) throws OrdinazioneException {
        if (coperti == null || coperti.trim().isEmpty()) {
            throw new OrdinazioneException("Il numero di coperti è obbligatorio per gli ordini 'Al Tavolo'.");
        }
        if (!coperti.matches("\\d+")) {
            throw new OrdinazioneException("Il campo 'Numero di Coperti' deve contenere solo numeri interi.");
        }
        if (tavolo == null || tavolo.trim().isEmpty()) {
            throw new OrdinazioneException("Il numero del tavolo è obbligatorio per gli ordini 'Al Tavolo'.");
        }
    }

    private void validateAsporto(String orario) throws OrdinazioneException {
        if (orario == null || orario.trim().isEmpty()) {
            throw new OrdinazioneException("Il campo Orario deve essere compilato per Asporto.");
        }
        if (!isValidTime(orario)) {
            throw new OrdinazioneException("Il campo 'Orario' deve essere nel formato HH:mm (es. '12:20').");
        }
    }



    public void creaOrdine(OrdinazioneBean ordinazioneBean) throws OrdinazioneException {
        try {
            Ordinazione ordinazione = convertToModel(ordinazioneBean);
            ordinazione.setStatoOrdine(StatoOrdinazione.NUOVO);
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

    public void eliminaOrdinazione(int id) throws OrdinazioneException {
        if (!ordinazioneDao.exists(id)) {
            throw new OrdinazioneException("L'ordinazione con ID " + id + " non esiste.");
        }
        try {
            ordinazioneDao.delete(id);
        } catch (Exception e) {
            throw new OrdinazioneException("Errore nell'eliminazione dell'ordinazione: " + e.getMessage(), e);
        }
    }

    public int getIdOrdineSelezionato() throws OrdinazioneException {
        OrdinazioneBean ordinazioneBean = OrdinazioneBoundary.getOrdineSelezionato();
        if (ordinazioneBean == null) {
            throw new OrdinazioneException("Nessuna ordinazione selezionata.");
        }
        return ordinazioneBean.getId();
    }

    public void aggiornaStatoOrdinazione(int ordineId, StatoOrdinazione nuovoStato) throws OrdinazioneException {
        try {
            ordinazioneDao.aggiornaStato(ordineId, nuovoStato);
        } catch (Exception e) {
            throw new OrdinazioneException("Errore nell'aggiornamento dello stato dell'ordinazione: " + e.getMessage(), e);
        }
    }

    public void cambiaASchermataOrdinazione() {
        SceneLoader.getInstance().loadScene("/com/biteme/app/ordinazione.fxml", "Torna a Ordinazione");
    }

    public boolean isValidTime(String time) {
        String timePattern = "([01]\\d|2[0-3]):([0-5]\\d)";
        if (!time.matches(timePattern)) {
            return false;
        }
        try {
            LocalTime.parse(time);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

        private Ordinazione convertToModel(OrdinazioneBean bean) {
        TipoOrdinazione tipoOrdine;
        if ("Al Tavolo".equals(bean.getTipoOrdine())) {
            tipoOrdine = TipoOrdinazione.AL_TAVOLO;
        } else if ("Asporto".equals(bean.getTipoOrdine())) {
            tipoOrdine = TipoOrdinazione.ASPORTO;
        } else {
            throw new IllegalArgumentException("Tipo Ordine non valido: " + bean.getTipoOrdine());
        }
        StatoOrdinazione statoOrdine = StatoOrdinazione.NUOVO;
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