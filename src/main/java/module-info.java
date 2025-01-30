module demo {
    // Moduli JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Per InputStream e altre classi di sistema

    requires java.sql;

    requires java.logging;
    requires java.desktop; // Aggiunge il supporto per il logging
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
    exports com.biteme.app.entity;
    opens com.biteme.app.entity to javafx.fxml;
    exports com.biteme.app.persistence;
    opens com.biteme.app.persistence to javafx.fxml;
    exports com.biteme.app.persistence.database;
    opens com.biteme.app.persistence.database to javafx.fxml;
    exports com.biteme.app.boundary;
    opens com.biteme.app.boundary to javafx.fxml;
    exports com.biteme.app.bean;
    opens com.biteme.app.bean to javafx.fxml;
}
