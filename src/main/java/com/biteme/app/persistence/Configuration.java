package com.biteme.app.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration {
    private static final Logger logger = Logger.getLogger(Configuration.class.getName());
    private static final Properties properties = new Properties();
    private static String persistenceMode = "in memory";
    static {
        try (InputStream input = Configuration.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
                String configuredPersistenceMode = properties.getProperty("persistence.mode");
                if (configuredPersistenceMode != null && !configuredPersistenceMode.isBlank()) {
                    persistenceMode = configuredPersistenceMode.toLowerCase();
                }
            } else {
                logger.warning(() -> "[AVVISO] File config.properties non trovato. Persistenza predefinita: " + persistenceMode + ".");
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, e, () -> "[AVVISO] Errore nel caricamento del file di configurazione. Persistenza predefinita: " + persistenceMode + ".");
        }
    }

    private Configuration() {
            }

    public static PersistenceProvider getPersistenceProvider() {
        return PersistenceProvider.getProviderByName(persistenceMode);
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

    public static String getUiMode() {
        return properties.getProperty("ui.mode", "fx").toLowerCase();     }
}
