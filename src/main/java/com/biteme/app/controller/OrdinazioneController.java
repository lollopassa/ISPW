package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.view.OrdinazioneView;
import com.biteme.app.model.Ordinazione;
import com.biteme.app.model.StatoOrdine;
import com.biteme.app.model.TipoOrdine;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.util.Configuration;
import com.biteme.app.util.SceneLoader;
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
        // Converte il bean in model
        Ordinazione ordinazione = convertToModel(ordinazioneBean);
        // Imposta lo stato iniziale (ad es. NUOVO)
        ordinazione.setStatoOrdine(StatoOrdine.NUOVO);
        // Salva l'ordinazione nel database
        ordinazioneDao.store(ordinazione);

        // Aggiorna l'ID generato nel bean
        ordinazioneBean.setId(ordinazione.getId());

        // Crea l'OrdineBean collegato e lo salva
        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setId(ordinazione.getId());
        ordineBean.setProdotti(new ArrayList<>());
        ordineBean.setQuantita(new ArrayList<>());
        ordineController.salvaOrdine(ordineBean, ordinazione.getId());
    }

    /**
     * Restituisce tutti gli ordini come bean.
     */
    public List<OrdinazioneBean> getOrdini() {
        List<Ordinazione> listaModel = ordinazioneDao.getAll();
        return listaModel.stream()
                .map(this::convertToBean)
                .toList();
    }

    public void eliminaOrdine(int id) {
        if (ordinazioneDao.exists(id)) {
            ordinazioneDao.delete(id);
        } else {
            throw new IllegalArgumentException("L'ordine con ID " + id + " non esiste.");
        }
    }

    // Metodo per ottenere l'ID dell'ordine selezionato dalla view
    public int getIdOrdineSelezionato() {
        OrdinazioneBean ordinazioneBean = OrdinazioneView.getOrdineSelezionato();
        return ordinazioneBean.getId();
    }

    // Metodo per aggiornare lo stato dell'ordinazione (se necessario)
    public void aggiornaStatoOrdinazione(int ordineId, StatoOrdine nuovoStato) {
        ordinazioneDao.aggiornaStato(ordineId, nuovoStato);
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

    public void eliminaOrdinazione(Integer id) {
        ordinazioneDao.delete(id);
    }

    private Ordinazione convertToModel(OrdinazioneBean bean) {
        // Converte la stringa presente nel bean in un valore enum del model
        TipoOrdine tipoOrdine;
        if ("Al Tavolo".equals(bean.getTipoOrdine())) {
            tipoOrdine = TipoOrdine.AL_TAVOLO;
        } else if ("Asporto".equals(bean.getTipoOrdine())) {
            tipoOrdine = TipoOrdine.ASPORTO;
        } else {
            throw new IllegalArgumentException("Tipo Ordine non valido: " + bean.getTipoOrdine());
        }
        // Per lo stato, se non è stato impostato dalla view si usa NUOVO
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
        // Converte l'enum in una stringa per la view
        bean.setTipoOrdine(model.getTipoOrdine().toString());
        bean.setInfoTavolo(model.getInfoTavolo());
        bean.setStatoOrdine(model.getStatoOrdine().toString());
        bean.setOrarioCreazione(model.getOrarioCreazione());
        return bean;
    }
}
