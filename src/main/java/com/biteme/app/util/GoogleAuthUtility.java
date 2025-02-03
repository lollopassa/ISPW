package com.biteme.app.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URL;
import java.util.Collections;
import java.util.Scanner;

public class GoogleAuthUtility {

    public static class GoogleUserData {
        private final String email;
        private final String name;
        private final String pictureUrl;

        public GoogleUserData(String email, String name, String pictureUrl) {
            this.email = email;
            this.name = name;
            this.pictureUrl = pictureUrl;
        }

        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getPictureUrl() { return pictureUrl; }
    }

    public static GoogleUserData authenticate() throws Exception {
        String accessToken = performAuthentication();
        return getGoogleUserData(accessToken);
    }

    private static String performAuthentication() throws Exception {
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                Configuration.getGoogleClientId(),
                Configuration.getGoogleClientSecret(),
                Collections.singletonList("https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email"))
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

        return response.getAccessToken();
    }

    private static GoogleUserData getGoogleUserData(String accessToken) throws Exception {
        String url = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" + accessToken;
        String response = new Scanner(new URL(url).openStream(), "UTF-8").useDelimiter("\\A").next();

        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        return new GoogleUserData(
                jsonObject.get("email").getAsString(),
                jsonObject.get("name").getAsString(),
                jsonObject.get("picture").getAsString()
        );
    }

    private static class LocalServer {
        private com.sun.net.httpserver.HttpServer server;
        private String authCode;

        public void start(int port) throws Exception {
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