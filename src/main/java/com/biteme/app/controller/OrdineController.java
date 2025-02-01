package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.entity.Ordine;
import com.biteme.app.entity.Prodotto;
import com.biteme.app.persistence.OrdineDao;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.util.Configuration;
import java.util.List;
import java.util.stream.Collectors;

public class OrdineController {

    private final ProdottoDao prodottoDao;

    private final OrdineDao ordineDao;

    public OrdineController() {
        // Ottenere ProdottoDao usando la configurazione del sistema
        this.prodottoDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getProdottoDao();

        this.ordineDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdineDao();
    }

    public List<ProdottoBean> getProdottiByCategoria(String categoria) {
        // Recupera i prodotti dalla DAO in base alla categoria
        List<Prodotto> prodotti = prodottoDao.getByCategoria(categoria);

        // Converti la lista di Prodotto a ProdottoBean per il livello di visualizzazione
        return prodotti.stream()
                .map(prodotto -> {
                    ProdottoBean prodottoBean = new ProdottoBean();
                    prodottoBean.setId(prodotto.getId());
                    prodottoBean.setNome(prodotto.getNome());
                    prodottoBean.setPrezzo(prodotto.getPrezzo());
                    prodottoBean.setCategoria(prodotto.getCategoria());
                    prodottoBean.setDisponibile(prodotto.isDisponibile());
                    return prodottoBean;
                })
                .collect(Collectors.toList());
    }

    public void salvaOrdine(OrdineBean ordineBean, int id) {
        // Converti OrdineBean in un oggetto Ordine
        Ordine nuovoOrdine = new Ordine(
                id,
                ordineBean.getProdotti(), // Lista dei prodotti
                ordineBean.getQuantita()  // Lista delle quantità
        );

        // Salva l'oggetto Ordine nel database
        ordineDao.store(nuovoOrdine);
    }
    public OrdineBean load(int idOrdine) {
        // Recupera l'ordine dalla DAO utilizzando l'ID fornito
        Ordine ordine = ordineDao.getById(idOrdine);

        if (ordine == null) {
            throw new IllegalArgumentException("L'ordine con ID " + idOrdine + " non esiste.");
        }

        // Crea un oggetto OrdineBean per il livello di visualizzazione
        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setId(ordine.getId());

        // Supponendo che ordine.getProdotti() restituisca una lista di nomi dei prodotti
        ordineBean.setProdotti(ordine.getProdotti());

        // Supponendo che ordine.getQuantita() restituisca una lista di quantità per i prodotti
        ordineBean.setQuantita(ordine.getQuantita());

        return ordineBean;
    }

    public List<ProdottoBean> getTuttiProdotti() {
        // Recupera tutti i prodotti da ProdottoDao
        List<Prodotto> prodotti = prodottoDao.getAll();

        // Converti la lista di Prodotto a ProdottoBean per il livello di visualizzazione
        return prodotti.stream()
                .map(prodotto -> {
                    ProdottoBean prodottoBean = new ProdottoBean();
                    prodottoBean.setId(prodotto.getId());
                    prodottoBean.setNome(prodotto.getNome());
                    prodottoBean.setPrezzo(prodotto.getPrezzo());
                    prodottoBean.setCategoria(prodotto.getCategoria());
                    prodottoBean.setDisponibile(prodotto.isDisponibile());
                    return prodottoBean;
                })
                .collect(Collectors.toList());
    }

}