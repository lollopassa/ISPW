package com.biteme.app.boundary;

import com.biteme.app.util.SceneLoader;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MenuLateraleBoundary {

    private static final Logger LOGGER = Logger.getLogger(MenuLateraleBoundary.class.getName());

    @FXML
    private StackPane homeButton;

    @FXML
    private StackPane prenotazioniButton;

    @FXML
    private StackPane ordiniButton;

    @FXML
    private StackPane cucinaButton;

    @FXML
    private StackPane magazzinoButton;

    @FXML
    private StackPane logoutButton;


    private void onLogout() {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Effettuato il logout");
        }
        SceneLoader.loadScene("/com/biteme/app/login.fxml", "Login");
    }

    @FXML
    public void initialize() {
        homeButton.setOnMouseClicked(_ -> SceneLoader.loadScene("/com/biteme/app/home.fxml", "Home Page"));
        prenotazioniButton.setOnMouseClicked(_ -> SceneLoader.loadScene("/com/biteme/app/prenotazioni.fxml", "Prenotazioni"));
        ordiniButton.setOnMouseClicked(_ -> SceneLoader.loadScene("/com/biteme/app/ordinazione.fxml", "Ordini"));
        cucinaButton.setOnMouseClicked(_ -> SceneLoader.loadScene("/com/biteme/app/cucina.fxml", "Cucina"));
        magazzinoButton.setOnMouseClicked(_ -> SceneLoader.loadScene("/com/biteme/app/prodotto.fxml", "Magazzino"));
        logoutButton.setOnMouseClicked(_ -> onLogout());
    }
}