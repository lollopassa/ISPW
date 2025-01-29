package com.biteme.app.util;

import com.biteme.app.persistence.PersistenceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    // Scegli il provider di persistenza (es. "in memory" o "database")
    private static final String PERSISTENCE_MODE = "database";

    private static final Properties properties = new Properties();

    // Caricamento delle proprietà dal file config.properties
    static {
        try (InputStream input = Configuration.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                throw new IllegalStateException("Impossibile trovare il file config.properties");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Errore durante il caricamento del file di configurazione", e);
        }
    }

    // Costruttore privato per impedire l'istanziazione
    private Configuration() {
        // Costruttore privato vuoto
    }

    // Metodo per ottenere il provider di persistenza configurato.
    public static PersistenceProvider getPersistenceProvider() {
        return PersistenceProvider.getProviderByName(PERSISTENCE_MODE);
    }

    // Metodo per ottenere una proprietà configurata (es. App ID di Facebook)
    public static String getFacebookAppId() {
        return properties.getProperty("facebook.app.id");
    }

    // Metodo per ottenere il segreto dell'App di Facebook
    public static String getFacebookAppSecret() {
        return properties.getProperty("facebook.app.secret");
    }
}