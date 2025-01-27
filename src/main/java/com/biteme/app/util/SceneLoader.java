package com.biteme.app.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneLoader {

    private static Stage primaryStage;

    // Costruttore privato per nascondere quello pubblico implicito (S1118)
    private SceneLoader() {
        throw new UnsupportedOperationException("Non è possibile istanziare la classe SceneLoader.");
    }

    // Configura lo stage principale dell'applicazione
    public static void setPrimaryStage(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Lo stage principale non può essere null.");
        }
        primaryStage = stage;
    }

    // Carica una nuova scena nello stage specificato
    public static void loadScene(String fxmlPath, String title, Stage stage) {
        if (fxmlPath == null || fxmlPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Il percorso del file FXML non può essere vuoto.");
        }

        try {
            FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource(fxmlPath));
            Parent root = loader.load();

            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            // Rilancia una eccezione specifica con informazioni contestuali (S112) senza loggare qui (S2139)
            throw new com.biteme.app.exception.SceneLoadingException("Errore durante il caricamento del file FXML: " + fxmlPath, e);
        }
    }

    // Carica una nuova scena utilizzando lo stage principale configurato
    public static void loadScene(String fxmlPath, String title) {
        if (primaryStage == null) {
            throw new IllegalStateException("Lo stage principale non è stato configurato. Chiama setPrimaryStage() prima di caricare una scena.");
        }
        loadScene(fxmlPath, title, primaryStage);
    }
}