package com.biteme.app.core;

import com.biteme.app.util.SceneLoader;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BiteMeApplication extends Application {

    private static final Logger logger = Logger.getLogger(BiteMeApplication.class.getName());

    @Override
    public void start(Stage stage) {
        try {
            // Configura lo Stage principale nel SceneLoader
            SceneLoader.setPrimaryStage(stage);

            // Carica la scena iniziale
            logger.log(Level.INFO, "Caricamento della scena iniziale.");
            SceneLoader.loadScene("/com/biteme/app/login.fxml", "Login - Applicazione BiteMe");

        } catch (Exception e) {
            // Gestione generale di eventuali errori durante l'avvio dell'applicazione
            logger.log(Level.SEVERE, "Errore durante l'avvio dell'applicazione: {0}", e.getMessage());
        }
    }

    public static void main(String[] args) {
        logger.log(Level.INFO, "Avvio dell'applicazione BiteMe.");
        launch(); // Metodo standard JavaFX per avviare l'applicazione
    }
}