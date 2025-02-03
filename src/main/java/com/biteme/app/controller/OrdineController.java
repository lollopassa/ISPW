package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.entity.Ordine;
import com.biteme.app.entity.Prodotto;
import com.biteme.app.entity.StatoOrdine;
import com.biteme.app.persistence.OrdineDao;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.util.Configuration;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrdineController {

    private final ProdottoDao prodottoDao;

    private final OrdineDao ordineDao;

    private VBox riepilogoContenuto; // Riferimento per accedere al VBox passato dal boundary

    public OrdineController() {
        // Ottenere ProdottoDao usando la configurazione del sistema
        this.prodottoDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getProdottoDao();

        this.ordineDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdineDao();
    }

    // Metodo per impostare il contenitore VBox (viene passato da OrdineBoundary)
    public void setRiepilogoContenuto(VBox riepilogoContenuto) {
        this.riepilogoContenuto = riepilogoContenuto;
    }

    private OrdineBean preparaOrdineBean(List<String> prodotti, List<Integer> quantita) {
        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setProdotti(prodotti);
        ordineBean.setQuantita(quantita);
        return ordineBean;
    }

    // Metodo per salvare l'ordine e aggiornare lo stato
    public void salvaOrdineEStato(int ordineId, StatoOrdine stato) {
        // Recupera prodotti e quantità
        List<String> prodotti = recuperaProdottiDalRiepilogo();
        List<Integer> quantita = new ArrayList<>();
        for (String prodotto : prodotti) {
            quantita.add(recuperaQuantitaDalRiepilogo(prodotto));
        }

        // Prepara OrdineBean e salva l'ordine
        OrdineBean ordineBean = preparaOrdineBean(prodotti, quantita);
        salvaOrdine(ordineBean, ordineId);

        // Aggiorna lo stato dell'ordinazione nel controller dell'ordinazione
        OrdinazioneController ordinazioneController = new OrdinazioneController();
        ordinazioneController.aggiornaStatoOrdinazione(ordineId, stato);
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

    // Recupero dei prodotti dal riepilogo
    private List<String> recuperaProdottiDalRiepilogo() {
        List<String> prodotti = new ArrayList<>();

        for (Node nodo : riepilogoContenuto.getChildren()) {
            // Verifica che il nodo sia un HBox e che il primo elemento sia una Label
            if (nodo instanceof HBox hbox && hbox.getChildren().get(0) instanceof Label nomeEQuantitaLabel) {
                String testo = nomeEQuantitaLabel.getText(); // Es. "Pizza Margherita x 2"

                // Verifica che il testo sia nel formato atteso ("NomeProdotto x Quantità")
                String[] parti = testo.split(" x ");
                if (parti.length > 1) { // Assumiamo che ci sia sempre il formato corretto
                    String nomeProdotto = parti[0].trim(); // Estrae il nome del prodotto
                    prodotti.add(nomeProdotto);
                }
            }
        }

        return prodotti;
    }

    // Recupero della quantità di un prodotto specifico dal riepilogo
    public int recuperaQuantitaDalRiepilogo(String nomeProdotto) {
        for (Node nodo : riepilogoContenuto.getChildren()) {
            // Verifica che il nodo sia un HBox e che contenga una Label con nome e quantità
            if (nodo instanceof HBox hbox && hbox.getChildren().get(0) instanceof Label nomeEQuantitaLabel) {
                String testo = nomeEQuantitaLabel.getText(); // Es. "Pizza Margherita x 2"

                // Verifica che il testo inizi con il nome del prodotto e il separatore " x"
                if (testo.startsWith(nomeProdotto + " x")) {
                    try {
                        // Estrae la quantità dal testo
                        String[] parti = testo.split(" x ");
                        return Integer.parseInt(parti[1].trim()); // Converti la quantità in intero
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                        // Se ci sono problemi nel parsing, restituisce 0
                        return 0;
                    }
                }
            }
        }
        return 0; // Restituisce 0 se il prodotto non viene trovato
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