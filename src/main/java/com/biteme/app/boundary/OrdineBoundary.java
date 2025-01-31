package com.biteme.app.boundary;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.OrdineController;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import java.util.List;

public class OrdineBoundary {

    private final OrdineController controller = new OrdineController();

    @FXML
    private FlowPane flowPaneProdotti; // Pane dove mostreremo i prodotti

    @FXML
    private Label nomeTavolo; // Etichetta con fx:id="nomeTavolo"

    private String tavoloCorrente; // Nome del tavolo corrente

    public OrdineBoundary() {
        // Costruttore vuoto
    }

    /**
     * Inizializza i componenti della UI con i dati dell'ordine selezionato.
     * Questo metodo viene chiamato automaticamente quando la scena viene caricata.
     */
    @FXML
    public void initialize() {
        // Recupera il bean statico da OrdinazioneBoundary
        OrdinazioneBean ordinazioneBean = OrdinazioneBoundary.getOrdineSelezionato();

        if (ordinazioneBean == null) {
            throw new IllegalStateException("Nessun ordine selezionato. Impossibile inizializzare la schermata.");
        }

        // Imposta i dati relativi all'ordine
        this.tavoloCorrente = ordinazioneBean.getInfoTavolo(); // Nome del tavolo o "Asporto"
        this.nomeTavolo.setText("Tavolo: " + tavoloCorrente);

        // Mostra il numero di clienti
        Label numeroClientiLabel = new Label("Numero Clienti: " + ordinazioneBean.getNumeroClienti());
        numeroClientiLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        numeroClientiLabel.setAlignment(Pos.CENTER);

        // Aggiungi l'etichetta al FlowPane
        flowPaneProdotti.getChildren().clear();
        flowPaneProdotti.getChildren().add(numeroClientiLabel);

        // Carica i prodotti associati all'ordine
        caricaProdottiAssociati();
    }

    /**
     * Metodo per caricare dinamicamente i prodotti associati all'ordine.
     * Recupera i prodotti tramite OrdineController e li aggiunge alla UI.
     */
    private void caricaProdottiAssociati() {
        // Usa il controller per ottenere i prodotti per il tavolo corrente
        List<ProdottoBean> prodotti = controller.getProdottiByCategoria("Tutti"); // Inserisci una categoria generica o altro criterio

        // Ripulisce il FlowPane prima di aggiungere nuovi elementi
        flowPaneProdotti.getChildren().clear();

        // Cicla sulla lista dei prodotti e li aggiunge alla UI
        for (ProdottoBean prodotto : prodotti) {
            VBox prodottoBox = new VBox();
            prodottoBox.setAlignment(Pos.CENTER);
            prodottoBox.setSpacing(5);

            Label nomeProdotto = new Label(prodotto.getNome());
            nomeProdotto.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label prezzoProdotto = new Label("€ " + prodotto.getPrezzo());
            prezzoProdotto.setStyle("-fx-font-size: 12px;");

            prodottoBox.getChildren().addAll(nomeProdotto, prezzoProdotto);

            // Aggiunge il contenitore del prodotto al FlowPane
            flowPaneProdotti.getChildren().add(prodottoBox);
        }
    }

    /**
     * Metodo per gestire le bevande.
     */
    @FXML
    private void handleCategoriaBevande() {
        caricaProdotti("Bevande");
    }

    /**
     * Metodo per gestire gli antipasti.
     */
    @FXML
    private void handleCategoriaAntipasti() {
        caricaProdotti("Antipasti");
    }

    /**
     * Metodo per gestire le pizze.
     */
    @FXML
    private void handleCategoriaPizze() {
        caricaProdotti("Pizze");
    }

    /**
     * Metodo per gestire i primi piatti.
     */
    @FXML
    private void handleCategoriaPrimiPiatti() {
        caricaProdotti("Primi");
    }

