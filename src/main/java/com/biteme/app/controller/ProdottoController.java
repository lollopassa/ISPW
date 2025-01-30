package com.biteme.app.controller;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.entity.Prodotto;
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