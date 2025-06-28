module demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.desktop;
    requires jdk.httpserver;

    requires com.google.gson;
    requires google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client;
    requires com.google.api.client.auth;
    requires com.google.api.services.gmail;     requires java.mail;

    opens com.biteme.app to javafx.fxml;

    exports com.biteme.app.core;
    opens com.biteme.app.core to javafx.fxml;
    exports com.biteme.app.controller;
    opens com.biteme.app.controller to javafx.fxml;
    exports com.biteme.app.util;
    opens com.biteme.app.util to javafx.fxml;
    exports com.biteme.app.entities;
    opens com.biteme.app.entities to javafx.fxml;
    exports com.biteme.app.persistence;
    opens com.biteme.app.persistence to javafx.fxml;
    exports com.biteme.app.persistence.database;
    opens com.biteme.app.persistence.database to javafx.fxml;
    exports com.biteme.app.boundary;
    opens com.biteme.app.boundary to javafx.fxml;
    exports com.biteme.app.bean;
    opens com.biteme.app.bean to javafx.fxml;
    exports com.biteme.app.googleapi;
    opens com.biteme.app.googleapi to javafx.fxml;
    exports com.biteme.app.exception;
    exports com.biteme.app.ui;
    opens com.biteme.app.ui to javafx.fxml;
    exports com.biteme.app.util.mapper;
    opens com.biteme.app.util.mapper to javafx.fxml;
}