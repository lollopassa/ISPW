package com.biteme.app.service;

import com.biteme.app.entity.User;
import com.biteme.app.exception.GoogleAuthException;
import com.biteme.app.persistence.UserDao;
import com.biteme.app.util.GoogleAuthUtility;
import com.biteme.app.util.HashingUtil;

import java.security.SecureRandom;
import java.util.Base64;

public class GoogleAuthService {

    private final UserDao userDao;

    public GoogleAuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User authenticateWithGoogle() throws GoogleAuthException {
        try {
            GoogleAuthUtility.GoogleUserData googleUser = GoogleAuthUtility.authenticate();

            if (googleUser.getEmail() == null) {
                throw new IllegalStateException("Autenticazione Google fallita");
            }

            return userDao.load(googleUser.getEmail())
                    .map(this::validateGoogleUser)
                    .orElseGet(() -> registerGoogleUser(googleUser));
        } catch (Exception e) {
            // Se qualsiasi eccezione viene lanciata, la incapsuliamo in GoogleAuthException
            throw new GoogleAuthException("Errore durante l'autenticazione con Google", e);
        }
    }

    private User validateGoogleUser(User user) {
        if (!user.isGoogleUser()) {
            throw new IllegalStateException("Email già registrata con metodo tradizionale");
        }
        return user;
    }

    private User registerGoogleUser(GoogleAuthUtility.GoogleUserData googleUser) {
        User newUser = new User(generateUsernameFromEmail(googleUser.getEmail()));
        newUser.setEmail(googleUser.getEmail());
        newUser.setPassword(HashingUtil.hashPassword(generateRandomPassword()));
        newUser.setGoogleUser(true);
        userDao.store(newUser);
        return newUser;
    }

    private String generateUsernameFromEmail(String email) {
        String base = email.split("@")[0];
        int suffix = 1;
        String username = base;
        while(userDao.exists(username)) {
            username = base + suffix;
            suffix++;
        }
        return username;
    }
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}