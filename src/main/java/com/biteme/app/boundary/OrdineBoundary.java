package com.biteme.app.boundary;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.OrdineController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.List;

public class OrdineBoundary {

    private final OrdineController controller;

    @FXML
    private FlowPane flowPaneProdotti; // Pane dove mostreremo i prodotti

    public OrdineBoundary() {
        this.controller = new OrdineController();
    }

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
        caricaProdotti("Primi Piatti");
    }

    @FXML
    private void handleCategoriaSecondiPiatti() {
        caricaProdotti("Secondi Piatti");
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
        // Ottieni i prodotti tramite il controller
        List<ProdottoBean> prodotti = controller.getProdottiByCategoria(categoria);

        flowPaneProdotti.getChildren().clear(); // Pulisci il contenuto precedente

        // Crea un elemento visivo per ogni prodotto e aggiungilo al FlowPane
        for (ProdottoBean prodotto : prodotti) {
            Label labelProdotto = new Label(prodotto.getNome() + " - €" + prodotto.getPrezzo());
            labelProdotto.setStyle("-fx-padding: 10px; -fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-font-size: 14px;");
            flowPaneProdotti.getChildren().add(labelProdotto);
        }
    }
}