package com.biteme.app.util;

import com.biteme.app.exception.SceneLoadingException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SceneLoader {
    private static SceneLoader instance;
    private final Stage primaryStage;
    private static final Logger logger = Logger.getLogger(SceneLoader.class.getName());

    private SceneLoader(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Lo stage principale non può essere null.");
        }
        this.primaryStage = stage;
    }

    public static SceneLoader getInstance(Stage stage) {
        if (instance == null) {
            instance = new SceneLoader(stage);
        }
        return instance;
    }

    public static SceneLoader getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SceneLoader non è stato inizializzato. Chiama prima getInstance(Stage stage).");
        }
        return instance;
    }

    public void loadScene(String fxmlPath, String title) {
        if (fxmlPath == null || fxmlPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Il percorso del file FXML non può essere vuoto.");
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            primaryStage.setTitle(title);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            SceneLoadingException exception = new SceneLoadingException("Errore durante il caricamento del file FXML: " + fxmlPath, e);
            handleSceneLoadingError(exception);
        }
    }

    private void handleSceneLoadingError(SceneLoadingException e) {
        logger.log(Level.SEVERE, "Errore nel caricamento della scena: ", e);

        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Errore nel Caricamento della Scena");
        alert.setHeaderText("Impossibile caricare la scena.");
        alert.setContentText("Si è verificato un errore durante il caricamento della scena.\n\nDettagli: " + e.getMessage());
        alert.showAndWait();

    }
}