module demo {
    // Moduli JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Altri moduli Java SE
    requires java.desktop;
    requires jdk.httpserver;

    // Librerie di terze parti
    requires com.google.gson;
    requires google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client;
    requires com.google.api.client.auth;
    requires com.google.api.services.gmail; // Aggiunto per la Gmail API
    requires java.mail;

    // Aprire il pacchetto principale per JavaFX
    opens com.biteme.app to javafx.fxml;

    // Esportazioni e opens dei pacchetti del progetto
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
    exports com.biteme.app.google;
    opens com.biteme.app.google to javafx.fxml;
}