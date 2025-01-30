package com.biteme.app.controller;

import com.biteme.app.bean.MagazzinoBean;
import com.biteme.app.entity.Prodotto;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.util.Configuration;

import java.util.List;

public class MagazzinoController {

    private final ProdottoDao prodottoDao;

    public MagazzinoController() {
        this.prodottoDao = Configuration.getPersistenceProvider().getDaoFactory().getProdottoDao();
    }

    public void aggiungiProdotto(MagazzinoBean magazzinoBean) {
        Prodotto prodotto = new Prodotto(
                magazzinoBean.getId() != null ? magazzinoBean.getId() : 0,
                magazzinoBean.getNome(),
                magazzinoBean.getPrezzo(),
                magazzinoBean.getCategoria(),
                magazzinoBean.getDisponibile() != null && magazzinoBean.getDisponibile()
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