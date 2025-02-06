package com.biteme.app.service;

import com.biteme.app.model.User;
import com.biteme.app.exception.GoogleAuthException;
import com.biteme.app.util.GoogleAuthUtility;
import com.biteme.app.util.HashingUtil;

import java.security.SecureRandom;
import java.util.Base64;

public class GoogleAuthService {

    // Modifichiamo il metodo per restituire l'access token
    public String authenticateWithGoogle() throws GoogleAuthException {
        try {
            return GoogleAuthUtility.authenticate();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GoogleAuthException("Il thread è stato interrotto durante l'autenticazione con Google.", e);
        } catch (Exception e) {
            throw new GoogleAuthException("Errore durante l'autenticazione con Google", e);
        }
    }

    // Aggiungiamo un nuovo metodo per ottenere i dati utente
    public GoogleAuthUtility.GoogleUserData getGoogleUserData(String accessToken) throws GoogleAuthException {
        return GoogleAuthUtility.getGoogleUserData(accessToken);
    }

    public User createGoogleUser(GoogleAuthUtility.GoogleUserData googleUser) {
        User newUser = new User(generateUsernameFromEmail(googleUser.getEmail()));
        newUser.setEmail(googleUser.getEmail());
        newUser.setPassword(HashingUtil.hashPassword(generateRandomPassword()));
        newUser.setGoogleUser(true);
        return newUser;
    }

    private String generateUsernameFromEmail(String email) {
        return email.split("@")[0];
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
