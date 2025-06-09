package com.biteme.app.ui;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.boundary.OrdineBoundary;
import com.biteme.app.boundary.OrdinazioneBoundary;
import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.exception.OrdineException;
import com.biteme.app.util.SceneLoader;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class OrdineUI {
    private static final String ORDINAZIONE_FXML = "/com/biteme/app/ordinazione.fxml";
    private static final String ORDINAZIONE_TITLE = "Torna a Ordinazione";
    @FXML private FlowPane flowPaneProdotti;
    @FXML private VBox riepilogoContenuto;
    @FXML private Label nomeTavolo;
    @FXML private Label totaleOrdine;
    @FXML private TextField txtProdotto;
    @FXML private TextField txtPrezzo;
    @FXML private TextField txtQuantita;

    private final OrdineBoundary boundary = new OrdineBoundary();
    private int currentOrdineId;

    @FXML
    public void initialize() {
        OrdinazioneBean sel = OrdinazioneBoundary.getSelected();
        if (sel == null) {
            showAlert(Alert.AlertType.ERROR, "Nessuna ordinazione selezionata.");
            return;
        }

        currentOrdineId = sel.getId();
        nomeTavolo.setText(
                (sel.getInfoTavolo() == null || sel.getInfoTavolo().isBlank() || sel.getTipoOrdine().equals("Asporto"))
                        ? "Asporto"
                        : "Tavolo: " + sel.getInfoTavolo()
        );

        try {
            // provo a caricare l'ordine esistente
            OrdineBean ordine = boundary.loadOrdine(currentOrdineId);
            caricaProdottiNelRiepilogo(ordine);
        } catch (OrdineException e) {
            try {
                boundary.salvaOrdineCompleto(
                        currentOrdineId,
                        java.util.Collections.emptyList(),
                        java.util.Collections.emptyList(),
                        java.util.Collections.emptyList()
                );
            } catch (OrdineException ex) {
                showAlert(Alert.AlertType.ERROR, "Errore nella creazione del nuovo ordine: " + ex.getMessage());
                return;
            }
            showAlert(Alert.AlertType.INFORMATION, "Nuovo ordine creato per questa ordinazione");
        }    }

    @FXML
    private void handleCategoriaBevande() {
        caricaCategoria("Bevande");
    }

    @FXML
    private void handleCategoriaAntipasti() {
        caricaCategoria("Antipasti");
    }

    @FXML
    private void handleCategoriaPizze() {
        caricaCategoria("Pizze");
    }

    @FXML
    private void handleCategoriaPrimiPiatti() {
        caricaCategoria("Primi");
    }

    @FXML
    private void handleCategoriaSecondiPiatti() {
        caricaCategoria("Secondi");
    }

    @FXML
    private void handleCategoriaContorni() {
        caricaCategoria("Contorni");
    }

    @FXML
    private void handleCategoriaDolci() {
        caricaCategoria("Dolci");
    }

    @FXML
    private void handleSalva() {
        try {
            var triple = estraiListeDaRiepilogo();
            boundary.salvaOrdineCompleto(currentOrdineId, triple.prodotti, triple.quantita, triple.prezzi);
            showAlert(Alert.AlertType.INFORMATION, "Ordine salvato con successo");
            SceneLoader.getInstance().loadScene(ORDINAZIONE_FXML, ORDINAZIONE_TITLE);
        } catch (OrdineException e) {
            showAlert(Alert.AlertType.WARNING, e.getMessage());
        }
    }

    @FXML
    private void handleCheckout() {
        try {
            var triple = estraiListeDaRiepilogo();
            boundary.salvaOrdineCompleto(currentOrdineId, triple.prodotti, triple.quantita, triple.prezzi);
            new OrdinazioneBoundary()
                    .aggiornaStatoOrdinazione(currentOrdineId, StatoOrdinazione.COMPLETATO);
            showAlert(Alert.AlertType.INFORMATION, "Checkout completato");
            SceneLoader.getInstance().loadScene(ORDINAZIONE_FXML, ORDINAZIONE_TITLE);
        } catch (OrdineException | OrdinazioneException e) {
            showAlert(Alert.AlertType.WARNING, e.getMessage());
        }
    }

    @FXML
    private void handleIndietro() {
        SceneLoader.getInstance().loadScene(ORDINAZIONE_FXML, ORDINAZIONE_TITLE);
    }

    private void caricaCategoria(String categoria) {
        flowPaneProdotti.getChildren().clear();
        List<ProdottoBean> list = boundary.getProdottiByCategoria(categoria);

        if (list.isEmpty()) {
            Label none = new Label("Nessun prodotto disponibile per " + categoria);
            none.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");
            flowPaneProdotti.getChildren().add(none);
            return;
        }

        for (ProdottoBean p : list) {
            VBox box =  new VBox(10);
            box.setAlignment(Pos.CENTER);
            box.setStyle("-fx-padding: 10px; -fx-background-color: white; -fx-border-color: lightgray; "
                    + "-fx-border-width: 1; -fx-effect: dropshadow(gaussian, lightgray, 10, 0, 3, 3);");
            box.setPrefSize(150, 150);
            Label nome = new Label(p.getNome());
            nome.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            Label prezzo = new Label("€ " + p.getPrezzo());
            HBox controls = new HBox(5);
            controls.setAlignment(Pos.CENTER);
            controls.setSpacing(10);
            Button meno = new Button("-");
            meno.setPrefSize(30, 30);
            meno.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 16px;-fx-cursor: hand;");
            Label qty = new Label("0");
            qty.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");
            Button piu = new Button("+");
            piu.setPrefSize(30, 30);
            piu.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 16px;-fx-cursor: hand;");

            meno.setOnAction(e -> {
                int q = Integer.parseInt(qty.getText());
                if (q > 0) {
                    qty.setText(String.valueOf(q - 1));
                    updateRiepilogo(p.getNome(), p.getPrezzo().doubleValue(), -1);
                }
            });

            piu.setOnAction(e -> {
                int q = Integer.parseInt(qty.getText());
                qty.setText(String.valueOf(q + 1));
                updateRiepilogo(p.getNome(), p.getPrezzo().doubleValue(), 1);
            });

            controls.getChildren().addAll(meno, qty, piu);
            box.getChildren().addAll(nome, prezzo, controls);
            flowPaneProdotti.getChildren().add(box);
        }
    }

    private void caricaProdottiNelRiepilogo(OrdineBean ob) {
        riepilogoContenuto.getChildren().clear();

        List<String> nomi       = ob.getProdotti() != null    ? ob.getProdotti()    : Collections.emptyList();
        List<Integer> qtys      = ob.getQuantita() != null    ? ob.getQuantita()    : Collections.emptyList();
        List<BigDecimal> prezzi = ob.getPrezzi() != null      ? ob.getPrezzi()      : Collections.emptyList();

        if (nomi.size() != qtys.size() || nomi.size() != prezzi.size()) {
            showAlert(Alert.AlertType.ERROR, "Dati ordine corrotti");
            return;
        }

        for (int i = 0; i < nomi.size(); i++) {
            String nome = nomi.get(i);
            int q       = qtys.get(i);
            double pu   = prezzi.get(i).doubleValue();
            updateRiepilogo(nome, pu, q);
        }
    }

    private void updateRiepilogo(String nomeProdotto, double prezzoUnitario, int delta) {
        for (Node node : riepilogoContenuto.getChildren()) {
            if (node instanceof HBox h) {
                Label first = (Label) h.getChildren().get(0);
                if (first.getText().startsWith(nomeProdotto + " x ")) {
                    String[] parts = first.getText().split(" x ");
                    int oldQ = Integer.parseInt(parts[1].trim());
                    int newQ = oldQ + delta;

                    if (newQ <= 0) {
                        riepilogoContenuto.getChildren().remove(h);
                    } else {
                        first.setText(nomeProdotto + " x " + newQ);
                        Label priceLabel = (Label) h.getChildren().get(2);
                        priceLabel.setText(String.format("%.2f €", prezzoUnitario * newQ));
                    }
                    aggiornaTotale();
                    return;
                }
            }
        }
        if (delta > 0) {
            HBox nuovo = new HBox(10);
            nuovo.setAlignment(Pos.CENTER_LEFT);
            nuovo.setCursor(Cursor.HAND);
            nuovo.setOnMouseClicked(evt -> {
                Label lbl = (Label) nuovo.getChildren().get(0);
                String[] parts = lbl.getText().split(" x ");
                String nome = parts[0].trim();
                int q = Integer.parseInt(parts[1].trim());
                Label prLab = (Label) nuovo.getChildren().get(2);
                String txt = prLab.getText().replace("€", "").replace(",", ".").trim();
                double totale = Double.parseDouble(txt);
                double pu = q > 0 ? totale / q : 0.0;
                txtProdotto.setText(nome);
                txtPrezzo.setText(String.format(Locale.ITALY, "%.2f", pu));
                txtQuantita.setText(String.valueOf(q));
            });
            Label lbl = new Label(nomeProdotto + " x " + delta);
            lbl.setPrefWidth(200);
            lbl.setStyle("-fx-font-size: 14px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label price = new Label(String.format("%.2f €", prezzoUnitario * delta));
            price.setStyle("-fx-font-size: 14px;");

            nuovo.getChildren().addAll(lbl, spacer, price);
            riepilogoContenuto.getChildren().add(nuovo);
            aggiornaTotale();
        }
    }

    @FXML
    private void onPulisci() {
        txtProdotto.clear();
        txtPrezzo.clear();
        txtQuantita.clear();
    }

    @FXML
    private void onAggiungi() {
        String nome = txtProdotto.getText().trim();
        if (nome.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Inserisci il nome del prodotto");
            return;
        }

        double prezzo;
        try {
            prezzo = Double.parseDouble(txtPrezzo.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Prezzo non valido");
            return;
        }

        int quantita;
        try {
            quantita = Integer.parseInt(txtQuantita.getText().trim());
            if (quantita < 0) {
                showAlert(Alert.AlertType.WARNING, "La quantità non può essere negativa");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Quantità non valida");
            return;
        }

        riepilogoContenuto.getChildren().removeIf(node ->
                node instanceof HBox h &&
                        ((Label)h.getChildren().get(0)).getText().startsWith(nome + " x ")
        );

        if (quantita > 0) {
            updateRiepilogo(nome, prezzo, quantita);
        }
        aggiornaTotale();
        onPulisci();
    }

    private void aggiornaTotale() {
        double tot = 0.0;
        for (javafx.scene.Node node : riepilogoContenuto.getChildren()) {
            if (node instanceof HBox h) {
                Label priceLabel = (Label)h.getChildren().get(2);
                String txt = priceLabel.getText().replace("€","").replace(",",".").trim();
                tot += Double.parseDouble(txt);
            }
        }
        totaleOrdine.setText(String.format("Totale: €%.2f", tot));
    }

    private static class Triple {
        final List<String> prodotti;
        final List<Integer> quantita;
        final List<java.math.BigDecimal> prezzi;
        Triple(List<String> p, List<Integer> q, List<java.math.BigDecimal> pr) {
            this.prodotti = p; this.quantita = q; this.prezzi = pr;
        }
    }

    private Triple estraiListeDaRiepilogo() {
        var nomi        = new java.util.ArrayList<String>();
        var quantita        = new java.util.ArrayList<Integer>();
        var prezziTotal = new java.util.ArrayList<java.math.BigDecimal>();

        for (javafx.scene.Node node : riepilogoContenuto.getChildren()) {
            if (node instanceof javafx.scene.layout.HBox h) {
                String label = ((Label)h.getChildren().get(0)).getText(); // "Nome x Q"
                String[] parts = label.split(" x ");
                String nome = parts[0].trim();
                int q = Integer.parseInt(parts[1].trim());

                String priceTxt = ((Label)h.getChildren().get(2))
                        .getText().replace("€","").replace(",",".").trim();
                java.math.BigDecimal total = new java.math.BigDecimal(priceTxt);
                java.math.BigDecimal unitPrice = total.divide(
                        java.math.BigDecimal.valueOf(q), 2, java.math.RoundingMode.HALF_UP);

                nomi.add(nome);
                quantita.add(q);
                prezziTotal.add(unitPrice);
            }
        }
        return new Triple(nomi, quantita, prezziTotal);
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }
}