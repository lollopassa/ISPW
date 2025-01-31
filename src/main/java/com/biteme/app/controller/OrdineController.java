package com.biteme.app.controller;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.entity.Prodotto;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.util.Configuration;

import java.util.List;
import java.util.stream.Collectors;

public class OrdineController {

    private final ProdottoDao prodottoDao;

    public OrdineController() {
        // Ottenere ProdottoDao usando la configurazione del sistema
        this.prodottoDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getProdottoDao();
    }

    /**
     * Metodo per ottenere i prodotti filtrati per categoria
     *
     * @param categoria la categoria da filtrare
     * @return una lista di ProdottoBean
     */
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


}