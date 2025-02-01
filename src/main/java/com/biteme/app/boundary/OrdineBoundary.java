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
import javafx.scene.layout.Region;
import com.biteme.app.util.SceneLoader;
import com.biteme.app.bean.OrdineBean;
import java.util.ArrayList;
import java.util.logging.Logger;

public class OrdineBoundary {

    private final OrdineController controller = new OrdineController();


    @FXML
    private FlowPane flowPaneProdotti; // Pane dove mostreremo i prodotti

    @FXML
    private Label nomeTavolo; // Etichetta con fx:id="nomeTavolo"

    @FXML
    private VBox riepilogoContenuto; // Contenitore per il riepilogo

    @FXML
    private Label totaleOrdine; // Etichetta con fx:id="totaleOrdine"

    private static final String ASPORTO = "Asporto";

    public OrdineBoundary() {
        // Costruttore vuoto
    }


    @FXML
    private void handleIndietro() {
        // Recupera i prodotti dal riepilogo
        List<String> prodotti = recuperaProdottiDalRiepilogo();
        List<Integer> quantita = new ArrayList<>();

        // Usa i prodotti per recuperare le quantità corrispondenti
        for (String prodotto : prodotti) {
            quantita.add(recuperaQuantitaDalRiepilogo(prodotto));
        }

        // Crea un oggetto OrdineBean e imposta i prodotti e le quantità
        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setProdotti(prodotti);
        ordineBean.setQuantita(quantita);

        // Salva l'ordine utilizzando OrdineController
        OrdineController ordineController = new OrdineController();
        OrdinazioneBean ordinazioneBean = OrdinazioneBoundary.getOrdineSelezionato();
        int ordineId = ordinazioneBean.getId();
        ordineController.salvaOrdine(ordineBean, ordineId);

        // Cambia scena e torna alla schermata di ordinazione
        SceneLoader.loadScene("/com/biteme/app/ordinazione.fxml", "Torna a Ordinazione");
    }


    @FXML
    public void initialize() {
        // Recupera l'ordinazione selezionata tramite OrdinazioneBoundary
        OrdinazioneBean ordinazioneBean = OrdinazioneBoundary.getOrdineSelezionato();

        if (ordinazioneBean != null) {
            // Ottieni l'ID dell'ordine dall'ordinazione selezionata
            int ordineId = ordinazioneBean.getId();

            // Carica l'ordine associato a questo ID tramite il controller
            OrdineBean ordineBean = controller.load(ordineId);

            if (ordineBean != null) {
                // Controlla se infoTavolo è null o rappresenta un ordine da asporto
                String infoTavolo = ordinazioneBean.getInfoTavolo();
                if (infoTavolo == null || ASPORTO.equalsIgnoreCase(infoTavolo)) {
                    this.nomeTavolo.setText(ASPORTO);
                } else {
                    this.nomeTavolo.setText("Tavolo: " + infoTavolo);
                }
                caricaProdottiAssociati();
                // Carica i prodotti e le quantità nel riepilogo
                caricaProdottiNelRiepilogo(ordineBean);
            }
        } else {
            Logger.getLogger(OrdineBoundary.class.getName())
                    .warning("Nessuna ordinazione selezionata.");
        }
    }


    /**
     * Carica i prodotti e le quantità nel riepilogo utilizzando i dati di OrdineBean.
     */
    private void caricaProdottiNelRiepilogo(OrdineBean ordineBean) {
        // Pulizia iniziale del contenitore per evitare duplicati
        riepilogoContenuto.getChildren().clear();

        // Recupera i prodotti e le quantità da OrdineBean
        List<String> prodotti = ordineBean.getProdotti();
        List<Integer> quantita = ordineBean.getQuantita();
        List<ProdottoBean> prodottiDisponibili = controller.getTuttiProdotti(); // Ottieni tutti i prodotti disponibili

        // Controllo della coerenza dei dati
        if (prodotti.size() != quantita.size()) {
            Logger.getLogger(OrdineBoundary.class.getName())
                    .warning("I dati del bean ordine sono incoerenti: prodotti e quantità non corrispondono.");
            return;
        }

        // Itera su ogni prodotto per aggiungerlo al riepilogo
        for (int i = 0; i < prodotti.size(); i++) {
            String nomeProdotto = prodotti.get(i);
            int quantitaProdotto = quantita.get(i);

            // Recupera il prezzo del prodotto usando il nome
            double prezzoProdotto = recuperaPrezzoProdotto(nomeProdotto, prodottiDisponibili);

            // Aggiunge il prodotto al riepilogo con il prezzo corretto
            aggiungiAlRiepilogo(nomeProdotto, prezzoProdotto, quantitaProdotto);

        }

        // Aggiorna il totale dell'ordine al termine
        aggiornaTotaleOrdine();
    }

