package com.biteme.app.controller;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.Ordine;
import com.biteme.app.entities.Prodotto;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.exception.OrdineException;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.persistence.OrdineDao;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.util.mapper.BeanEntityMapperFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class GestioneOrdiniController {

    private final ProdottoDao prodottoDao;
    private final OrdineDao ordineDao;
    private final OrdinazioneDao ordinazioneDao;
    private final BeanEntityMapperFactory mapper = BeanEntityMapperFactory.getInstance();

    public GestioneOrdiniController() {
        var factory = Configuration.getPersistenceProvider().getDaoFactory();
        this.prodottoDao = factory.getProdottoDao();
        this.ordineDao = factory.getOrdineDao();
        this.ordinazioneDao = factory.getOrdinazioneDao();
    }

    /* ---------- Ordine ---------- */

    public void salvaOrdine(OrdineBean bean, int idOrdine) throws OrdineException {
        try {
            bean.setId(idOrdine);
            bean.setPrezzi(completaPrezzi(bean));
            Ordine entity = mapper.toEntity(bean, OrdineBean.class);
            ordineDao.create(entity);
        } catch (Exception e) {
            throw new OrdineException("Errore nel salvataggio dell'ordine: " + e.getMessage(), e);
        }
    }

    public OrdineBean getOrdineById(int id) throws OrdineException {
        try {
            Ordine ord = ordineDao.getById(id);
            if (ord == null) {
                throw new OrdineException("Ordine con ID " + id + " non trovato.");
            }
            return mapper.toBean(ord, OrdineBean.class);
        } catch (OrdineException e) {
            throw e;
        } catch (Exception e) {
            throw new OrdineException("Errore caricando l'ordine con ID " + id + ": " + e.getMessage(), e);
        }
    }

    public List<ProdottoBean> getProdottiByCategoria(String categoria) {
        return prodottoDao.getByCategoria(categoria).stream()
                .map(p -> mapper.toBean(p, ProdottoBean.class))
                .toList();
    }

    /* ---------- Ordinazione ---------- */

    public void creaOrdinazione(OrdinazioneBean bean) throws OrdinazioneException {
        try {
            Ordinazione entity = mapper.toEntity(bean, OrdinazioneBean.class);
            int id = ordinazioneDao.create(entity);
            bean.setId(id);
        } catch (Exception e) {
            throw new OrdinazioneException("Errore nella creazione dell'ordinazione: " + e.getMessage(), e);
        }
    }

    public List<OrdinazioneBean> getOrdinazioni() {
        return ordinazioneDao.getAll().stream()
                .map(e -> mapper.toBean(e, OrdinazioneBean.class))
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

    public void aggiornaStatoOrdinazione(int ordineId, StatoOrdinazione nuovoStato) throws OrdinazioneException {
        try {
            ordinazioneDao.aggiornaStato(ordineId, nuovoStato);
        } catch (Exception e) {
            throw new OrdinazioneException("Errore nell'aggiornamento dello stato dell'ordinazione: " + e.getMessage(), e);
        }
    }

    /* ---------- Helper ---------- */

    private List<BigDecimal> completaPrezzi(OrdineBean bean) {
        List<String> prodottiUI = bean.getProdotti();
        List<BigDecimal> prezziUI = bean.getPrezzi();
        List<BigDecimal> out = new ArrayList<>(prodottiUI.size());
        for (int i = 0; i < prodottiUI.size(); i++) {
            if (prezziUI != null && prezziUI.size() > i) {
                out.add(prezziUI.get(i));
                continue;
            }
            Prodotto p = prodottoDao.findByNome(prodottiUI.get(i).trim());
            out.add(p != null ? p.getPrezzo() : BigDecimal.ZERO);
        }
        return out;
    }
}
