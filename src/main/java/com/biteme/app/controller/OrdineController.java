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
    private final OrdineDao ordineDao;
    public OrdineController() {
        this.prodottoDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getProdottoDao();
        this.ordineDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdineDao();
    }


    private OrdineBean preparaOrdineBean(List<String> prodotti, List<Integer> quantita) {
        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setProdotti(prodotti);
        ordineBean.setQuantita(quantita);

        List<BigDecimal> prezzi = new ArrayList<>();
        for (String nomeProdotto : prodotti) {
            Prodotto prodotto = prodottoDao.findByNome(nomeProdotto.trim()); // Aggiungi .trim()
            if (prodotto == null) {
                throw new IllegalStateException("Prodotto '" + nomeProdotto + "' non trovato nel database");
            }
            prezzi.add(prodotto.getPrezzo());
        }
        ordineBean.setPrezzi(prezzi);

        return ordineBean;
    }



    public void salvaOrdine(OrdineBean ordineBean, int id) throws OrdineException {
        try {
            if (ordineBean.getPrezzi() == null || ordineBean.getPrezzi().isEmpty()) {
                OrdineBean tmp = preparaOrdineBean(ordineBean.getProdotti(), ordineBean.getQuantita());
                ordineBean.setPrezzi(tmp.getPrezzi());
            }

            if (!ordineBean.getProdotti().isEmpty()
                    && (ordineBean.getPrezzi() == null || ordineBean.getPrezzi().isEmpty())) {
                throw new OrdineException("Prezzi non inizializzati correttamente");
            }

            Ordine nuovoOrdine = new Ordine(
                    id,
                    ordineBean.getProdotti(),
                    ordineBean.getQuantita(),
                    ordineBean.getPrezzi()
            );
            ordineDao.store(nuovoOrdine);

        } catch (OrdineException oe) {
            throw oe;
        } catch (Exception e) {
            throw new OrdineException("Errore nel salvataggio dell'ordine: " + e.getMessage(), e);
        }
    }


    public List<ProdottoBean> getProdottiByCategoria(String categoria) {
        List<Prodotto> prodotti = prodottoDao.getByCategoria(categoria);
        return prodotti.stream()
                .map(prodotto -> {
                    ProdottoBean prodottoBean = new ProdottoBean();
                    prodottoBean.setId(prodotto.getId());
                    prodottoBean.setNome(prodotto.getNome());
                    prodottoBean.setPrezzo(prodotto.getPrezzo());
                    prodottoBean.setCategoria(prodotto.getCategoria().name());
                    prodottoBean.setDisponibile(prodotto.isDisponibile());
                    return prodottoBean;
                })
                .toList();
    }

    public OrdineBean getOrdineById(int id) throws OrdineException {
        return load(id);
    }

    public OrdineBean load(int idOrdine) throws OrdineException {
        try {
            Ordine ordine = ordineDao.getById(idOrdine);

            OrdineBean bean = new OrdineBean();
            bean.setId(idOrdine);

            if (ordine != null) {
                bean.setProdotti(ordine.getProdotti());
                bean.setQuantita(ordine.getQuantita());
                bean.setPrezzi(ordine.getPrezzi());
            } else {
                bean.setProdotti(new ArrayList<>());
                bean.setQuantita(new ArrayList<>());
                bean.setPrezzi(new ArrayList<>());
            }

            return bean;
        } catch (Exception e) {
            throw new OrdineException(
                    "Errore caricando l'ordine con ID " + idOrdine + ": " + e.getMessage(), e
            );
        }
    }
}