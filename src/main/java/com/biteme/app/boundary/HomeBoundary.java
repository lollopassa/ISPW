package com.biteme.app.boundary;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeBoundary {

    @FXML
    private Label welcomeMessageLabel; // Collegamento all'etichetta definita nel FXML

    @FXML
    public void initialize() {
        // Mostra il messaggio di benvenuto
        showWelcomeMessage();
    }

    // Metodo per impostare il messaggio di benvenuto
    private void showWelcomeMessage() {
        String message = "Benvenuto! Naviga nel sistema per gestire il ristorante.";
        welcomeMessageLabel.setText(message);
    }
}