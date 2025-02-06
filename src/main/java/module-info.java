module demo {
    // Moduli JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Per InputStream e altre classi di sistema

    requires java.desktop;
    requires jdk.httpserver;
    requires com.google.gson;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.gson; // Aggiunge il supporto per il logging
    // Aggiungi altre dipendenze necessarie, se applicabile

    // Aprire il pacchetto FXML per JavaFX
    opens com.biteme.app to javafx.fxml;

    // Rimuove pacchetti inutili o non validi
    exports com.biteme.app.core;
    opens com.biteme.app.core to javafx.fxml;
    exports com.biteme.app.controller;
    opens com.biteme.app.controller to javafx.fxml;
    exports com.biteme.app.util;
    opens com.biteme.app.util to javafx.fxml;
    exports com.biteme.app.model;
    opens com.biteme.app.model to javafx.fxml;
    exports com.biteme.app.persistence;
    opens com.biteme.app.persistence to javafx.fxml;
    exports com.biteme.app.persistence.database;
    opens com.biteme.app.persistence.database to javafx.fxml;
    exports com.biteme.app.view;
    opens com.biteme.app.view to javafx.fxml;
    exports com.biteme.app.bean;
    opens com.biteme.app.bean to javafx.fxml;
}