package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.exception.OrdineException;
import com.biteme.app.entities.Ordine;
import com.biteme.app.entities.Prodotto;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.persistence.OrdineDao;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.persistence.Configuration;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrdineController {

    private final ProdottoDao prodottoDao;
    private final OrdineDao ordineDao;
    private VBox riepilogoContenuto;
    public OrdineController() {
        this.prodottoDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getProdottoDao();
        this.ordineDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdineDao();
    }

    public void setRiepilogoContenuto(VBox riepilogoContenuto) {
        this.riepilogoContenuto = riepilogoContenuto;
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

    public void salvaOrdineEStato(int ordineId, String statoStr) throws OrdineException {
        try {
            StatoOrdinazione stato = convertStringToStatoOrdine(statoStr);
            List<String> prodotti = recuperaProdottiDalRiepilogo();

            validateProdottiNonVuoti(prodotti);

            List<Integer> quantita = recuperaQuantita(prodotti);
            OrdineBean ordineBean = preparaEValidaOrdine(prodotti, quantita);

            salvaOrdine(ordineBean, ordineId);
            aggiornaStatoOrdinazioneSafely(ordineId, stato);

        } catch (IllegalStateException e) {
            throw new OrdineException("Errore di stato interno: " + e.getMessage(), e);
        }
    }


    private void aggiornaStatoOrdinazioneSafely(int ordineId, StatoOrdinazione stato) throws OrdineException {
        try {
            OrdinazioneController ordinazioneController = new OrdinazioneController();
            ordinazioneController.aggiornaStatoOrdinazione(ordineId, stato);
        } catch (OrdinazioneException e) {
            throw new OrdineException("Errore durante l'aggiornamento dello stato: " + e.getMessage(), e);
        }
    }

    private void validateProdottiNonVuoti(List<String> prodotti) throws OrdineException {
        if(prodotti.isEmpty()) {
            throw new OrdineException("Impossibile creare un ordine senza prodotti");
        }
    }

    private List<Integer> recuperaQuantita(List<String> prodotti) {
        List<Integer> quantita = new ArrayList<>();
        for (String prodotto : prodotti) {
            quantita.add(recuperaQuantitaDalRiepilogo(prodotto));
        }
        return quantita;
    }

    private OrdineBean preparaEValidaOrdine(List<String> prodotti, List<Integer> quantita) throws OrdineException {
        OrdineBean ordineBean = preparaOrdineBean(prodotti, quantita);

        if(ordineBean.getPrezzi() == null || ordineBean.getPrezzi().size() != prodotti.size()) {
            throw new OrdineException("Errore di inizializzazione prezzi");
        }

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



    public List<String> recuperaProdottiDalRiepilogo() {
        List<String> prodotti = new ArrayList<>();
        for (Node nodo : riepilogoContenuto.getChildren()) {
            if (nodo instanceof HBox hbox && hbox.getChildren().get(0) instanceof Label nomeEQuantitaLabel) {
                String testo = nomeEQuantitaLabel.getText();
                String[] parti = testo.split(" x ");
                if (parti.length > 1) {
                    String nomeProdotto = parti[0].trim();
                    prodotti.add(nomeProdotto);
                }
            }
        }
        return prodotti;
    }

    public int recuperaQuantitaDalRiepilogo(String nomeProdotto) {
        for (Node nodo : riepilogoContenuto.getChildren()) {
            if (nodo instanceof HBox hbox && hbox.getChildren().get(0) instanceof Label nomeEQuantitaLabel) {
                String testo = nomeEQuantitaLabel.getText();
                if (testo.startsWith(nomeProdotto + " x")) {
                    try {
                        String[] parti = testo.split(" x ");
                        return Integer.parseInt(parti[1].trim());
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                        return 0;
                    }
                }
            }
        }
        return 0;
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




    public List<ProdottoBean> getTuttiProdotti() {
        List<Prodotto> prodotti = prodottoDao.getAll();
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

    private StatoOrdinazione convertStringToStatoOrdine(String statoStr) {
        if (statoStr == null) {
            throw new IllegalArgumentException("Stato ordine non puÃ² essere null");
        }
        switch (statoStr.toUpperCase()) {
            case "IN_CORSO":
                return StatoOrdinazione.IN_CORSO;
            case "COMPLETATO":
                return StatoOrdinazione.COMPLETATO;
            default:
                throw new IllegalArgumentException("Stato ordine non valido: " + statoStr);
        }
    }
}