package com.biteme.app.util;

import com.biteme.app.exception.SceneLoadingException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneLoader {
    private static SceneLoader instance;
    private Stage primaryStage;

    // Costruttore privato
    private SceneLoader(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Lo stage principale non può essere null.");
        }
        this.primaryStage = stage;
    }

    // Metodo per ottenere l'istanza con stage (inizializzazione)
    public static SceneLoader getInstance(Stage stage) {
        if (instance == null) {
            instance = new SceneLoader(stage);
        }
        return instance;
    }

    // Overload: metodo per ottenere l'istanza già inizializzata
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
            throw new SceneLoadingException("Errore durante il caricamento del file FXML: " + fxmlPath, e);
        }
    }
}