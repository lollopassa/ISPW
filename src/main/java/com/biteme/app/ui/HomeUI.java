package com.biteme.app.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeUI {

    @FXML
    private Label welcomeMessageLabel; 

    @FXML
    public void initialize() {
        
        showWelcomeMessage();
    }

    
    private void showWelcomeMessage() {
        String message = "Benvenuto! Naviga nel sistema per gestire il ristorante.";
        welcomeMessageLabel.setText(message);
    }
}