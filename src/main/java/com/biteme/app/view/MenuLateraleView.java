package com.biteme.app.view;

import com.biteme.app.controller.LoginController;
import com.biteme.app.util.SceneLoader;
import com.biteme.app.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MenuLateraleView {

    private LoginController loginController;

    private static final Logger LOGGER = Logger.getLogger(MenuLateraleView.class.getName());

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
        UserSession.clear();
        SceneLoader.loadScene("/com/biteme/app/login.fxml", "Login");
    }

    @FXML
    public void initialize() {
        this.loginController = new LoginController();
        if (loginController.isUserAdmin()) {
            homeButton.setOnMouseClicked(_ -> SceneLoader.loadScene("/com/biteme/app/adminHome.fxml", "Admin Home"));
        } else {
            homeButton.setOnMouseClicked(_ -> SceneLoader.loadScene("/com/biteme/app/home.fxml", "Home Page"));
        }

        prenotazioniButton.setOnMouseClicked(_ -> SceneLoader.loadScene("/com/biteme/app/prenotazioni.fxml", "Prenotazioni"));
        ordiniButton.setOnMouseClicked(_ -> SceneLoader.loadScene("/com/biteme/app/ordinazione.fxml", "Ordini"));
        magazzinoButton.setOnMouseClicked(_ -> SceneLoader.loadScene("/com/biteme/app/prodotto.fxml", "Magazzino"));
        logoutButton.setOnMouseClicked(_ -> onLogout());
    }
}