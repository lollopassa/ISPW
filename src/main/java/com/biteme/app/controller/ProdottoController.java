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

    // ProdottoController.java
    public ProdottoBean getProdottoByNome(String nome) {
        Prodotto prodotto = prodottoDao.findByNome(nome);
        if (prodotto == null) return null;

        ProdottoBean bean = new ProdottoBean();
        bean.setId(prodotto.getId());
        bean.setNome(prodotto.getNome());
        bean.setPrezzo(prodotto.getPrezzo());
        bean.setCategoria(prodotto.getCategoria());
        bean.setDisponibile(prodotto.isDisponibile());
        return bean;
    }

    public List<Prodotto> getProdotti() {
        return prodottoDao.getByDisponibilita(true);
    }

    public void eliminaProdotto(Integer id) {
        prodottoDao.delete(id);
    }

    public void modificaProdotto(Prodotto prodottoAggiornato) {
        prodottoDao.update(prodottoAggiornato); // Chiama il DAO per aggiornare il prodotto nel database
    }
}