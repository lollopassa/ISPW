package com.biteme.app.util;

import com.biteme.app.persistence.PersistenceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    private static final Properties properties = new Properties();
    private static String persistenceMode = "in memory"; // Imposta il valore predefinito come "in memory"

    // Caricamento delle proprietà dal file config.properties
    static {
        try (InputStream input = Configuration.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);

                // Leggere la modalità di persistenza dal file config.properties
                String configuredPersistenceMode = properties.getProperty("persistence.mode");
                if (configuredPersistenceMode != null && !configuredPersistenceMode.isBlank()) {
                    persistenceMode = configuredPersistenceMode.toLowerCase(); // Usa la configurazione dal file se presente
                }
            } else {
                System.err.println("[AVVISO] File config.properties non trovato. Persistenza predefinita: in-memory.");
            }
        } catch (IOException e) {
            System.err.println("[AVVISO] Errore nel caricamento del file di configurazione. Persistenza predefinita: in-memory.");
        }
    }

    // Costruttore privato per impedire l'instanziazione della classe
    private Configuration() {
        // Costruttore privato per disabilitare l'istanziazione
    }

    // Metodo per ottenere il provider di persistenza configurato
    public static PersistenceProvider getPersistenceProvider() {
        return PersistenceProvider.getProviderByName(persistenceMode);
    }

    // Altri metodi per ottenere configurazioni aggiuntive
    public static String getGoogleClientId() {
        return properties.getProperty("google.client.id");
    }

    public static String getGoogleClientSecret() {
        return properties.getProperty("google.client.secret");
    }

    public static String getGoogleRedirectUri() {
        return properties.getProperty("google.redirect.uri");
    }

    public static String getUiMode() {
        return properties.getProperty("ui.mode", "fx").toLowerCase(); // Default: "fx"
    }
}