package com.biteme.app.persistence.database;

import com.biteme.app.exception.DatabaseConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final String CONFIG_FILE = "/config.properties";     private static String url;
    private static String user;
    private static String password;

    private DatabaseConnection() {
        throw new UnsupportedOperationException("Questa è una classe di utilità e non può essere istanziata.");
    }

        static {
        Properties properties = new Properties();

        try (InputStream fis = DatabaseConnection.class.getResourceAsStream(CONFIG_FILE)) {
            if (fis == null) {
                throw new DatabaseConfigurationException("Il file config.properties non è trovato nel classpath.");
            }
            properties.load(fis);

            url = properties.getProperty("db.url");
            user = properties.getProperty("db.username");
            password = properties.getProperty("db.password");

                        if (url == null || user == null || password == null) {
                throw new DatabaseConfigurationException("Una o più proprietà di configurazione mancano nel file config.properties.");
            }
        } catch (IOException e) {
            throw new DatabaseConfigurationException("Errore durante il caricamento del file di configurazione.", e);
        }
    }

        public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

        public static String getDatabaseUrl() {
        return url;
    }

    public static String getDatabaseUser() {
        return user;
    }

    public static String getDatabasePassword() {
        return password;
    }
}