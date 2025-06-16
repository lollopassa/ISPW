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

public class OrdineController {                   // logica applicativa sugli ordini

    private final ProdottoDao prodottoDao;        // DAO prodotti
    private final OrdineDao   ordineDao;          // DAO ordini

    public OrdineController() {                   // costruttore: ottiene DAO da factory
        this.prodottoDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getProdottoDao();                // implementazione ProdottoDao
        this.ordineDao   = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdineDao();                  // implementazione OrdineDao
    }

    // Completa un OrdineBean calcolando i prezzi dal catalogo
    private OrdineBean preparaOrdineBean(List<String> prodotti, List<Integer> quantita) {
        OrdineBean ob = new OrdineBean();         // nuovo bean vuoto
        ob.setProdotti(prodotti);                 // imposta lista prodotti
        ob.setQuantita(quantita);                 // imposta lista quantità

        List<BigDecimal> prezzi = new ArrayList<>(); // lista prezzi da riempire
        for (String nome : prodotti) {            // loop su ogni prodotto
            Prodotto p = prodottoDao.findByNome(nome.trim()); // lookup DB
            if (p == null)
                throw new IllegalStateException("Prodotto '" + nome + "' non trovato nel database");
            prezzi.add(p.getPrezzo());            // aggiunge prezzo
        }
        ob.setPrezzi(prezzi);                     // imposta prezzi
        return ob;                                // ritorna bean completo
    }

    // Salva (o aggiorna) un ordine associato a un ID ordinazione
    public void salvaOrdine(OrdineBean bean, int id) throws OrdineException {
        try {
            if (!bean.isPrezziPresenti()) {       // se mancano i prezzi calcolali da catalogo
                OrdineBean tmp = preparaOrdineBean(bean.getProdotti(), bean.getQuantita());
                bean.setPrezzi(tmp.getPrezzi());
            }
            Ordine entity = bean.toEntity(id);    // DTO → entity
            ordineDao.store(entity);              // persiste tramite DAO
        } catch (Exception e) {                   // qualunque errore
            throw new OrdineException("Errore nel salvataggio dell'ordine: " + e.getMessage(), e);
        }
    }

    // Recupera prodotti filtrati per categoria
    public List<ProdottoBean> getProdottiByCategoria(String categoria) {
        return prodottoDao.getByCategoria(categoria)
                .stream()
                .map(ProdottoBean::fromEntity)    // entity → DTO
                .toList();
    }

    // Facade rapido che delega a load()
    public OrdineBean getOrdineById(int id) throws OrdineException { return load(id); }

    // Carica un ordine dal DAO e lo converte in Bean
    public OrdineBean load(int idOrdine) throws OrdineException {
        try {
            Ordine ord = ordineDao.getById(idOrdine); // fetch entity
            return (ord != null) ? OrdineBean.fromEntity(ord) : new OrdineBean();
        } catch (Exception e) {
            throw new OrdineException("Errore caricando l'ordine con ID " + idOrdine + ": " + e.getMessage(), e);
        }
    }
}
