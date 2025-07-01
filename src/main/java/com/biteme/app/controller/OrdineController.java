package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.entities.Ordine;
import com.biteme.app.entities.Prodotto;
import com.biteme.app.exception.OrdineException;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.persistence.OrdineDao;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.util.mapper.BeanEntityMapperFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrdineController {

    private final ProdottoDao prodottoDao;
    private final OrdineDao   ordineDao;
    private final BeanEntityMapperFactory mapperFactory = BeanEntityMapperFactory.getInstance();

    public OrdineController() {
        this.prodottoDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getProdottoDao();
        this.ordineDao   = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdineDao();
    }

    /* ---------- creazione / update ---------- */
    public void salvaOrdine(OrdineBean bean, int idOrdine) throws OrdineException {
        try {
            bean.setId(idOrdine);

            /* ① calcoliamo/integriamo SEMPRE la lista prezzi */
            bean.setPrezzi( complettaPrezzi(bean) );

            Ordine entity = mapperFactory.toEntity(bean, OrdineBean.class);
            ordineDao.create(entity);

        } catch (Exception e) {
            throw new OrdineException(
                    "Errore nel salvataggio dell'ordine: " + e.getMessage(), e);
        }
    }

    /* ---------- lettura ---------- */
    public OrdineBean getOrdineById(int id) throws OrdineException {
        try {
            Ordine ord = ordineDao.getById(id);
            if (ord == null) {                       // ← ❶
                throw new OrdineException(
                        "Ordine con ID " + id + " non trovato.");
            }
            return mapperFactory.toBean(ord, OrdineBean.class);
        } catch (OrdineException e) {                // già lanciata sopra
            throw e;
        } catch (Exception e) {
            throw new OrdineException(
                    "Errore caricando l'ordine con ID " + id + ": " + e.getMessage(), e);
        }
    }


    public List<ProdottoBean> getProdottiByCategoria(String categoria) {
        return prodottoDao.getByCategoria(categoria).stream()
                .map(p -> mapperFactory.toBean(p, ProdottoBean.class))
                .toList();
    }

    /* ---------- helper ---------- */

    /** Restituisce una lista di prezzi lunga quanto {@code bean.getProdotti()},
     *  completando/normalizzando quella eventualmente già presente.                     */
    private List<BigDecimal> complettaPrezzi(OrdineBean bean) {

        List<String>      prodottiUI = bean.getProdotti();
        List<BigDecimal>  prezziUI   = bean.getPrezzi();        // può essere null

        List<BigDecimal> out = new ArrayList<>(prodottiUI.size());

        for (int i = 0; i < prodottiUI.size(); i++) {

            /* a) se la UI ha già fornito il prezzo in quella posizione lo ri-uso */
            if (prezziUI != null && prezziUI.size() > i) {
                out.add(prezziUI.get(i));
                continue;
            }

            /* b) provo a leggerlo dal magazzino */
            Prodotto p = prodottoDao.findByNome(prodottiUI.get(i).trim());
            if (p != null) {
                out.add(p.getPrezzo());
            } else {
                /* c) articolo “extra” → fallback 0 €           */
                out.add(BigDecimal.ZERO);
            }
        }
        return out;
    }
}
