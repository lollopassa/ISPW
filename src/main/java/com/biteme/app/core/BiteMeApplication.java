package com.biteme.app.core;

import com.biteme.app.cli.LoginCLI;
import com.biteme.app.util.Configuration;
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
            SceneLoader.setPrimaryStage(stage);
            logger.log(Level.INFO, "Caricamento della scena iniziale.");
            SceneLoader.loadScene("/com/biteme/app/login.fxml", "Login - Applicazione BiteMe");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Errore durante l'avvio dell'applicazione: {0}", e.getMessage());
        }
    }

    public static void main(String[] args) {
        logger.log(Level.INFO, "Avvio dell'applicazione BiteMe.");

        String uiMode = Configuration.getUiMode(); // Legge dal config.properties
        if ("cli".equalsIgnoreCase(uiMode)) {
            logger.log(Level.INFO, "Avvio in modalità CLI.");
            LoginCLI.login();
        } else {
            logger.log(Level.INFO, "Avvio in modalità JavaFX.");
            launch(); // Metodo standard JavaFX
        }
    }
}