    private double recuperaPrezzoProdotto(String nomeProdotto, List<ProdottoBean> prodottiDisponibili) {
        for (ProdottoBean prodotto : prodottiDisponibili) {
            if (prodotto.getNome().equalsIgnoreCase(nomeProdotto)) {
                return prodotto.getPrezzo().doubleValue();
            }
        }
        return 0.0;
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

    private void caricaProdotti(String categoria) {
        // Pulisce il contenitore per i prodotti
        flowPaneProdotti.getChildren().clear();

        // Carica i prodotti della categoria specificata
        List<ProdottoBean> prodotti = controller.getProdottiByCategoria(categoria);

        if (prodotti.isEmpty()) {
            // Mostra il messaggio "Nessun prodotto disponibile"
            mostraMessaggioNessunProdotto(categoria);
        } else {
            for (ProdottoBean prodotto : prodotti) {
                // Creazione della VBox del prodotto (gestisce già TUTTI i componenti)
                VBox boxProdotto = creaBoxProdotto(prodotto);

                // Aggiunge il prodotto al FlowPane
                flowPaneProdotti.getChildren().add(boxProdotto);
            }
        }
    }

    private int recuperaQuantitaDalRiepilogo(String nomeProdotto) {

        for (javafx.scene.Node nodo : riepilogoContenuto.getChildren()) {
            // Verifica direttamente se il nodo è un HBox e contiene una Label come primo elemento
            if (nodo instanceof HBox hbox && hbox.getChildren().get(0) instanceof Label nomeEQuantitaLabel) {
                String testo = nomeEQuantitaLabel.getText(); // Es. "Pizza Margherita x 2"

                // Verifica che il testo sia nel formato atteso ("NomeProdotto x Quantità")
                if (testo.startsWith(nomeProdotto + " x")) {
                    try {
                        // Estrae la quantità dal testo
                        String[] parti = testo.split(" x ");
                        return Integer.parseInt(parti[1].trim()); // Converti in intero
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                        // In caso di errore nel parsing, restituisce 0
                        return 0;
                    }
                }
            }
        }

        // Se il prodotto non è trovato nel riepilogo, restituisce 0 (quantità iniziale)
        return 0;
    }


    private List<String> recuperaProdottiDalRiepilogo() {
        List<String> prodotti = new ArrayList<>();

        for (javafx.scene.Node nodo : riepilogoContenuto.getChildren()) {
            // Verifica direttamente se il nodo è un HBox e il suo primo elemento è una Label
            if (nodo instanceof HBox hbox && hbox.getChildren().get(0) instanceof Label nomeEQuantitaLabel) {
                String testo = nomeEQuantitaLabel.getText(); // Es. "Pizza Margherita x 2"

                // Verifica che il testo sia nel formato atteso ("NomeProdotto x Quantità")
                String[] parti = testo.split(" x ");
                if (parti.length > 1) { // Assumiamo che ci sia sempre il formato corretto
                    String nomeProdotto = parti[0].trim();
                    prodotti.add(nomeProdotto); // Aggiunge solo il nome del prodotto
                }
            }
        }
        return prodotti;
    }


    private VBox creaBoxProdotto(ProdottoBean prodotto) {
        VBox boxProdotto = new VBox(10); // Spaziatura di 10px
        boxProdotto.setAlignment(Pos.CENTER); // Allineato centralmente
        boxProdotto.setStyle("-fx-padding: 10px; -fx-background-color: white; -fx-border-color: lightgray; "
                + "-fx-border-width: 1; -fx-effect: dropshadow(gaussian, lightgray, 10, 0, 3, 3);");
        boxProdotto.setPrefSize(150, 150);

        // Nome del prodotto
        Label labelNome = creaLabelNome(prodotto.getNome());

        // Recupera l'eventuale quantità dal riepilogo
        int quantitaDalRiepilogo = recuperaQuantitaDalRiepilogo(prodotto.getNome());

        // Creazione dell'etichetta "Quantità:"
        Label labelQuantitaText = new Label("Quantità:");
        labelQuantitaText.setStyle("-fx-font-size: 14px; -fx-text-fill: black; -fx-font-weight: bold;");

        // Crea i controlli della quantità (già sincronizzati)
        HBox controlliQuantita = creaControlliQuantita(prodotto);
        Label quantitaLabel = (Label) controlliQuantita.getChildren().get(1); // La `Label` del contatore

        // Imposta la quantità iniziale nella Label
        quantitaLabel.setText(String.valueOf(quantitaDalRiepilogo));

        // Aggiunge i componenti alla VBox
        boxProdotto.getChildren().addAll(labelNome, labelQuantitaText, controlliQuantita);

        return boxProdotto;
    }

    private Label creaLabelNome(String nome) {
        Label labelNome = new Label(nome);
        labelNome.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        return labelNome;
    }

    private HBox creaControlliQuantita(ProdottoBean prodotto) {
        HBox controlloQuantita = new HBox(10); // Spaziatura orizzontale di 10px
        controlloQuantita.setAlignment(Pos.CENTER); // Centra gli elementi

        // Recupera l'eventuale quantità già presente nel riepilogo
        int quantitaIniziale = recuperaQuantitaDalRiepilogo(prodotto.getNome());

        // Crea una sola Label per rappresentare il contatore
        Label quantitaLabel = creaLabelQuantitaContatore(quantitaIniziale);

        // Crea i pulsanti "-" e "+"
        Button meno = creaBottoneMeno(quantitaLabel, prodotto);
        Button piu = creaBottonePiu(quantitaLabel, prodotto);

        // Aggiunge i controlli e il contatore all'HBox
        controlloQuantita.getChildren().addAll(meno, quantitaLabel, piu);

        return controlloQuantita;
    }

    private Label creaLabelQuantitaContatore(int quantitaIniziale) {
        Label labelQuantitaContatore = new Label(String.valueOf(quantitaIniziale)); // Imposta la quantità
        labelQuantitaContatore.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");
        return labelQuantitaContatore;
    }




    private Button creaBottoneMeno(Label quantitaLabel, ProdottoBean prodotto) {
        Button meno = new Button("-");
        meno.setPrefSize(30, 30);

        // Aggiungi uno stile per colorare il pulsante di rosso
        meno.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 16px;");

        meno.setOnAction(event -> {
            // Riduci la quantità visualizzata e aggiorna il riepilogo
            int quantitaAttuale = Integer.parseInt(quantitaLabel.getText());

            if (quantitaAttuale > 0) {
                diminuisciQuantita(quantitaLabel, prodotto.getNome());
                aggiornaRiepilogo(prodotto.getNome(), prodotto.getPrezzo().doubleValue(), -1);
            }
        });

        return meno;
    }

    private void diminuisciQuantita(Label quantita, String nomeProdotto) {
        int currentQuantity = Integer.parseInt(quantita.getText());

        if (currentQuantity > 0) {
            quantita.setText(String.valueOf(currentQuantity - 1));

            // Se la quantità arriva a 0, rimuovi il prodotto
            if (currentQuantity - 1 == 0) {
                rimuoviDalRiepilogo(nomeProdotto);
            }
        }
    }

    private void rimuoviDalRiepilogo(String nomeProdotto) {
        // Rimuovi il prodotto dal riepilogo
        riepilogoContenuto.getChildren().removeIf(nodo ->
                nodo instanceof HBox hbox &&
                        hbox.getChildren().get(0) instanceof Label nomeEQuantitaLabel &&
                        nomeEQuantitaLabel.getText().startsWith(nomeProdotto + " x")
        );

        // Una volta completata la rimozione, aggiorna il totale
        aggiornaTotaleOrdine();
    }



    private Button creaBottonePiu(Label quantitaLabel, ProdottoBean prodotto) {
        Button piu = new Button("+");
        piu.setPrefSize(30, 30);

        // Aggiungi uno stile per il testo verde con font grassetto
        piu.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 16px;");

        // Aggiungi l'azione al pulsante
        piu.setOnAction(event -> {
            // Usa il metodo centrale per aumentare la quantità
            aumentaQuantita(quantitaLabel);

            // Aggiorna il riepilogo dei prodotti
            aggiornaRiepilogo(prodotto.getNome(), prodotto.getPrezzo().doubleValue(), 1);
        });

        return piu;
    }

    private void aumentaQuantita(Label quantita) {
        int currentQuantity = Integer.parseInt(quantita.getText());
        quantita.setText(String.valueOf(currentQuantity + 1));
    }

    private void aggiungiAlRiepilogo(String nomeProdotto, double prezzo, int quantita) {
        // Crea una HBox per rappresentare l'elemento nel riepilogo
        HBox nuovoElemento = new HBox(10); // 10 è il gap tra i nodi
        nuovoElemento.setAlignment(Pos.CENTER_LEFT);

        // Combina nome prodotto e quantità in un'unica Label
        Label nomeEQuantitaLabel = new Label(nomeProdotto + " x " + quantita);
        nomeEQuantitaLabel.setPrefWidth(200); // Larghezza fissa per allineamento migliore
        nomeEQuantitaLabel.setStyle("-fx-font-size: 14px;");

        // Crea uno Spacer (flessibile) per separare il nome e il prezzo
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS); // Permette allo spacer di espandersi

        // Aggiungi il prezzo
        Label prezzoLabel = new Label(String.format("%.2f €", prezzo * quantita));
        prezzoLabel.setStyle("-fx-font-size: 14px;");

        // Aggiungi i nodi alla HBox: nome/quantità, spacer, prezzo
        nuovoElemento.getChildren().addAll(nomeEQuantitaLabel, spacer, prezzoLabel);

        // Aggiungi la HBox al contenitore dei riepiloghi
        riepilogoContenuto.getChildren().add(nuovoElemento);
        // Richiama il metodo per aggiornare il totale ordine
        aggiornaTotaleOrdine();
    }



