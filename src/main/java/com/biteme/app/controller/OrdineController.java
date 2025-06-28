package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.exception.OrdineException;
import com.biteme.app.entities.Ordine;
import com.biteme.app.entities.Prodotto;
import com.biteme.app.persistence.OrdineDao;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.persistence.Configuration;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrdineController {

    private final ProdottoDao prodottoDao;
    private final OrdineDao   ordineDao;

    public OrdineController() {
        this.prodottoDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getProdottoDao();
        this.ordineDao   = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdineDao();
    }

    private OrdineBean preparaOrdineBean(List<String> prodotti, List<Integer> quantita) {
        OrdineBean ob = new OrdineBean();
        ob.setProdotti(prodotti);
        ob.setQuantita(quantita);

        List<BigDecimal> prezzi = new ArrayList<>();
        for (String nome : prodotti) {
            Prodotto p = prodottoDao.findByNome(nome.trim());
            if (p == null)
                throw new IllegalStateException("Prodotto '" + nome + "' non trovato nel database");
            prezzi.add(p.getPrezzo());
        }
        ob.setPrezzi(prezzi);
        return ob;
    }

    public void salvaOrdine(OrdineBean bean, int id) throws OrdineException {
        try {
            if (!bean.isPrezziPresenti()) {
                OrdineBean tmp = preparaOrdineBean(bean.getProdotti(), bean.getQuantita());
                bean.setPrezzi(tmp.getPrezzi());
            }
            Ordine entity = bean.toEntity(id);
            ordineDao.create(entity);
        } catch (Exception e) {
            throw new OrdineException("Errore nel salvataggio dell'ordine: " + e.getMessage(), e);
        }
    }


    public List<ProdottoBean> getProdottiByCategoria(String categoria) {
        return prodottoDao.getByCategoria(categoria)
                .stream()
                .map(ProdottoBean::fromEntity)
                .toList();
    }

    public OrdineBean getOrdineById(int id) throws OrdineException { return load(id); }

    public OrdineBean load(int idOrdine) throws OrdineException {
        try {
            Ordine ord = ordineDao.getById(idOrdine);
            return (ord != null) ? OrdineBean.fromEntity(ord) : new OrdineBean();
        } catch (Exception e) {
            throw new OrdineException("Errore caricando l'ordine con ID " + idOrdine + ": " + e.getMessage(), e);
        }
    }
}
