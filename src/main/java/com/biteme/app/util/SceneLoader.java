package com.biteme.app.util;

import com.biteme.app.exception.SceneLoadingException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SceneLoader {
    private static SceneLoader instance;
    private final Stage primaryStage;
    private static final Logger logger = Logger.getLogger(SceneLoader.class.getName());

    private final Map<String, Parent> sceneCache = new HashMap<>();
    private final Map<String, Object> controllerCache = new HashMap<>();

    private SceneLoader(Stage stage) {
        if (stage == null) throw new IllegalArgumentException("Stage non può essere null.");
        this.primaryStage = stage;
    }

    public static synchronized SceneLoader getInstance(Stage stage) {
        if (instance == null) {
            instance = new SceneLoader(stage);
        }
        return instance;
    }

    public static synchronized SceneLoader getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Prima chiama getInstance(Stage).");
        }
        return instance;
    }

    public void loadScene(String fxmlPath, String title) {
        Parent root = loadRoot(fxmlPath);
        primaryStage.setTitle(title);

        if (primaryStage.getScene() != null) {
            primaryStage.getScene().setRoot(root);
        } else {
            primaryStage.setScene(new Scene(root));
        }

        primaryStage.show();
    }

    private Parent loadRoot(String fxmlPath) {
        if (fxmlPath == null || fxmlPath.isBlank()) {
            throw new IllegalArgumentException("Percorso FXML non valido.");
        }

        if (sceneCache.containsKey(fxmlPath)) {
            return sceneCache.get(fxmlPath);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            root.getProperties().put("loader", loader);
            sceneCache.put(fxmlPath, root);
            controllerCache.put(fxmlPath, loader.getController());

            return root;
        } catch (IOException e) {
            SceneLoadingException ex = new SceneLoadingException(
                    "Errore nel caricamento FXML: " + fxmlPath, e);
            handleSceneLoadingError(ex);
            throw ex;
        }
    }

    private void handleSceneLoadingError(SceneLoadingException e) {
        logger.log(Level.SEVERE, "Errore caricamento scena", e);
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Errore Caricamento Scena");
        alert.setHeaderText("Non ho potuto caricare la scena.");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}