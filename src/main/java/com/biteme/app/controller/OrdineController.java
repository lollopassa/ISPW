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

    /* ---------- creazione ---------- */

    public void salvaOrdine(OrdineBean bean, int idOrdine) throws OrdineException {
        try {
            bean.setId(idOrdine);

            if (!bean.isPrezziPresenti()) {
                bean.setPrezzi(prezziPer(bean.getProdotti()));
            }

            Ordine entity = mapperFactory.toEntity(bean, OrdineBean.class);
            ordineDao.create(entity);

        } catch (Exception e) {
            throw new OrdineException("Errore nel salvataggio dell'ordine: " + e.getMessage(), e);
        }
    }


    /* ---------- lettura ---------- */

    public OrdineBean getOrdineById(int id) throws OrdineException {
        try {
            Ordine ord = ordineDao.getById(id);
            return (ord != null)
                    ? mapperFactory.toBean(ord, OrdineBean.class)
                    : new OrdineBean();
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

    private List<BigDecimal> prezziPer(List<String> nomiProdotti) {
        List<BigDecimal> list = new ArrayList<>();
        for (String nome : nomiProdotti) {
            Prodotto p = prodottoDao.findByNome(nome.trim());
            list.add( (p != null) ? p.getPrezzo() : BigDecimal.ZERO );
        }
        return list;
    }

}
