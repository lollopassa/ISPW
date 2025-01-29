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
        // Crea un nuovo oggetto Prodotto utilizzando i dati del bean
        Prodotto prodotto = new Prodotto(
                magazzinoBean.getId() != null ? magazzinoBean.getId() : 0, // Imposta l'ID a 0 se non specificato
                magazzinoBean.getNomeProdotto(),
                magazzinoBean.getQuantita(),
                magazzinoBean.getPrezzo(),
                magazzinoBean.getCategoria(),
                magazzinoBean.getDataScadenza(),
                magazzinoBean.getDisponibile() != null ? magazzinoBean.getDisponibile() : true // Disponibile di default
        );

        // Salva il prodotto utilizzando il DAO
        prodottoDao.store(prodotto);
    }

    public List<Prodotto> getProdotti() {
        return prodottoDao.getByDisponibilita(true); // Ottieni solo prodotti disponibili
    }

    public void eliminaProdotto(Integer id) {
        prodottoDao.delete(id); // Elimina un prodotto tramite ID
    }
}