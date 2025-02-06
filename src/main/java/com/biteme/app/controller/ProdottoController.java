package com.biteme.app.controller;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.model.Prodotto;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.util.Configuration;

import java.util.List;

public class ProdottoController {

    private final ProdottoDao prodottoDao;

    public ProdottoController() {
        this.prodottoDao = Configuration.getPersistenceProvider().getDaoFactory().getProdottoDao();
    }

    /**
     * Aggiunge un prodotto convertendo il bean in entità.
     */
    public void aggiungiProdotto(ProdottoBean prodottoBean) {
        Prodotto prodotto = new Prodotto(
                prodottoBean.getId() != null ? prodottoBean.getId() : 0,
                prodottoBean.getNome(),
                prodottoBean.getPrezzo(),
                prodottoBean.getCategoria(),
                prodottoBean.getDisponibile() != null && prodottoBean.getDisponibile()
        );
        prodottoDao.store(prodotto);
    }

    /**
     * Restituisce un ProdottoBean a partire dal nome del prodotto.
     */
    public ProdottoBean getProdottoByNome(String nome) {
        Prodotto prodotto = prodottoDao.findByNome(nome);
        if (prodotto == null) {
            return null;
        }
        return mapProdottoToBean(prodotto);
    }

    /**
     * Restituisce la lista dei prodotti (solo quelli disponibili) come bean.
     */
    public List<ProdottoBean> getProdotti() {
        List<Prodotto> prodotti = prodottoDao.getByDisponibilita(true);
        return prodotti.stream()
                .map(this::mapProdottoToBean)
                .toList();
    }

    /**
     * Elimina un prodotto dato il suo ID.
     */
    public void eliminaProdotto(Integer id) {
        prodottoDao.delete(id);
    }

    /**
     * Aggiorna un prodotto convertendo il bean in entità.
     */
    public void modificaProdotto(ProdottoBean prodottoBean) {
        Prodotto prodottoAggiornato = new Prodotto(
                prodottoBean.getId(),
                prodottoBean.getNome(),
                prodottoBean.getPrezzo(),
                prodottoBean.getCategoria(),
                prodottoBean.getDisponibile() != null && prodottoBean.getDisponibile()
        );
        prodottoDao.update(prodottoAggiornato);
    }

    /**
     * Metodo di supporto per convertire un'entità Prodotto in un bean.
     */
    private ProdottoBean mapProdottoToBean(Prodotto prodotto) {
        ProdottoBean bean = new ProdottoBean();
        bean.setId(prodotto.getId());
        bean.setNome(prodotto.getNome());
        bean.setPrezzo(prodotto.getPrezzo());
        bean.setCategoria(prodotto.getCategoria());
        bean.setDisponibile(prodotto.isDisponibile());
        return bean;
    }
}