    private void mostraMessaggioNessunProdotto(String categoria) {
        Label noProdotti = new Label("Nessun prodotto disponibile per la categoria: " + categoria);
        noProdotti.setStyle("-fx-font-size: 14px; -fx-text-fill: gray; -fx-padding: 10px;");
        flowPaneProdotti.getChildren().add(noProdotti);
    }



    private void aggiornaRiepilogo(String nomeProdotto, double prezzo, int quantita) {
        // Cerca e aggiorna l'elemento, se trovato
        if (aggiornaElementoEsistente(nomeProdotto, prezzo, quantita)) {
            return;
        }

        // Aggiungi l'elemento al riepilogo solo se la quantità è > 0
        if (quantita > 0) {
            aggiungiAlRiepilogo(nomeProdotto, prezzo, quantita);
        }
    }

    private boolean aggiornaElementoEsistente(String nomeProdotto, double prezzo, int quantita) {
        for (javafx.scene.Node nodo : riepilogoContenuto.getChildren()) {
            if (nodo instanceof HBox hbox) {
                Label nomeEQuantitaLabel = getNomeEQuantitaLabel(hbox);
                if (nomeEQuantitaLabel != null && nomeEQuantitaLabel.getText().startsWith(nomeProdotto + " x")) {
                    int currentQuantity = getQuantitaCorrente(nomeEQuantitaLabel);

                    // Se la nuova quantità va a 0 o sotto, rimuovi
                    if (currentQuantity + quantita <= 0) {
                        rimuoviDalRiepilogo(nomeProdotto);
                        return true;
                    }

                    // Aggiorna quantità e prezzo
                    aggiornaQuantitaEPrezzo(hbox, nomeProdotto, prezzo, currentQuantity + quantita);
                    return true;
                }
            }
        }
        return false;
    }

