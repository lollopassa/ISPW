package com.biteme.app.util;

import com.biteme.app.exception.GoogleAuthException;
import com.biteme.app.exception.ServerInitializationException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

public class GoogleAuthUtility {
    // Autenticazione che restituisce direttamente l'access token
    public static String authenticate() throws GoogleAuthException, InterruptedException {
        try {
            String accessToken = performAuthentication();
            if (accessToken == null) {
                throw new GoogleAuthException("Autenticazione Google fallita. Verifica i dettagli e riprova.");
            }
            return accessToken;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GoogleAuthException("Il thread è stato interrotto durante l'autenticazione con Google.", e);
        } catch (Exception e) {
            throw new GoogleAuthException("Errore durante l'autenticazione con Google", e);
        }
    }

    // Metodo che ottiene i dati dell'utente da Google usando l'access token
    public static GoogleUserData getGoogleUserData(String accessToken) throws GoogleAuthException {
        try {
            String url = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" + accessToken;
            try (Scanner scanner = new Scanner(new URL(url).openStream())) {
                String response = scanner.useDelimiter("\\A").next();

                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                return new GoogleUserData(
                        jsonObject.get("email").getAsString(),
                        jsonObject.get("name").getAsString()
                );
            }
        } catch (Exception e) {
            throw new GoogleAuthException("Errore durante il recupero dei dati utente Google", e);
        }
    }

    // Classe di supporto per contenere i dati dell'utente
    public static class GoogleUserData {
        private final String email;
        private final String name;

        public GoogleUserData(String email, String name) {
            this.email = email;
            this.name = name;
        }

        public String getEmail() { return email; }
        public String getName() { return name; }
    }

    // Metodo privato per gestire il flusso di autenticazione e ottenere l'access token
    private static String performAuthentication() throws GoogleAuthException, InterruptedException {
        try {
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    Configuration.getGoogleClientId(),
                    Configuration.getGoogleClientSecret(),
                    Arrays.asList(
                            "https://www.googleapis.com/auth/userinfo.profile",
                            "https://www.googleapis.com/auth/userinfo.email",
                            "https://mail.google.com/" // Scope per invio email
                    ))
                    .build();

            LocalServer localServer = new LocalServer();
            localServer.start(8080);

            String url = flow.newAuthorizationUrl()
                    .setRedirectUri(Configuration.getGoogleRedirectUri())
                    .build();

            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));

            while (localServer.getAuthCode() == null) {
                Thread.sleep(1000);
            }

            GoogleTokenResponse response = flow.newTokenRequest(localServer.getAuthCode())
                    .setRedirectUri(Configuration.getGoogleRedirectUri())
                    .execute();

            return response.getAccessToken(); // Restituisce l'access token
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new GoogleAuthException("Errore durante il processo di autenticazione", e);
        }
    }

    // Classe per gestire il server locale per ottenere il codice di autenticazione
    private static class LocalServer {
        private com.sun.net.httpserver.HttpServer server;
        private String authCode;

        public void start(int port) throws ServerInitializationException {
            try {
                server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(port), 0);
                server.createContext("/callback", exchange -> {
                    String query = exchange.getRequestURI().getQuery();
                    authCode = query.split("code=")[1].split("&")[0];

                    String response = "Login completato! Puoi chiudere questa finestra.";
                    exchange.sendResponseHeaders(200, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.close();

                    stop();
                });
                server.start();
            } catch (Exception e) {
                throw new ServerInitializationException("Errore durante l'avvio del server sulla porta: " + port, e);
            }
        }

        public void stop() {
            if (server != null) {
                server.stop(0);
            }
        }

        public String getAuthCode() {
            return authCode;
        }
    }
}
