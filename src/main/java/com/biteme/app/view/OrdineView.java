package com.biteme.app.view;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.OrdinazioneController;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.util.SceneLoader;
import com.biteme.app.bean.OrdineBean;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrdineView {

    private final OrdineController controller = new OrdineController();
    private final OrdinazioneController ordinazioneController = new OrdinazioneController();

    @FXML
    private FlowPane flowPaneProdotti;

    @FXML
    private Label nomeTavolo;

    @FXML
    private VBox riepilogoContenuto;

    @FXML
    private Label totaleOrdine;

    private static final String ASPORTO = "Asporto";

    @FXML
    private void handleSalva() {
        int ordineId = ordinazioneController.getIdOrdineSelezionato();
        controller.salvaOrdineEStato(ordineId, "IN_CORSO");
        showAlert("Ordine salvato", "L'ordine è stato salvato con successo.", AlertType.INFORMATION);
        ordinazioneController.cambiaASchermataOrdinazione();
    }

    @FXML
    public void handleCheckout(ActionEvent actionEvent) {
        int ordineId = ordinazioneController.getIdOrdineSelezionato();
        controller.salvaOrdineEStato(ordineId, "COMPLETATO");
        showAlert("Checkout completato", "Il checkout dell'ordine è stato completato con successo.", AlertType.INFORMATION);
        ordinazioneController.cambiaASchermataOrdinazione();
    }

    @FXML
    private void handleIndietro(){
        SceneLoader.loadScene("/com/biteme/app/ordinazione.fxml", "Torna a Ordinazione");
    }

    @FXML
    public void initialize() {
        controller.setRiepilogoContenuto(this.riepilogoContenuto);
        OrdinazioneBean ordinazioneBean = OrdinazioneView.getOrdineSelezionato();

        if (ordinazioneBean != null) {
            int ordineId = ordinazioneBean.getId();
            OrdineBean ordineBean = controller.load(ordineId);
            if (ordineBean != null) {
                String infoTavolo = ordinazioneBean.getInfoTavolo();
                if (infoTavolo == null || infoTavolo.trim().isEmpty() || ASPORTO.equalsIgnoreCase(infoTavolo)) {
                    this.nomeTavolo.setText(ASPORTO);
                } else {
                    this.nomeTavolo.setText("Tavolo: " + infoTavolo);
                }
                caricaProdottiAssociati();
                caricaProdottiNelRiepilogo(ordineBean);
            } else {
                Logger.getLogger(OrdineView.class.getName())
                        .log(Level.SEVERE, () -> "OrdineBean non trovato per l'ID: " + ordineId);
                showAlert("Errore", "Ordine non trovato per l'ID: " + ordineId, AlertType.ERROR);
            }
        } else {
            Logger.getLogger(OrdineView.class.getName())
                    .warning("Nessuna ordinazione selezionata.");
            showAlert("Errore", "Nessuna ordinazione selezionata.", AlertType.ERROR);
        }
    }

    private void caricaProdottiNelRiepilogo(OrdineBean ordineBean) {
        riepilogoContenuto.getChildren().clear();
        List<String> prodotti = ordineBean.getProdotti();
        List<Integer> quantita = ordineBean.getQuantita();
        List<ProdottoBean> prodottiDisponibili = controller.getTuttiProdotti(); // Ottieni tutti i prodotti disponibili

        for (int i = 0; i < prodotti.size(); i++) {
            String nomeProdotto = prodotti.get(i);
            int quantitaProdotto = quantita.get(i);
            double prezzoProdotto = recuperaPrezzoProdotto(nomeProdotto, prodottiDisponibili);

            aggiungiAlRiepilogo(nomeProdotto, prezzoProdotto, quantitaProdotto);
        }
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

    private void caricaProdottiAssociati() {
        List<ProdottoBean> prodotti = controller.getProdottiByCategoria("Tutti");

        flowPaneProdotti.getChildren().clear();

        for (ProdottoBean prodotto : prodotti) {
            VBox prodottoBox = new VBox();
            prodottoBox.setAlignment(Pos.CENTER);
            prodottoBox.setSpacing(5);

            Label nomeProdotto = new Label(prodotto.getNome());
            nomeProdotto.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label prezzoProdotto = new Label("€ " + prodotto.getPrezzo());
            prezzoProdotto.setStyle("-fx-font-size: 12px;");

            prodottoBox.getChildren().addAll(nomeProdotto, prezzoProdotto);

            flowPaneProdotti.getChildren().add(prodottoBox);
        }
    }

    // I metodi per la gestione delle categorie e dei controlli sul riepilogo rimangono invariati
    @FXML
    private void handleCategoriaBevande() {
        caricaProdotti("Bevande");
    }

    @FXML
    private void handleCategoriaAntipasti() {
        caricaProdotti("Antipasti");
    }

    @FXML
    private void handleCategoriaPizze() {
        caricaProdotti("Pizze");
    }

    @FXML
    private void handleCategoriaPrimiPiatti() {
        caricaProdotti("Primi");
    }

    @FXML
    private void handleCategoriaSecondiPiatti() {
        caricaProdotti("Secondi");
    }

    @FXML
    private void handleCategoriaContorni() {
        caricaProdotti("Contorni");
    }

    @FXML
    private void handleCategoriaDolci() {
        caricaProdotti("Dolci");
    }

    private void caricaProdotti(String categoria) {
        flowPaneProdotti.getChildren().clear();
        List<ProdottoBean> prodotti = controller.getProdottiByCategoria(categoria);

        if (prodotti.isEmpty()) {
            mostraMessaggioNessunProdotto(categoria);
        } else {
            for (ProdottoBean prodotto : prodotti) {
                VBox boxProdotto = creaBoxProdotto(prodotto);
                flowPaneProdotti.getChildren().add(boxProdotto);
            }
        }
    }

    private VBox creaBoxProdotto(ProdottoBean prodotto) {
        VBox boxProdotto = new VBox(10);
        boxProdotto.setAlignment(Pos.CENTER);
        boxProdotto.setStyle("-fx-padding: 10px; -fx-background-color: white; -fx-border-color: lightgray; "
                + "-fx-border-width: 1; -fx-effect: dropshadow(gaussian, lightgray, 10, 0, 3, 3);");
        boxProdotto.setPrefSize(150, 150);
        Label labelNome = creaLabelNome(prodotto.getNome());

        int quantitaDalRiepilogo = controller.recuperaQuantitaDalRiepilogo(prodotto.getNome());

        Label labelQuantitaText = new Label("Quantità:");
        labelQuantitaText.setStyle("-fx-font-size: 14px; -fx-text-fill: black; -fx-font-weight: bold;");
        HBox controlliQuantita = creaControlliQuantita(prodotto);
        Label quantitaLabel = (Label) controlliQuantita.getChildren().get(1); // La Label del contatore
        quantitaLabel.setText(String.valueOf(quantitaDalRiepilogo));

        boxProdotto.getChildren().addAll(labelNome, labelQuantitaText, controlliQuantita);
        return boxProdotto;
    }

    private Label creaLabelNome(String nome) {
        Label labelNome = new Label(nome);
        labelNome.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        return labelNome;
    }

    private HBox creaControlliQuantita(ProdottoBean prodotto) {
        HBox controlloQuantita = new HBox(10);
        controlloQuantita.setAlignment(Pos.CENTER);

        int quantitaIniziale = controller.recuperaQuantitaDalRiepilogo(prodotto.getNome());
        Label quantitaLabel = creaLabelQuantitaContatore(quantitaIniziale);

        Button meno = creaBottoneMeno(quantitaLabel, prodotto);
        Button piu = creaBottonePiu(quantitaLabel, prodotto);

        controlloQuantita.getChildren().addAll(meno, quantitaLabel, piu);
        return controlloQuantita;
    }

    private Label creaLabelQuantitaContatore(int quantitaIniziale) {
        Label labelQuantitaContatore = new Label(String.valueOf(quantitaIniziale));
        labelQuantitaContatore.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");
        return labelQuantitaContatore;
    }

    private Button creaBottoneMeno(Label quantitaLabel, ProdottoBean prodotto) {
        Button meno = new Button("-");
        meno.setPrefSize(30, 30);
        meno.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 16px;");
        meno.setOnAction(event -> {
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
            if (currentQuantity - 1 == 0) {
                rimuoviDalRiepilogo(nomeProdotto);
            }
        }
    }

    private void rimuoviDalRiepilogo(String nomeProdotto) {
        riepilogoContenuto.getChildren().removeIf(nodo ->
                nodo instanceof HBox hbox &&
                        hbox.getChildren().get(0) instanceof Label nomeEQuantitaLabel &&
                        nomeEQuantitaLabel.getText().startsWith(nomeProdotto + " x")
        );
        aggiornaTotaleOrdine();
    }

    private Button creaBottonePiu(Label quantitaLabel, ProdottoBean prodotto) {
        Button piu = new Button("+");
        piu.setPrefSize(30, 30);
        piu.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 16px;");
        piu.setOnAction(event -> {
            aumentaQuantita(quantitaLabel);
            aggiornaRiepilogo(prodotto.getNome(), prodotto.getPrezzo().doubleValue(), 1);
        });
        return piu;
    }

    private void aumentaQuantita(Label quantita) {
        int currentQuantity = Integer.parseInt(quantita.getText());
        quantita.setText(String.valueOf(currentQuantity + 1));
    }

    private void aggiungiAlRiepilogo(String nomeProdotto, double prezzo, int quantita) {
        HBox nuovoElemento = new HBox(10);
        nuovoElemento.setAlignment(Pos.CENTER_LEFT);
        Label nomeEQuantitaLabel = new Label(nomeProdotto + " x " + quantita);
        nomeEQuantitaLabel.setPrefWidth(200);
        nomeEQuantitaLabel.setStyle("-fx-font-size: 14px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label prezzoLabel = new Label(String.format("%.2f €", prezzo * quantita));
        prezzoLabel.setStyle("-fx-font-size: 14px;");
        nuovoElemento.getChildren().addAll(nomeEQuantitaLabel, spacer, prezzoLabel);
        riepilogoContenuto.getChildren().add(nuovoElemento);
        aggiornaTotaleOrdine();
    }

    private void mostraMessaggioNessunProdotto(String categoria) {
        Label noProdotti = new Label("Nessun prodotto disponibile per la categoria: " + categoria);
        noProdotti.setStyle("-fx-font-size: 14px; -fx-text-fill: gray; -fx-padding: 10px;");
        flowPaneProdotti.getChildren().add(noProdotti);
    }

    private void aggiornaRiepilogo(String nomeProdotto, double prezzo, int quantita) {
        if (aggiornaElementoEsistente(nomeProdotto, prezzo, quantita)) {
            return;
        }
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
                    if (currentQuantity + quantita <= 0) {
                        rimuoviDalRiepilogo(nomeProdotto);
                        return true;
                    }
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
        Label prezzoLabel = (Label) hbox.getChildren().get(2);
        nomeEQuantitaLabel.setText(nomeProdotto + " x " + nuovaQuantita);
        prezzoLabel.setText(String.format("%.2f €", prezzo * nuovaQuantita));
        aggiornaTotaleOrdine();
    }

    @FXML
    private void aggiornaTotaleOrdine() {
        Logger logger = Logger.getLogger(this.getClass().getName());
        double totale = 0.0;
        if (riepilogoContenuto.getChildren().isEmpty()) {
            totaleOrdine.setText("Totale: €0.00");
            return;
        }
        for (javafx.scene.Node nodo : riepilogoContenuto.getChildren()) {
            if (nodo instanceof HBox hbox) {
                Label prezzoLabel = (Label) hbox.getChildren().get(hbox.getChildren().size() - 1);
                String prezzoTesto = prezzoLabel.getText().replace("€", "").trim().replace(",", ".");
                try {
                    totale += Double.parseDouble(prezzoTesto);
                } catch (NumberFormatException e) {
                    logger.warning("Errore nel formato del prezzo: " + prezzoTesto);
                }
            }
        }
        totaleOrdine.setText(String.format("Totale: €%.2f", totale));
    }

    private void showAlert(String title, String content, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
