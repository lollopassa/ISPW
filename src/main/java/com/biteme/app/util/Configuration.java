package com.biteme.app.util;

import com.biteme.app.persistence.PersistenceProvider;

import javax.xml.parsers.SAXParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    private static final Properties properties = new Properties();
    private static String persistenceMode; // Modalità di persistenza letta da config.properties

    // Caricamento delle proprietà dal file config.properties
    static {
        try (InputStream input = Configuration.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);

                // Leggere la modalità di persistenza dal file config.properties
                persistenceMode = properties.getProperty("persistence.mode", "inmemory").toLowerCase(); // Predefinito "inmemory"
            } else {
                throw new IllegalStateException("Impossibile trovare il file config.properties");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Errore durante il caricamento del file di configurazione", e);
        }
    }

    // Costruttore privato per impedire l'istanziazione
    private Configuration() {
        // Costruttore privato vuoto
    }

    // Metodo per ottenere il provider di persistenza configurato.
    public static PersistenceProvider getPersistenceProvider() {
        return PersistenceProvider.getProviderByName(persistenceMode);
    }

    // Metodo per ottenere una proprietà configurata (es. App ID di Facebook)
    public static String getFacebookAppId() {
        return properties.getProperty("facebook.app.id");
    }

    // Metodo per ottenere il segreto dell'App di Facebook
    public static String getFacebookAppSecret() {
        return properties.getProperty("facebook.app.secret");
    }

    public static String getGoogleClientId() {
        return properties.getProperty("google.client.id");
    }
    public static String getGoogleClientSecret() {
        return properties.getProperty("google.client.secret");
    }
    public static String getGoogleRedirectUri() {
        return properties.getProperty("google.redirect.uri");
    }
}