    /**
     * Metodo per gestire i secondi piatti.
     */
    @FXML
    private void handleCategoriaSecondiPiatti() {
        caricaProdotti("Secondi");
    }

    /**
     * Metodo per gestire i contorni.
     */
    @FXML
    private void handleCategoriaContorni() {
        caricaProdotti("Contorni");
    }

    /**
     * Metodo per gestire i dolci.
     */
    @FXML
    private void handleCategoriaDolci() {
        caricaProdotti("Dolci");
    }

    /**
     * Metodo generico per caricare i prodotti in base alla categoria.
     * Utilizza il controller per recuperare i dati e li aggiunge al FlowPane.
     *
     * @param categoria Categoria dei prodotti
     */
    private void caricaProdotti(String categoria) {
        // Verifica che il FlowPane sia inizializzato
        if (flowPaneProdotti == null) {
            System.err.println("Errore: flowPaneProdotti non è stato inizializzato.");
            return;
        }

        // Pulizia dinamica: rimuove tutto prima di aggiungere nuovi contenuti
        flowPaneProdotti.getChildren().clear();

        // Ottieni i prodotti tramite il controller
        List<ProdottoBean> prodotti = controller.getProdottiByCategoria(categoria);

        // Aggiungi i nuovi prodotti al FlowPane
        if (prodotti != null && !prodotti.isEmpty()) {
            for (ProdottoBean prodotto : prodotti) {
                // Creazione del layout grafico per ogni prodotto
                VBox boxProdotto = new VBox(10); // Box verticale con spaziatura di 10px
                boxProdotto.setAlignment(Pos.CENTER); // Contenuto centrato
                boxProdotto.setStyle("-fx-padding: 10px; -fx-background-color: white; -fx-border-color: lightgray; "
                        + "-fx-border-width: 1; -fx-effect: dropshadow(gaussian, lightgray, 10, 0, 3, 3);");
                boxProdotto.setPrefSize(150, 150);

                // Nome del prodotto centrato
                Label labelNome = new Label(prodotto.getNome());
                labelNome.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                // Etichetta quantità centrata
                Label labelQuantita = new Label("Quantità");
                labelQuantita.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");

                // Controllo della quantità
                HBox controlloQuantita = new HBox(10); // Box orizzontale con spaziatura di 10px
                controlloQuantita.setAlignment(Pos.CENTER); // Elementi centrati

                Button meno = new Button("-"); // Pulsante per diminuire
                meno.setPrefSize(30, 30);

                Label quantita = new Label("0"); // Quantità iniziale
                quantita.setStyle("-fx-font-size: 14px;");

                Button piu = new Button("+"); // Pulsante per incrementare
                piu.setPrefSize(30, 30);

                // Aggiungi evento al pulsante +
                piu.setOnAction(event -> {
                    int currentQuantity = Integer.parseInt(quantita.getText());
                    quantita.setText(String.valueOf(currentQuantity + 1));
                });

                // Aggiungi evento al pulsante -
                meno.setOnAction(event -> {
                    int currentQuantity = Integer.parseInt(quantita.getText());
                    if (currentQuantity > 0) {
                        quantita.setText(String.valueOf(currentQuantity - 1));
                    }
                });

                // Aggiungi i controlli di quantità al layout
                controlloQuantita.getChildren().addAll(meno, quantita, piu);

                // Aggiungi elementi al layout del prodotto
                boxProdotto.getChildren().addAll(labelNome, labelQuantita, controlloQuantita);

                // Aggiungi il prodotto al FlowPane
                flowPaneProdotti.getChildren().add(boxProdotto);
            }
        } else {
            // Nessun prodotto trovato: mostra messaggio
            Label noProdotti = new Label("Nessun prodotto disponibile per la categoria: " + categoria);
            noProdotti.setStyle("-fx-font-size: 14px; -fx-text-fill: gray; -fx-padding: 10px;");
            flowPaneProdotti.getChildren().add(noProdotti);
        }
    }
}