    private Label getNomeEQuantitaLabel(HBox hbox) {
        if (hbox.getChildren().get(0) instanceof Label label) {
            return label;
        }
        return null;
    }

    private int getQuantitaCorrente(Label nomeEQuantitaLabel) {
        String[] parti = nomeEQuantitaLabel.getText().split(" x ");
        return Integer.parseInt(parti[1]);
    }

    private void aggiornaQuantitaEPrezzo(HBox hbox, String nomeProdotto, double prezzo, int nuovaQuantita) {
        Label nomeEQuantitaLabel = (Label) hbox.getChildren().get(0);
        Label prezzoLabel = (Label) hbox.getChildren().get(2); // Terzo elemento è il prezzo

        nomeEQuantitaLabel.setText(nomeProdotto + " x " + nuovaQuantita);
        prezzoLabel.setText(String.format("%.2f €", prezzo * nuovaQuantita));

        aggiornaTotaleOrdine();
    }


    @FXML
    private void aggiornaTotaleOrdine() {
        Logger logger = Logger.getLogger(this.getClass().getName());
        double totale = 0.0;

        // Calcola il totale solo se ci sono elementi nel riepilogo
        if (riepilogoContenuto.getChildren().isEmpty()) {
            // Se non ci sono prodotti, il totale è zero
            totaleOrdine.setText("Totale: €0.00");
            return;
        }

        // Itera attraverso gli elementi per calcolare il totale
        for (javafx.scene.Node nodo : riepilogoContenuto.getChildren()) {
            if (nodo instanceof HBox hbox) {
                // Ultimo nodo dell'HBox è il prezzo
                Label prezzoLabel = (Label) hbox.getChildren().get(hbox.getChildren().size() - 1);
                String prezzoTesto = prezzoLabel.getText().replace("€", "").trim().replace(",", ".");
                try {
                    totale += Double.parseDouble(prezzoTesto);
                } catch (NumberFormatException e) {
                    logger.warning("Errore nel formato del prezzo: " + prezzoTesto);
                }
            }
        }

        // Imposta il valore aggiornato
        totaleOrdine.setText(String.format("Totale: €%.2f", totale));
    }


}