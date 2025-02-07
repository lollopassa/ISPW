package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.model.Ordine;
import com.biteme.app.model.Prodotto;
import com.biteme.app.model.StatoOrdine;
import com.biteme.app.persistence.OrdineDao;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.util.Configuration;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class OrdineController {

    private final ProdottoDao prodottoDao;
    private final OrdineDao ordineDao;
    private VBox riepilogoContenuto; // Impostato dalla view

    public OrdineController() {
        this.prodottoDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getProdottoDao();
        this.ordineDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdineDao();
    }

    // Imposta il VBox passato dalla view
    public void setRiepilogoContenuto(VBox riepilogoContenuto) {
        this.riepilogoContenuto = riepilogoContenuto;
    }

    private OrdineBean preparaOrdineBean(List<String> prodotti, List<Integer> quantita) {
        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setProdotti(prodotti);
        ordineBean.setQuantita(quantita);
        return ordineBean;
    }

    /**
     * Metodo modificato: la view passa una stringa che viene convertita internamente in un valore
     * di StatoOrdine (model). In questo modo la view non fa riferimento diretto al model.
     */
    public void salvaOrdineEStato(int ordineId, String statoStr) {
        StatoOrdine stato = convertStringToStatoOrdine(statoStr);
        List<String> prodotti = recuperaProdottiDalRiepilogo();
        List<Integer> quantita = new ArrayList<>();
        for (String prodotto : prodotti) {
            quantita.add(recuperaQuantitaDalRiepilogo(prodotto));
        }
        OrdineBean ordineBean = preparaOrdineBean(prodotti, quantita);
        salvaOrdine(ordineBean, ordineId);
        // Aggiorna lo stato dell'ordinazione tramite il controller di ordinazione
        OrdinazioneController ordinazioneController = new OrdinazioneController();
        ordinazioneController.aggiornaStatoOrdinazione(ordineId, stato);
    }

    // Metodo helper per convertire la stringa in StatoOrdine
    private StatoOrdine convertStringToStatoOrdine(String statoStr) {
        if (statoStr == null) {
            throw new IllegalArgumentException("Stato ordine non può essere null");
        }
        switch (statoStr.toUpperCase()) {
            case "IN_CORSO":
                return StatoOrdine.IN_CORSO;
            case "COMPLETATO":
                return StatoOrdine.COMPLETATO;
            default:
                throw new IllegalArgumentException("Stato ordine non valido: " + statoStr);
        }
    }

    public void salvaOrdine(OrdineBean ordineBean, int id) {
        Ordine nuovoOrdine = new Ordine(
                id,
                ordineBean.getProdotti(),
                ordineBean.getQuantita()
        );
        ordineDao.store(nuovoOrdine);
    }

    // Recupero dei prodotti dal riepilogo (il VBox viene gestito internamente)
    private List<String> recuperaProdottiDalRiepilogo() {
        List<String> prodotti = new ArrayList<>();
        for (Node nodo : riepilogoContenuto.getChildren()) {
            if (nodo instanceof HBox hbox && hbox.getChildren().get(0) instanceof Label nomeEQuantitaLabel) {
                String testo = nomeEQuantitaLabel.getText(); // Es. "Pizza Margherita x 2"
                String[] parti = testo.split(" x ");
                if (parti.length > 1) {
                    String nomeProdotto = parti[0].trim();
                    prodotti.add(nomeProdotto);
                }
            }
        }
        return prodotti;
    }

    // Recupera la quantità di un prodotto specifico dal riepilogo
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
                    // Converte il valore enum in String
                    prodottoBean.setCategoria(prodotto.getCategoria().name());
                    prodottoBean.setDisponibile(prodotto.isDisponibile());
                    return prodottoBean;
                })
                .toList();
    }


    public OrdineBean getOrdineById(int id) {
        return load(id);
    }

    public OrdineBean load(int idOrdine) {
        Ordine ordine = ordineDao.getById(idOrdine);
        if (ordine == null) {
            throw new IllegalArgumentException("L'ordine con ID " + idOrdine + " non esiste.");
        }
        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setId(ordine.getId());
        ordineBean.setProdotti(ordine.getProdotti());
        ordineBean.setQuantita(ordine.getQuantita());
        return ordineBean;
    }

    public List<ProdottoBean> getTuttiProdotti() {
        List<Prodotto> prodotti = prodottoDao.getAll();
        return prodotti.stream()
                .map(prodotto -> {
                    ProdottoBean prodottoBean = new ProdottoBean();
                    prodottoBean.setId(prodotto.getId());
                    prodottoBean.setNome(prodotto.getNome());
                    prodottoBean.setPrezzo(prodotto.getPrezzo());
                    // Converte il valore enum in String
                    prodottoBean.setCategoria(prodotto.getCategoria().name());
                    prodottoBean.setDisponibile(prodotto.isDisponibile());
                    return prodottoBean;
                })
                .toList();
    }

}
